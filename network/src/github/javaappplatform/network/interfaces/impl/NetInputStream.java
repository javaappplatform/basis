/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.interfaces.impl;

import github.javaappplatform.network.msg.Converter;
import github.javaappplatform.network.msg.IMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This InputStream-implementation belongs to the StreamingInterface and provides internal methods to process network messages.
 * This implementation extends the normal inputStream with the method {@link #id()} with which the id of the stream can be retrieved.
 * @author funsheep
 */
public class NetInputStream extends InputStream
{


	private final ReentrantLock contentLock = new ReentrantLock();
	private final Condition noMessageCondition = this.contentLock.newCondition();
	protected final ArrayDeque<IMessage> content = new ArrayDeque<>();
	private final StreamingInterface parent;
	protected volatile boolean closed = false;
	private final int id;
	private int pointer = 0;
	protected int progress = 0;
	protected int length = LENGTH_UNKNOWN;
	public static final int LENGTH_UNKNOWN = -1;


	protected NetInputStream(int id, StreamingInterface face)
	{
		this.id = id;
		this.parent = face;
	}

	protected NetInputStream(IMessage m, StreamingInterface face)
	{
		if (m.type() != IMessageAPI.MSGTYPE_STREAM_HEADER)
			throw new RuntimeException("Initial message was not a head message.");
		this.parent = face;
		this.id = Converter.getIntBig(m.body(), 0);
		this.addMessage(m);
	}


	protected void addMessage(IMessage m)
	{
		this.contentLock.lock();
		try
		{
			if (m.type() == IMessageAPI.MSGTYPE_STREAM_HEADER)
			{
				if (this.id != Converter.getIntBig(m.body(), 0))
					throw new RuntimeException("Should never happen!");
				this.length = Converter.getIntBig(m.body(), 4);
			}
			this.content.add(m);
			this.noMessageCondition.signal();
		}
		finally
		{
			this.contentLock.unlock();
		}
	}

	/**
	 * The id of the stream.
	 * @return The id.
	 */
	public int id()
	{
		return this.id;
	}

	/**
	 * Returns the number of read bytes so far.
	 * @return The number of read bytes.
	 */
	public long progress()
	{
		return this.progress;
	}

	/**
	 * Returns the supposed length of the stream in bytes. Note, that this value must not be correct. It is possible, that you can still read from the stream,
	 * though {@link #progress()} > {@link #length()}. To correctly identify the end of the stream, use the {@link InputStream} mechanism.
	 * @return The supposed length or {@value #LENGTH_UNKNOWN} if unknown.
	 */
	public long length()
	{
		return this.length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int available() throws IOException
	{
		return this.closed ? 0 : this.content.size() * StreamingInterface.DEFAULT_STREAMING_PACKAGE_SIZE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException
	{
		this.contentLock.lock();
		try
		{
			while (true)
			{
				while (!this.closed && this.content.isEmpty())
				{
					if (this.noMessageCondition.awaitNanos(TimeUnit.SECONDS.toNanos(30)) <= 0)
						throw new IOException("Timeout on network stream " + this.id());
				}
				
				if (this.content.isEmpty() && this.closed)
					return -1;
				
				final IMessage m = this.content.peek();
				if (m.type() == IMessageAPI.MSGTYPE_STREAM_ERROR)
					throw new IOException("Remote partner signaled an error. Streaming of "+this.id+" did not finish sucessfully.");

				if (m.body().length == (m.type() == IMessageAPI.MSGTYPE_STREAM_HEADER ? 8 : 4))
				{
					this.content.remove();
					if (m.type() == IMessageAPI.MSGTYPE_STREAM_TAIL)
					{
						this.close();
						return -1;
					}
				}
				else
				{
					final int position = this.pointer + (m.type() == IMessageAPI.MSGTYPE_STREAM_HEADER ? 8 : 4);
					if (position+1 == m.body().length)
					{
						this.content.remove();
						this.pointer = 0;
						if (m.type() == IMessageAPI.MSGTYPE_STREAM_TAIL)
							this.close();
					}
					else
						this.pointer++;
					this.progress++;
					return m.body()[position] & 0xff;
				}
			}
		}
		catch (InterruptedException e)
		{
			this.close();
			return -1;
		}
		finally
		{
			this.contentLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte b[], int off, int len) throws IOException
	{
		this.contentLock.lock();
		try
		{
			while (!this.closed && this.content.isEmpty())
				if (this.noMessageCondition.awaitNanos(TimeUnit.SECONDS.toNanos(30)) <= 0)
					throw new IOException("Timeout on network stream " + this.id());
			if (this.content.isEmpty() && this.closed)
				return -1;
			final IMessage m = this.content.peek();
			final int position = this.pointer + (m.type() == IMessageAPI.MSGTYPE_STREAM_HEADER ? 8 : 4);
			int i = 0;
			while (i < len && position+i < m.body().length)
			{
				b[off++] = m.body()[position+i];
				i++;
			}
			if (position+i == m.body().length)
			{
				this.pointer = 0;
				this.content.remove();
				if (m.type() == IMessageAPI.MSGTYPE_STREAM_TAIL)
					this.close();
			}
			else
				this.pointer += i;
			this.progress += i;
			return i;
		}
		catch (InterruptedException e)
		{
			this.close();
			return -1;
		}
		finally
		{
			this.contentLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close()
	{
		if (this.closed)
			return;

		this.closed = true;
		this.contentLock.lock();
		try
		{
			this.noMessageCondition.signalAll();
		}
		finally
		{
			this.contentLock.unlock();
		}
		this.parent.close(this);
	}
	
	public void keepAlive()
	{
		if (this.closed)
			return;
		this.contentLock.lock();
		try
		{
			this.noMessageCondition.signalAll();
		}
		finally
		{
			this.contentLock.unlock();
		}
	}

}
