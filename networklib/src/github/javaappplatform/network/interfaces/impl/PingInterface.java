/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
 */
package github.javaappplatform.network.interfaces.impl;

import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.ISession;
import github.javaappplatform.network.interfaces.IInterfaceType;
import github.javaappplatform.network.interfaces.ISessionInterface;
import github.javaappplatform.network.internal.Message;
import github.javaappplatform.network.msg.Converter;
import github.javaappplatform.network.msg.IMessage;
import gnu.trove.list.array.TLongArrayList;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public class PingInterface implements ISessionInterface
{
	public static final String EXT_ID = "de.d3fact.network.interfaces.impl.PingInterface";

	/**
	 * Time to live. No older values are allowed.
	 */
	private static final long DELTA_TTL = 10 * 60 * 1000;

	/**
	 * Best Before. Older values are allowed, but new ones are being gathered.
	 */
	private static final long DELTA_BBF = 5 * 60 * 1000;

	private static final long NET_TIMEOUT = 2000;

	/**
	 * Number of Ping-Messages that will be used to determine the current server-time.
	 */
	private static final int NUM_SYNC_STEPS = 13;

	/**
	 * Number of values that will be taken for the synced global time. X times the median of all
	 * values will be taken (and removed). The arithmetic mean of those X values will be taken as
	 * resulting value.
	 */
	private static final int MEAN_RANGE = 3;

	private volatile long delta;
	private volatile long delta_ts;

	private Object signal_timeDeltaSynced = new Object();
	private volatile boolean synchronizing = false;
	private TLongArrayList vals = new TLongArrayList(NUM_SYNC_STEPS);
	private long sent = 0;

	private ReentrantLock sendVals = new ReentrantLock();

	private ISession session = null;

	@Override
	public int type()
	{
		return IInterfaceType.SESSION_INTERFACE;
	}

	@Override
	public void execute(IMessage msg)
	{
		if (msg.type() == IMessageAPI.MSGTYPE_PING)
		{
			try
			{
				byte[] data = new byte[8];
				Converter.putLongBig(data, 0, System.currentTimeMillis());

				this.session.asyncSend(IMessageAPI.MSGTYPE_PONG, data);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		} else if (msg.type() == IMessageAPI.MSGTYPE_PONG)
		{
			if (this.synchronizing)
				this.syncStep(msg);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose()
	{
		this.session = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(ISession handler)
	{
		this.session = handler;
	}

	public long getSynchedTimeMillis()
	{
		long age = System.currentTimeMillis() - this.delta_ts;
		
		if (this.session.state() == INetworkAPI.STATE_CONNECTED)
		{
			if (age > DELTA_BBF) // Trigger new synchronization
			{
				if (!this.synchronizing) // not already synchronizing
				{
					this.syncStep(null);
				}
	
				if (age > DELTA_TTL) // Wait for correct delta
				{
					try
					{
						synchronized (this.signal_timeDeltaSynced)
						{
							while (this.synchronizing)
								this.signal_timeDeltaSynced.wait(NET_TIMEOUT);
						}
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		return System.currentTimeMillis() + this.delta;
	}

	private synchronized void syncStep(IMessage msg)
	{
		if (msg != null)
		{
			if (msg instanceof Message)
			{
				Message m = (Message)msg;

				this.sendVals.lock();
				try
				{
					long now = System.currentTimeMillis();
					long delay = (now - this.sent) / 2;
					long serverTime = Converter.getLongBig(m.body(), 0);

					this.vals.add(serverTime + delay - now);
				} finally
				{
					this.sendVals.unlock();
				}
			} else
				return;
		}

		this.synchronizing = true;

		if (this.vals.size() >= NUM_SYNC_STEPS) // sync has finished
		{
			this.vals.sort();

			this.delta = 0;
			for (int i = (NUM_SYNC_STEPS - MEAN_RANGE) / 2; i < (NUM_SYNC_STEPS + MEAN_RANGE) / 2; i++)
				this.delta += this.vals.get(i);
			this.delta /= MEAN_RANGE;

			this.delta_ts = System.currentTimeMillis();
			this.vals.clear();
			this.synchronizing = false;

			synchronized (this.signal_timeDeltaSynced)
			{
				this.signal_timeDeltaSynced.notifyAll();
			}
		} else
		{
			try
			{
				this.sendPing();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void sendPing() throws IOException
	{
		this.sendVals.lock();
		try
		{
			this.sent = System.currentTimeMillis();
			this.session.asyncSend(IMessageAPI.MSGTYPE_PING, new byte[0]);
		} finally
		{
			this.sendVals.unlock();
		}
	}
}
