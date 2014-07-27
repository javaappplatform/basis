/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.internal;

import github.javaappplatform.commons.collection.SemiDynamicByteArray;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.network.IClientUnit;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.internal.events.SyncedTalkerStub;
import github.javaappplatform.network.msg.IMessage;

import java.io.IOException;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO javadoc
 * @author funsheep
 */
public class Session extends SyncedTalkerStub implements IInternalSession
{

	private static final Logger LOGGER = Logger.getLogger();

	private final ReentrantLock receivedLock = new ReentrantLock();
	private final PriorityQueue<Message> received = new PriorityQueue<>();

	private final ReentrantLock sendLock = new ReentrantLock();
	private long sendMSGID = Long.MIN_VALUE;

	private final AClientUnit unit;
	private final int sessionID;
	private AtomicInteger state = new AtomicInteger(INetworkAPI.STATE_CONNECTED);

	private Object attachment;


	/**
	 *
	 */
	public Session(int sessionID, AClientUnit unit)
	{
		this.unit = unit;
		this.sessionID = sessionID;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int sessionID()
	{
		return this.sessionID;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void asyncSend(int type, byte[] data) throws IOException
	{
		this.asyncSend(type, data, 0, data.length, null, INetworkAPI.RELIABLE_PROTOCOL);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void asyncSend(int type, byte[] data, int off, int len, IListener callback, int protocol) throws IOException
	{
		if (type < 0)
			throw new IllegalArgumentException("Messages with types lower than null are reserved for internal purposes.");
		if (this.state.get() == INetworkAPI.STATE_NOT_CONNECTED)
			throw new IOException("Session is closed.");
		final int _state = this.unit.state();
		if (_state == INetworkAPI.STATE_NOT_CONNECTED)
			throw new IOException("Cannot send data. Client no longer connected.");
		this.sendLock.lock();

		try
		{
			boolean success = false;
			if (protocol == INetworkAPI.FAST_UNRELIABLE_PROTOCOL)
				success = this.unit.sendFast(Message.send(this.unit.clientID(), this.sessionID(), this.sendMSGID, type, data, off, len, callback));
			if (!success)
				this.unit.sendReliable(Message.send(this.unit.clientID(), this.sessionID(), ++this.sendMSGID, type, data, off, len, callback));
		}
		finally
		{
			this.sendLock.unlock();
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void asyncSend(int type, SemiDynamicByteArray data) throws IOException
	{
		this.asyncSend(type, data, data.size() - data.cursor(), null, INetworkAPI.RELIABLE_PROTOCOL);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void asyncSend(int type, SemiDynamicByteArray data, int len, IListener callback, int protocol) throws IOException
	{
		if (this.state.get() != INetworkAPI.STATE_CONNECTED)
			throw new IOException("Session is closed.");
		final int _state = this.unit.state();
		if (_state == INetworkAPI.STATE_NOT_CONNECTED)
			throw new IOException("Cannot send data. Client no longer connected.");
		this.sendLock.lock();
		try
		{
			boolean success = false;
			if (protocol == INetworkAPI.FAST_UNRELIABLE_PROTOCOL)
				success = this.unit.sendFast(Message.send(this.unit.clientID(), this.sessionID(), this.sendMSGID, type, data, len, callback));
			if (!success)
				this.unit.sendReliable(Message.send(this.unit.clientID(), this.sessionID(), ++this.sendMSGID, type, data, len, callback));
		}
		finally
		{
			this.sendLock.unlock();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasReceivedMSGs()
	{
		this.receivedLock.lock();
		try
		{
			return !this.received.isEmpty();
		}
		finally
		{
			this.receivedLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IMessage receiveMSG()
	{
		this.receivedLock.lock();
		try
		{
			return this.received.poll();
		}
		finally
		{
			this.receivedLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void received(Message msg)
	{
		boolean postReceive = false;
		this.receivedLock.lock();
		try
		{
			if (msg.type() > 0)
			{
				msg._setSession(this.sessionID());
				this.received.add(msg);
				postReceive = true;
				assert LOGGER.trace("Received msg on session {} of type {}", Integer.valueOf(this.sessionID()), Integer.valueOf(msg.type()));
			}
			else
				msg.dispose();
		}
		finally
		{
			this.receivedLock.unlock();
		}
		if (postReceive)
			this.postEvent(INetworkAPI.EVENT_MSG_RECEIVED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int state()
	{
		return this.state.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close()
	{
		if (!this.state.compareAndSet(INetworkAPI.STATE_CONNECTED, INetworkAPI.STATE_CLOSING))
			return;

		try
		{
			this.unit.closeSession(this);
			this.postEvent(INetworkAPI.EVENT_STATE_CHANGED);
		}
		finally
		{
			this.shutdown();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		Close.close(this);
		if (this.state.getAndSet(INetworkAPI.STATE_NOT_CONNECTED) != INetworkAPI.STATE_NOT_CONNECTED)
		{
			this.unit.removeSession(this);
			this.postEvent(INetworkAPI.EVENT_STATE_CHANGED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IClientUnit client()
	{
		return this.unit;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object attach(Object _attachment)
	{
		final Object old = this.attachment;
		this.attachment = _attachment;
		return old;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object attachment()
	{
		return this.attachment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Session ");
		sb.append(this.sessionID);
		sb.append(" State: ");
		switch (this.state())
		{
			case INetworkAPI.STATE_NOT_CONNECTED:
				sb.append("Not Connected");
				break;
			case INetworkAPI.STATE_CONNECTED:
				sb.append("Connected");
				break;
			case INetworkAPI.STATE_CLOSING:
				sb.append("Closing");
				break;
		}
		return sb.toString();
	}

}
