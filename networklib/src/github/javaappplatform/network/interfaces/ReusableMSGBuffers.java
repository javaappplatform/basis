/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 Hendrik Renken
	
	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the 
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.interfaces;

import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.ISession;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO javadoc
 * @author funsheep
 */
public class ReusableMSGBuffers
{

	private class CallbackWrapper implements IListener
	{

		public IListener callback;
		public byte[] buffer;

		public CallbackWrapper(IListener callback, byte[] buffer)
		{
			this.callback = callback;
			this.buffer = buffer;
		}

		@Override
		public void handleEvent(Event e)
		{
			final IListener _callback = this.callback;
			ReusableMSGBuffers.this.lock.lock();
			try
			{
				if (ReusableMSGBuffers.this.buffers.size() < 4)
					ReusableMSGBuffers.this.buffers.addLast(this.buffer);
				if (ReusableMSGBuffers.this.wrappers.size() < 4)
					ReusableMSGBuffers.this.wrappers.addLast(this);
				this.buffer = null;
				this.callback = null;
			}
			finally
			{
				ReusableMSGBuffers.this.lock.unlock();
			}
			if (_callback != null)
				_callback.handleEvent(e);
		}

	}


	private final ReentrantLock lock;
	private final ISession session;
	private final int size;
	private final Deque<byte[]> buffers = new ArrayDeque<>(4);
	private final Deque<CallbackWrapper> wrappers = new ArrayDeque<>(4);


	public ReusableMSGBuffers(ISession session, int size)
	{
		this(session, size, new ReentrantLock());
	}
	/**
	 *
	 */
	public ReusableMSGBuffers(ISession session, int size, ReentrantLock lock)
	{
		this.session = session;
		this.size = size;
		this.lock = lock;
	}


	public byte[] pullBuffer()
	{
		this.lock.lock();
		try
		{
			if (this.buffers.size() == 0)
				return new byte[this.size];
			return this.buffers.pollFirst();
		}
		finally
		{
			this.lock.unlock();
		}
	}

	private IListener wrap(IListener callback, byte[] buffer)
	{
		this.lock.lock();
		try
		{
			if (this.wrappers.size() == 0)
				return new CallbackWrapper(callback, buffer);
			CallbackWrapper wrap = this.wrappers.pollFirst();
			wrap.callback = callback;
			wrap.buffer = buffer;
			return wrap;
		}
		finally
		{
			this.lock.unlock();
		}
	}

	public void asyncSendMSGTCP(int msgType, byte[] buffer) throws IOException
	{
		this.asyncSendMSGTCP(msgType, buffer, 0, buffer.length, null);
	}

	public void asyncSendMSGTCP(int msgType, byte[] buffer, int offset, int length, IListener callback) throws IOException
	{
		if (buffer.length != this.size)
			throw new IllegalArgumentException("Given buffer was not pulled from this reusable buffer instance.");
		this.session.asyncSend(msgType, buffer, offset, length, this.wrap(callback, buffer), INetworkAPI.RELIABLE_PROTOCOL);
	}

}
