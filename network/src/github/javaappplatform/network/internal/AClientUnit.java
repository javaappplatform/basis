/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.internal;

import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.events.SyncedTalkerStub;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.commons.util.Collections2;
import github.javaappplatform.network.IClientUnit;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.ISession;
import github.javaappplatform.network.msg.Converter;
import github.javaappplatform.network.msg.MessageReader;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This works as follows:
 * 1) If TCP MSG Connection down -> Client is disconnected
 * 2) If UDP MSG Connection down -> Send it through TCP
 * 3) If StreamingPool empty (no more streaming possible) -> Client is disconnected
 * @author funsheep
 */
public abstract class AClientUnit extends SyncedTalkerStub implements IClientUnit
{

	protected static final Logger LOGGER = Logger.getLogger();

	/************ Usual stuff ***************/
	private int clientID;
	private final byte type;

	/************ Session ***************/
	private final ReentrantLock sessionLock = new ReentrantLock();
	private final Condition newSessionAvailable = this.sessionLock.newCondition();
	private final TIntObjectMap<Session> _sessionMap = new TIntObjectHashMap<>(6);
	private final TIntHashSet _openSessionRequests = new TIntHashSet(6);
	

	protected AClientUnit(byte type)
	{
		this.type = type;
	}


	protected void set(int clientID)
	{
		this.clientID = clientID;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte type()
	{
		return this.type;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int clientID()
	{
		return this.clientID;
	}

	/************ Session ***************/
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISession startSession()
	{
		if (this.state() != INetworkAPI.STATE_CONNECTED)
			throw new IllegalStateException("Client not connected.");

		int sessionID = 0;
		this.sessionLock.lock();
		try
		{

			while (sessionID == 0 || this._openSessionRequests.contains(sessionID) || this._sessionMap.containsKey(sessionID))
				sessionID = InternalNetTools.newSessionID();

			this.sendSession0Message(InternalMessageAPI.MSGTYPE_START_NEW_SESSION, sessionID);

			this._openSessionRequests.add(sessionID);
			while (this._openSessionRequests.contains(sessionID))
				if (!this.newSessionAvailable.await(InternalNetTools.TIMEOUT, TimeUnit.MILLISECONDS))
				{
					this._openSessionRequests.remove(sessionID);
					return null;	//Timeout - probably network down?
				}


			Session newSession = new Session(sessionID, this);
			this._sessionMap.put(sessionID, newSession);
			return newSession;
		}
		catch (InterruptedException e)
		{
			this._openSessionRequests.remove(sessionID);
			return null;
		}
		finally
		{
			this.sessionLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISession getSession(int sessionID)
	{
		this.sessionLock.lock();
		try
		{
			return this._sessionMap.get(sessionID);
		}
		finally
		{
			this.sessionLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<ISession> getAllSessions()
	{
		this.sessionLock.lock();
		try
		{
			return Collections2.wrapArray(this._sessionMap.values());
		}
		finally
		{
			this.sessionLock.unlock();
		}
	}

	public void distributeReceivedMSG(Message msg)
	{
		Event event = null;
		this.sessionLock.lock();
		try
		{
			if (msg.session() == 0)
				event = this.handleSession0Messages(msg);
			else
			{
				final IInternalSession ses = this._sessionMap.get(msg.session());
				if (ses != null)
				{
					ses.received(msg);
					return;
				}
			}
		}
		finally
		{
			this.sessionLock.unlock();
		}

		if (event != null)
			this.postEvent(event.type(), event.getData());
	}
	
	private final MessageReader session0Reader = new MessageReader();
	private Event handleSession0Messages(Message msg)
	{
		assert (msg.session() == 0);
		this.session0Reader.reset(msg);
		final int sessionID = this.session0Reader.readInt();
		switch (msg.type())
		{
			case InternalMessageAPI.MSGTYPE_START_NEW_SESSION:
				if (sessionID == 0 || this._openSessionRequests.contains(sessionID) || this.getSession(sessionID) != null)
				{
					this.sendSession0Message(InternalMessageAPI.MSGTYPE_ERROR_NEW_SESSION, sessionID);
					break;
				}
				
				Session ses = new Session(sessionID, this);
				this._sessionMap.put(sessionID, ses);
				this.sendSession0Message(InternalMessageAPI.MSGTYPE_ACK_NEW_SESSION, sessionID);
				return new Event(null, INetworkAPI.EVENT_SESSION_STARTED, Integer.valueOf(sessionID));
			case InternalMessageAPI.MSGTYPE_ACK_NEW_SESSION:
				if (this._openSessionRequests.remove(sessionID))
					this.newSessionAvailable.signalAll();
				break;
			case InternalMessageAPI.MSGTYPE_ERROR_NEW_SESSION:
				this._openSessionRequests.remove(sessionID);
				this._sessionMap.remove(sessionID);
				this.newSessionAvailable.signalAll();
				break;
			case InternalMessageAPI.MSGTYPE_CLOSE_SESSION:
				this._openSessionRequests.remove(sessionID);
				final Session s = this._sessionMap.remove(sessionID);
				if (s != null)
					Close.close(s);
				
				this.sendSession0Message(InternalMessageAPI.MSGTYPE_ACK_CLOSE_SESSION, sessionID);
				this.newSessionAvailable.signalAll();
				break;
		}
		return null;
	}
	
	protected void closeSession(Session session)
	{
		if (session.state() != INetworkAPI.STATE_CLOSING)
			throw new IllegalStateException("Should only be called on a purposely closed session.");

		boolean send = false;
		this.sessionLock.lock();
		try
		{
			send = this._sessionMap.containsKey(session.sessionID());
		}
		finally
		{
			this.sessionLock.unlock();
		}
		
		if (send)
			this.sendSession0Message(InternalMessageAPI.MSGTYPE_CLOSE_SESSION, session.sessionID());
	}
	
	protected void removeSession(Session session)
	{
		if (session.state() != INetworkAPI.STATE_NOT_CONNECTED)
			throw new IllegalStateException("Should only be called on a purposely shutdown session.");

		this.sessionLock.lock();
		try
		{
			this._sessionMap.remove(session.sessionID());
		}
		finally
		{
			this.sessionLock.unlock();
		}
	}
	
	protected void closeAllSessions()
	{
		this.sessionLock.lock();
		try
		{

			for (Object ses : this._sessionMap.values())
				Close.close((Closeable) ses);

			this._sessionMap.clear();
		}
		finally
		{
			this.sessionLock.unlock();
		}
	}

	/********* send messages ************/
	protected void sendSession0Message(int msgType, int sessionID)
	{
		byte[] _sessionID = new byte[4];
		Converter.putIntBig(_sessionID, 0, sessionID);
		try
		{
			this.sendSystem(Message.send(this.clientID(), 0, 0, msgType, _sessionID));
		}
		catch (IOException ex)
		{
			LOGGER.info("Client Unit ["+this.clientID()+"] went down. Connection Timeout.");
			this.shutdown();
		}
	}
	
	protected abstract void sendSystem(Message msg) throws IOException;

	protected abstract void sendReliable(Message msg) throws IOException;

	public boolean sendFast(Message msg)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		this.sessionLock.lock();
		try
		{
			sb.append("Sessions: " + this._sessionMap.size());
			for (ISession ses : this._sessionMap.valueCollection())
			{
				sb.append('\n');
				sb.append(String.valueOf(ses));
			}
		}
		finally
		{
			this.sessionLock.unlock();
		}
		return sb.toString();
	}

}
