/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.io;

import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.commons.util.StringID;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Tool to pipe an inputstream into an outputstream through various methods.
 * @author funsheep
 */
public class InPipeOut
{

	/** Event that indicates that the pipe process finished successfully. The data and source fields are empty! */
	public static final int EVENT_PIPE_OK = StringID.id("EVENT_PIPE_OK");

	/**
	 * Event that indicates that the pipe process failed with an exception. The source field is empty!
	 * The data field contains the exception: Either an IOException or an {@link InterruptedException}.
	 */
	public static final int EVENT_PIPE_ERROR = StringID.id("EVENT_PIPE_ERROR");

	/**
	 * Pipes the inputstream content into the outputstream content. The outputstream is closed after completion!
	 * This method directly executes the piping. Blocks until finishing.
	 * @param in The inputstream.
	 * @param out the outputsteam.
	 * @throws IOException Is thrown if something goes wrong.
	 * @throws InterruptedException Is thrown if the executing thread is interrupted.
	 */
	public static final void pipe(InputStream in, OutputStream out) throws IOException, InterruptedException
	{
		pipe(in, out, false);
	}

	/**
	 * Pipes the inputstream content into the outputstream content. The outputstream is closed after completion!
	 * This method directly executes the piping. Blocks until finishing.
	 * @param in The inputstream.
	 * @param out the outputsteam.
	 * @param autoFlush A boolean; if true, the output buffer will be flushed after every <tt>read</tt> operation.
	 * @throws IOException Is thrown if something goes wrong.
	 * @throws InterruptedException Is thrown if the executing thread is interrupted.
	 */
	public static final void pipe(InputStream in, OutputStream out, boolean autoFlush) throws IOException, InterruptedException
	{
		byte[] buffer = new byte[8096];
		int len;
		try
		{
			while ((len = in.read(buffer)) != -1)
			{
				out.write(buffer, 0, len);
				if(autoFlush)
					out.flush();
				if (Thread.interrupted())
					throw new InterruptedException();
			}
		}
		finally
		{
			out.close();
		}
	}

	/**
	 * Does the same as {@link #pipe(InputStream, OutputStream)} but the pipe process is executed in a separate thread. If a listener is provided,
	 * the listener is informed if the process finishes successfully and if it fails (see {@link #EVENT_PIPE_OK} and {@link #EVENT_PIPE_ERROR}).
	 * @param in The inputstream.
	 * @param out The outputstream.
	 * @param listener The listener. May be <code>null</code>.
	 */
	public static final void run(final InputStream in, final OutputStream out, final IListener listener)
	{
		run(in, out, listener, false);
	}

	/**
	 * Does the same as {@link #pipe(InputStream, OutputStream)} but the pipe process is executed in a separate thread. If a listener is provided,
	 * the listener is informed if the process finishes successfully and if it fails (see {@link #EVENT_PIPE_OK} and {@link #EVENT_PIPE_ERROR}).
	 * @param in The inputstream.
	 * @param out The outputstream.
	 * @param listener The listener. May be <code>null</code>.
	 * @param autoFlush A boolean; if true, the output buffer will be flushed after every <tt>read</tt> operation.
	 */
	public static final void run(final InputStream in, final OutputStream out, final IListener listener, final boolean autoFlush)
	{
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					InPipeOut.pipe(in, out, autoFlush);
					if (listener != null)
						listener.handleEvent(new Event(null, EVENT_PIPE_OK));
				}
				catch (Exception e)
				{
					if (listener != null)
						listener.handleEvent(new Event(null, EVENT_PIPE_ERROR, e));
				}
			}
		};
		thread.start();
	}

	private static final class BlockingListener implements IListener
	{

		public final ReentrantLock lock = new ReentrantLock();
		public final Condition finished = this.lock.newCondition();
		public Exception cause;


		@Override
		public void handleEvent(Event e)
		{
			this.lock.lock();
			try
			{
				if (e.type() == EVENT_PIPE_ERROR)
				{
					this.cause = (Exception) e.getData();
				}
				this.finished.signal();
			}
			finally
			{
				this.lock.unlock();
			}
		}
	}

	/**
	 * Does the same as {@link #pipe(InputStream, OutputStream)} and {@link #run(InputStream, OutputStream, IListener)}. The piping process is executed in a separate
	 * thread but the method blocks until finished.
	 * @param in The inputstream.
	 * @param out The outputstream.
	 * @throws IOException Is thrown if something goes wrong.
	 * @throws InterruptedException Is thrown if the executing thread is interrupted.
	 */
	public static final void pipeAndRun(InputStream in, OutputStream out) throws IOException, InterruptedException
	{
		pipeAndRun(in, out, false);
	}

	/**
	 * Does the same as {@link #pipe(InputStream, OutputStream)} and {@link #run(InputStream, OutputStream, IListener)}. The piping process is executed in a separate
	 * thread but the method blocks until finished.
	 * @param in The inputstream.
	 * @param out The outputstream.
	 * @param autoFlush A boolean; if true, the output buffer will be flushed after every <tt>read</tt> operation.
	 * @throws IOException Is thrown if something goes wrong.
	 * @throws InterruptedException Is thrown if the executing thread is interrupted.
	 */
	public static final void pipeAndRun(InputStream in, OutputStream out, boolean autoFlush) throws IOException, InterruptedException
	{
		BlockingListener listener = new BlockingListener();
		listener.lock.lock();
		try
		{
			run(in, out, listener, autoFlush);
			listener.finished.await();
		}
		finally
		{
			listener.lock.unlock();
		}
		if (listener.cause != null)
		{
			if (listener.cause instanceof IOException)
				throw (IOException) listener.cause;
			throw (InterruptedException) listener.cause;
		}
	}


	private InPipeOut()
	{
		//no instance
	}

}
