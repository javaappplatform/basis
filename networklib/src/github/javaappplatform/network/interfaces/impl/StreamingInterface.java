/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.interfaces.impl;

import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.commons.io.InPipeOut;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.ISession;
import github.javaappplatform.network.interfaces.IInterfaceType;
import github.javaappplatform.network.interfaces.ISessionInterface;
import github.javaappplatform.network.interfaces.ReusableMSGBuffers;
import github.javaappplatform.network.msg.Converter;
import github.javaappplatform.network.msg.IMessage;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This interface can be used to stream data through a session. The interface is capable to transfer and receive several streams at once.
 * To distinguish streams int-based IDs are used (see e.g. {@link NetInputStream#id()} or {@link #request(int)}).
 * These IDs have to be somehow exchanged or defined between the connected clients e.g. through a command or resource interface.
 * @author funsheep
 */
public class StreamingInterface implements ISessionInterface
{

	public static final String ID = "de.d3fact.network.interfaces.impl.Streaming";

	public static final int DEFAULT_STREAMING_PACKAGE_SIZE = 4096;
	private static final Logger LOGGER = Logger.getLogger();


	private final class OutgoingInputStream implements IListener, Runnable
	{

		public final int id;
		public final int length;
		public final InputStream stream;
		private final Semaphore parallelMSGs = new Semaphore(PARALLEL_SEND_MSGS);
		private boolean headSend = false;
		private boolean tailSend = false;
		private final IListener externalCallback;

		public OutgoingInputStream(int id, InputStream stream, int length)
		{
			this(id, stream, length, null);
		}

		public OutgoingInputStream(int id, InputStream stream, int length, IListener externalCallback)
		{
			this.id = id;
			this.length = length;
			this.stream = stream;
			this.externalCallback = externalCallback;
		}


		private void send() throws IOException, InterruptedException
		{
			if (this.headSend)
				throw new IllegalStateException("This inputstream is already send or sending.");
			try
			{
				this.parallelMSGs.acquire();
				byte[] buffer = StreamingInterface.this.buffers.pullBuffer();
				Converter.putIntBig(buffer, 0, this.id);
				Converter.putIntBig(buffer, 4, this.length);
				int read = fillBuffer(this.stream, buffer, 8);
				StreamingInterface.this.buffers.asyncSendMSGTCP(IMessageAPI.MSGTYPE_STREAM_HEADER, buffer, 0, read+8, this);
				this.headSend = true;

				if (read+8 < buffer.length)
				{
					this.parallelMSGs.acquire();
					buffer = StreamingInterface.this.buffers.pullBuffer();
					Converter.putIntBig(buffer, 0, this.id);
					StreamingInterface.this.buffers.asyncSendMSGTCP(IMessageAPI.MSGTYPE_STREAM_TAIL, buffer, 0, 4, this);
					this.tailSend = true;
					return;
				}

				while (!this.tailSend)
				{
					this.parallelMSGs.acquire();
					buffer = StreamingInterface.this.buffers.pullBuffer();
					Converter.putIntBig(buffer, 0, this.id);
					read = fillBuffer(this.stream, buffer, 4);
					if (read+4 == buffer.length)
						StreamingInterface.this.buffers.asyncSendMSGTCP(IMessageAPI.MSGTYPE_STREAM_INTERMEDIATE, buffer, 0, read+4, this);
					else
					{
						StreamingInterface.this.buffers.asyncSendMSGTCP(IMessageAPI.MSGTYPE_STREAM_TAIL, buffer, 0, read+4, this);
						this.tailSend = true;
					}
				}
			}
			catch (IOException e)
			{
				byte[] buffer = StreamingInterface.this.buffers.pullBuffer();
				Converter.putIntBig(buffer, 0, this.id);
				this.tailSend = true;
				StreamingInterface.this.buffers.asyncSendMSGTCP(IMessageAPI.MSGTYPE_STREAM_ERROR, buffer, 0, 4, null);
				LOGGER.debug("Streaming encountered an error.", e);
				throw e;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handleEvent(Event e)
		{
			this.parallelMSGs.release();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run()
		{
			try
			{
				this.send();
				if (this.externalCallback != null)
					this.externalCallback.handleEvent(new Event(StreamingInterface.this, InPipeOut.EVENT_PIPE_OK, Integer.valueOf(this.id)));
			}
			catch (IOException | InterruptedException e)
			{
				if (this.externalCallback != null)
					this.externalCallback.handleEvent(new Event(StreamingInterface.this, InPipeOut.EVENT_PIPE_ERROR, new Object[] { Integer.valueOf(this.id), e }));
			}
			finally
			{
				StreamingInterface.this.close(this);
			}
		}

	}
	private static final int fillBuffer(InputStream in, byte[] buffer, final int off) throws IOException
	{
		int len = buffer.length - off;
		int eof = in.read(buffer, off, len);
		if (eof == -1)
			return -1;
		len -= eof;
		int read = eof;
		while (len > 0)
		{
			eof = in.read(buffer, read + off, len);
			if (eof == -1)
				return read;
			read += eof;
			len -= eof;
		}
		return read;
	}



	private final class OutgoingStream extends OutputStream implements IListener, Runnable
	{

		public final int id;
		public final int length;
		private final Semaphore parallelMSGs = new Semaphore(PARALLEL_SEND_MSGS);
		private byte[] buffer;
		private int position = 8;
		private boolean headSend = false;
		private boolean tailSend = false;


		public OutgoingStream(int id, int length)
		{
			this.id = id;
			this.length = length;
			this.buffer = StreamingInterface.this.buffers.pullBuffer();
			Converter.putIntBig(this.buffer, 0, this.id);
			Converter.putIntBig(this.buffer, 4, this.length);
			this.position = 8;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public synchronized void write(int b) throws IOException
		{
			try
			{
				if (this.tailSend)
					throw new IllegalStateException("Tail has already been send and stream is closed.");
				this.buffer[this.position] = (byte) b;
				this.position++;
				if (this.position == this.buffer.length)
					this.flush();
			}
			catch (IOException e)
			{
				this._close();
				LOGGER.debug("Streaming encountered an error.", e);
				throw e;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public synchronized void write(byte b[], int off, int len) throws IOException
		{
			try
			{
				if (this.tailSend)
					throw new IllegalStateException("Tail of OutputStream "+this.id+" has already been send and stream is closed.");
				if ((off < 0) || (off > b.length) || (len < 0) ||
					((off + len) - b.length > 0)) {
					throw new IndexOutOfBoundsException();
				}
				int tocopy = 0;
				while (len > (tocopy = this.buffer.length-this.position))
				{
					System.arraycopy(b, off, this.buffer, this.position, tocopy);
					this.position = this.buffer.length;
					len -= tocopy;
					off += tocopy;
					this.flush();
				}
				if (len > 0)
				{
					System.arraycopy(b, off, this.buffer, this.position, len);
					this.position += len;
				}
			}
			catch (IOException e)
			{
				this._close();
				LOGGER.debug("Streaming encountered an error.", e);
				throw e;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public synchronized void flush() throws IOException
		{
			try
			{
				if (this.tailSend)
					return;

				this.parallelMSGs.acquire();

				if (!this.headSend)
				{
					StreamingInterface.this.buffers.asyncSendMSGTCP(IMessageAPI.MSGTYPE_STREAM_HEADER, this.buffer, 0, this.position, this);
					this.headSend = true;
				}
				else
					StreamingInterface.this.buffers.asyncSendMSGTCP(IMessageAPI.MSGTYPE_STREAM_INTERMEDIATE, this.buffer, 0, this.position, this);
				this.buffer = StreamingInterface.this.buffers.pullBuffer();
				Converter.putIntBig(this.buffer, 0, this.id);
				this.position = 4;
			}
			catch (InterruptedException e)
			{
				this._close();
				throw new IOException("Connection timeout.");
			}
			catch (IOException e)
			{
				this._close();
				LOGGER.debug("Streaming encountered an error.", e);
				throw e;
			}
		}

		private void _close()
		{
			this.tailSend = true;
			this.buffer = null;
			StreamingInterface.this.close(this.id);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public synchronized void close() throws IOException
		{
			if (this.tailSend)
				return;

			this.flush();
			
			try
			{
				StreamingInterface.this.buffers.asyncSendMSGTCP(IMessageAPI.MSGTYPE_STREAM_TAIL, this.buffer, 0, this.position, null);
			}
			finally
			{
				this._close();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handleEvent(Event e)
		{
			this.parallelMSGs.release();
		}

		private synchronized boolean tailSend()
		{
			return this.tailSend;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run()
		{
			try
			{
				while (!this.tailSend())
				{
					Thread.sleep(INetworkAPI.CONNECTION_TIMEOUT / 2);
					this.flush();
				}
			}
			catch (InterruptedException | IOException e)
			{
				LOGGER.debug("Keepalive threw exception.", e);
			}
		}

	}


	private static final int PARALLEL_SEND_MSGS = 4;
	private static final int OFFSET_STREAM_ID = 0;


	private ReusableMSGBuffers buffers;
	private final ReentrantLock incommingStreamLock = new ReentrantLock();
	private final TIntObjectHashMap<NetInputStream> incommingStreams = new TIntObjectHashMap<>(10);
	private final ReentrantLock outgoingStreamLock = new ReentrantLock();
	private final TIntObjectHashMap<Closeable> outgoingStreams = new TIntObjectHashMap<>(10);
	private final ExecutorService threadPool = Executors.newCachedThreadPool();


	/**
	 * Use this method to retrieve the specific inputstream with the given id. Please use this method for each id only once.
	 * If the stream with the given ID has not started yet, the method returns an empty {@link NetInputStream} implementation
	 * which is filled with data once the data arrives. This is useful e.g. when you expect a certain stream with a defined id and
	 * you don't want to make assumption when the data really arrives.
	 * @param id The id of the inputstream.
	 * @return A NetInputStream implementation.
	 */
	public NetInputStream request(int id)
	{
		this.incommingStreamLock.lock();
		try
		{
			NetInputStream stream = this.incommingStreams.get(id);
			if (stream == null)
			{
				stream = new NetInputStream(id, this);
				this.incommingStreams.put(id, stream);
			}
			return stream;
		}
		finally
		{
			this.incommingStreamLock.unlock();
		}
	}


	/**
	 * Sends the data of the given inputstream through the associated session to the other side. The stream can be identified by the other client through the given ID.
	 * The ID should be exchanged through command messages between the clients. The streaming interface does not provide any service for this.
	 * The method blocks until the data has completely arrived at the other client.
	 * @param stream The data to be send.
	 * @param id The id used to identify this particular stream.
	 * @throws IOException If an IO-Error occurs during sending (e.g. the network goes down).
	 * @throws InterruptedException If the blocking is interrupted.
	 */
	public void send(InputStream stream, int id) throws IOException, InterruptedException
	{
		this.send(stream, id, NetInputStream.LENGTH_UNKNOWN);
	}

	/**
	 * Just like {@link #send(InputStream, int)}, but the length of the stream is known. This information can be useful for the other client e.g. to the user a progress or
	 * download bar.
	 * @param stream The stream to be send.
	 * @param id The id to identify the stream.
	 * @param length The length of the stream. Can be {@link NetInputStream#LENGTH_UNKNOWN}.
	 * @throws IOException If an IO-Error occurs during sending (e.g. the network goes down).
	 * @throws InterruptedException If the blocking is interrupted.
	 */
	public void send(InputStream stream, int id, int length) throws IOException, InterruptedException
	{
		OutgoingInputStream oin = new OutgoingInputStream(id, stream, length);
		oin.run();
	}

	/**
	 * Use this method to send data to the other client through the {@link OutputStream#write(byte[])} operation. This method returns an outputstream which
	 * the caller can use to directly send the written data to the other client. The stream ending is indicated through closing the outputstream object.
	 * @param id The id of the stream.
	 * @return An outputstream as a pipe to the other client.
	 * @throws IOException If the network breaks.
	 */
	public OutputStream send(int id) throws IOException
	{
		return this.send(id, NetInputStream.LENGTH_UNKNOWN);
	}

	/**
	 * Use this method to send data to the other client through the {@link OutputStream#write(byte[])} operation. This method returns an outputstream which
	 * the caller can use to directly send the written data to the other client. The stream ending is indicated through closing the outputstream object.
	 * @param id The id of the stream.
	 * @return An outputstream as a pipe to the other client.
	 * @throws IOException If the network breaks.
	 */
	public OutputStream send(int id, int length) throws IOException
	{
		this.outgoingStreamLock.lock();
		try
		{
			if (this.outgoingStreams.contains(id))
				throw new IOException("ID " + id + " is already used in this sessionHandler.");

			OutputStream out = new OutgoingStream(id, length);
			this.outgoingStreams.put(id, out);
			return out;
		}
		finally
		{
			this.outgoingStreamLock.unlock();
		}

	}

	/**
	 * Use this method to send data to the other client in a non-blocking way. This method immediately returns and uses an internal thread to read and send the data
	 * to the other client.
	 * @param stream The data to be send.
	 * @param id The id of the stream.
	 * @param length The length of the stream. May be {@link NetInputStream#LENGTH_UNKNOWN}.
	 * @param callback A callback listener which is called when the streaming has finished. May be <code>null</code>.
	 * @throws IOException If the network breaks.
	 */
	public void sendAsync(InputStream stream, int id, int length, IListener callback) throws IOException
	{
		OutgoingInputStream out = null;
		this.outgoingStreamLock.lock();
		try
		{
			if (this.outgoingStreams.contains(id))
				throw new IOException("ID " + id + " is already used in this sessionHandler.");

			out = new OutgoingInputStream(id, new BufferedInputStream(stream), length, callback);

			this.outgoingStreams.put(id, stream);
		}
		finally
		{
			this.outgoingStreamLock.unlock();
		}
		this.threadPool.execute(out);
	}
	
	/**
	 * This method simply sends an empty package over the stream with the given id.
	 * This can be useful to keep streams open that would be closed otherwise by a timeout.
	 * 
	 * @param id The id of the stream.
	 * @throws IOException If the network breaks.
	 */
	public void keepAlive(int id) throws IOException
	{
		this.outgoingStreamLock.lock();
		try
		{
			Closeable close = this.outgoingStreams.get(id);
			if (close == null)
				throw new IOException("There is no open stream with ID " + id + ".");

			if (close instanceof OutgoingStream)
			{
				OutgoingStream out = (OutgoingStream) close;
				this.threadPool.execute(out);
			}
		}
		finally
		{
			this.outgoingStreamLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int type()
	{
		return IInterfaceType.SESSION_INTERFACE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(final ISession isession)
	{
		this.buffers = new ReusableMSGBuffers(isession, DEFAULT_STREAMING_PACKAGE_SIZE);
		isession.client().addListener(INetworkAPI.EVENT_STATE_CHANGED, new IListener()
		{
			
			@Override
			public void handleEvent(Event e)
			{
				if (isession.client().state() == INetworkAPI.STATE_CLOSING)
					StreamingInterface.this.closeOutgoingStreams();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(IMessage msg)
	{
		this.incommingStreamLock.lock();
		try
		{
			final int streamID = Converter.getIntBig(msg.body(), OFFSET_STREAM_ID);
			NetInputStream stream = this.incommingStreams.get(streamID);
			if (stream != null)
				stream.addMessage(msg);
			else if (msg.type() == IMessageAPI.MSGTYPE_STREAM_HEADER)
			{
				NetInputStream in = new NetInputStream(msg, this);
				this.incommingStreams.put(streamID, in);
			}
			else
				LOGGER.warn("Warning. Got message for stream no longer available. Stream ID: {}", Integer.valueOf(streamID));
		}
		finally
		{
			this.incommingStreamLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose()
	{
		this.incommingStreamLock.lock();
		try
		{
			for (NetInputStream stream : this.incommingStreams.values(new NetInputStream[this.incommingStreams.size()]))
				Close.close(stream);
			this.incommingStreams.clear();
		}
		finally
		{
			this.incommingStreamLock.unlock();
		}
		this.closeOutgoingStreams();
		this.threadPool.shutdownNow();
	}

	private void closeOutgoingStreams()
	{
		this.outgoingStreamLock.lock();
		try
		{
			for (Closeable stream : this.outgoingStreams.values(new Closeable[this.outgoingStreams.size()]))
			{
				Close.close(stream);
			}
			this.outgoingStreams.clear();
		}
		finally
		{
			this.outgoingStreamLock.unlock();
		}
	}

	void close(NetInputStream netInputStream)
	{
		this.incommingStreamLock.lock();
		try
		{
			this.incommingStreams.remove(netInputStream.id());
		}
		finally
		{
			this.incommingStreamLock.unlock();
		}
	}

	private void close(OutgoingInputStream out)
	{
		this.outgoingStreamLock.lock();
		try
		{
			this.outgoingStreams.remove(out.id);
		}
		finally
		{
			this.outgoingStreamLock.unlock();
		}
		if(out.externalCallback != null)
			out.externalCallback.handleEvent(new Event(this, IMessageAPI.EVENT_STREAM_SEND, Integer.valueOf(out.id)));
	}

	private void close(int streamID)
	{
		this.outgoingStreamLock.lock();
		try
		{
			this.outgoingStreams.remove(streamID);
		}
		finally
		{
			this.outgoingStreamLock.unlock();
		}
	}

}
