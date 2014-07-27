/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.server;

import github.javaappplatform.commons.collection.SmallSet;
import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.internal.IInternalServer;
import github.javaappplatform.network.internal.IInternalServerUnit;
import github.javaappplatform.network.internal.InternalNetTools;
import github.javaappplatform.network.internal.events.SyncedTalkerStub;
import gnu.trove.iterator.TIntLongIterator;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


/**
 * TODO javadoc
 * @author funsheep
 */
public class Server extends SyncedTalkerStub implements IInternalServer
{

//	private static final Logger LOGGER = Logger.getLogger();

	private final SmallSet<IInternalServerUnit> units = new SmallSet<>();
	private final ReentrantLock clientLock = new ReentrantLock();
	private final TIntObjectHashMap<IRemoteClientUnit> _clientUnits = new TIntObjectHashMap<>(1);
	private final AtomicInteger state = new AtomicInteger(INetworkAPI.STATE_NOT_STARTED);
	private final TIntLongMap reservedIDs = new TIntLongHashMap();

	
	public Server(IInternalServerUnit... units)
	{
		this.units.addAll(units);
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
	public void start() throws IOException
	{
		if (!this.state.compareAndSet(INetworkAPI.STATE_NOT_STARTED, INetworkAPI.STATE_STARTING))
			return;
		this.postEvent(INetworkAPI.EVENT_STATE_CHANGED);
		
		for (IInternalServerUnit unit : this.units)
			unit.start(this);
		
		this.state.set(INetworkAPI.STATE_RUNNING);
		this.postEvent(INetworkAPI.EVENT_STATE_CHANGED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEvent(Event e)
	{
		if (e.getSource() instanceof IRemoteClientUnit)
		{
			final IRemoteClientUnit unit = e.getSource();
			if (unit.state() == INetworkAPI.STATE_NOT_CONNECTED)
			{
				this.clientLock.lock();
				try
				{
					unit.removeListener(this);
					this._clientUnits.remove(unit.clientID());
				}
				finally
				{
					this.clientLock.unlock();
				}
			}
		}
		else if (this.state.get() == INetworkAPI.STATE_RUNNING) //one of the units shut down during runtime
		{
			this.shutdown();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close()
	{
		if (!this.state.compareAndSet(INetworkAPI.STATE_RUNNING, INetworkAPI.STATE_CLOSING))
			return;

		for (IInternalServerUnit unit : this.units)
			unit.shutdown();
		this.units.clear();

		this.clientLock.lock();
		try
		{
			for (IRemoteClientUnit unit : this._clientUnits.values(new IRemoteClientUnit[this._clientUnits.size()]))
				Close.close(unit);
		}
		finally
		{
			this.clientLock.unlock();
		}
		this.state.set(INetworkAPI.STATE_SHUTDOWN);
		this.postEvent(INetworkAPI.EVENT_STATE_CHANGED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		if (this.state.getAndSet(INetworkAPI.STATE_SHUTDOWN) == INetworkAPI.STATE_SHUTDOWN)
			return;
		
		for (IInternalServerUnit unit : this.units)
			unit.shutdown();
		this.units.clear();

		this.clientLock.lock();
		try
		{
			for (IRemoteClientUnit unit : this._clientUnits.values(new IRemoteClientUnit[this._clientUnits.size()]))
				unit.shutdown();
		}
		finally
		{
			this.clientLock.unlock();
		}
		this.postEvent(INetworkAPI.EVENT_STATE_CHANGED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IRemoteClientUnit getClient(int id)
	{
		this.clientLock.lock();
		try
		{
			return this._clientUnits.get(id);
		}
		finally
		{
			this.clientLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<IRemoteClientUnit> getAllClients()
	{
		this.clientLock.lock();
		try
		{
			return Collections.unmodifiableCollection(this._clientUnits.valueCollection());
		}
		finally
		{
			this.clientLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(IRemoteClientUnit unit)
	{
		this.clientLock.lock();
		try
		{
			this.cleanupReserved();
			if (unit.clientID() == 0)
				throw new IllegalArgumentException("Given client " + unit + " does not have a client ID.");
			if (this.reservedIDs.remove(unit.clientID()) == 0)
				throw new IllegalArgumentException("Given client " + unit + " has an ID that was not previously reserved. Probably an IServerUnit implemented in the wrong way.");
			if (unit.state() == INetworkAPI.STATE_NOT_CONNECTED)
				return;
			unit.addListener(INetworkAPI.EVENT_STATE_CHANGED, this);
			this._clientUnits.put(unit.clientID(), unit);
		}
		finally
		{
			this.clientLock.unlock();
		}
		this.postEvent(INetworkAPI.EVENT_CLIENT_CONNECTED, Integer.valueOf(unit.clientID()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int reserveClientID()
	{
		this.clientLock.lock();
		try
		{
			int cID = 0;
			while (cID == 0 || this._clientUnits.contains(cID) || this.reservedIDs.containsKey(cID))
			{
				cID = InternalNetTools.newClientID();
			}
			this.reservedIDs.put(cID, System.currentTimeMillis());
			return cID;
		}
		finally
		{
			this.clientLock.unlock();
		}
	}

	
	private void cleanupReserved()
	{
		Server.this.clientLock.lock();
		try
		{
			final long time = System.currentTimeMillis();
			TIntLongIterator iter = Server.this.reservedIDs.iterator();
			while (iter.hasNext())
			{
				iter.advance();
				if (iter.value() + INetworkAPI.CONNECTION_TIMEOUT < time)
					iter.remove();
			}
		}
		finally
		{
			Server.this.clientLock.unlock();
		}
	}

}
