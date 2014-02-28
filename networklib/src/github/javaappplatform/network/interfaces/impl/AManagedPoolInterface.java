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
import github.javaappplatform.commons.events.ITalker;
import github.javaappplatform.commons.json.JSONWriter;
import github.javaappplatform.commons.util.StringID;
import github.javaappplatform.network.ISession;
import github.javaappplatform.network.interfaces.IInterfaceType;
import github.javaappplatform.network.interfaces.ISessionInterface;
import github.javaappplatform.network.msg.Converter;
import github.javaappplatform.network.msg.IMessage;
import github.javaappplatform.network.msg.MessageReader;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashSet;

public abstract class AManagedPoolInterface implements ISessionInterface, IListener
{
	private HashSet<IPool<?>> pools = new HashSet<IPool<?>>();
	private ISession session;

	@Override
	public int type()
	{
		return IInterfaceType.SESSION_INTERFACE;
	}

	@Override
	public void init(ISession ses)
	{
		this.session = ses;
	}

	@Override
	public void execute(IMessage msg)
	{
		MessageReader read = new MessageReader(msg);
		switch(msg.type())
		{
			case IMessageAPI.MSGTYPE_POOL_GET_CONTENT:
			{
				String poolid = read.readString();
				boolean updates = read.readBoolean();

				IPool<?> pool = this.getPool(poolid);

				if(pool == null)
				{
					try
					{
						this.sendErrorMsg(IMessageAPI.MSGTYPE_POOL_GET_CONTENT_ERROR, "No pool found.");
					} catch (IOException e)
					{
						e.printStackTrace();
					}
					break;
				}

				try
				{
					this.sendUpdate(pool, IMessageAPI.MSGTYPE_POOL_GET_CONTENT_RESPONSE, pool.getContent().toArray());
				} catch (IOException e)
				{
					// TODO error ?
					e.printStackTrace();
				}

				if(updates)
				{
					// add listener
					this.pools.add(pool);
					pool.addListener(IPool.EVENT_NEW_OBJECT, this);
					pool.addListener(IPool.EVENT_REMOVED_OBJECT, this);
				}
			}
			break;

			case IMessageAPI.MSGTYPE_POOL_REGISTER_FOR_UPDATES:
			{
				String poolid = read.readString();

				IPool<?> pool = this.getPool(poolid);

				if(pool == null)
				{
					try
					{
						this.sendErrorMsg(IMessageAPI.MSGTYPE_POOL_GET_CONTENT_ERROR, "No pool found.");
					} catch (IOException e)
					{
						e.printStackTrace();
					}
					break;
				}

				this.pools.add(pool);
				pool.addListener(IPool.EVENT_NEW_OBJECT, this);
				pool.addListener(IPool.EVENT_REMOVED_OBJECT, this);
			}
			break;

			case IMessageAPI.MSGTYPE_POOL_UNREGISTER_FROM_UPDATES:
			{
				String poolid = read.readString();

				IPool<?> pool = this.getPool(poolid);

				if(pool == null)
				{
					try
					{
						this.sendErrorMsg(IMessageAPI.MSGTYPE_POOL_GET_CONTENT_ERROR, "No pool found.");
					} catch (IOException e)
					{
						e.printStackTrace();
					}
					break;
				}

				this.pools.remove(pool);
				pool.removeListener(IPool.EVENT_NEW_OBJECT, this);
				pool.removeListener(IPool.EVENT_REMOVED_OBJECT, this);
			}
			break;
		}
	}

	@Override
	public void handleEvent(Event e)
	{
		if(e.getSource() instanceof IPool)
		{
			IPool<?> pool = e.getSource();
			Object object = e.getData();

			if(e.type() == IPool.EVENT_NEW_OBJECT)
			{
				try
				{
					this.sendUpdate(pool, IMessageAPI.MSGTYPE_POOL_NEW_OBJECTS, object);
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
			else if(e.type() == IPool.EVENT_REMOVED_OBJECT)
			{
				try
				{
					this.sendUpdate(pool, IMessageAPI.MSGTYPE_POOL_REMOVED_OBJECTS, object);
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
	}

	private void sendUpdate(IPool<?> pool, int msgType, Object... objects) throws IOException
	{
		StringWriter sw = new StringWriter();
		JSONWriter jw = new JSONWriter(sw);

		jw.startObject();

		jw.writeField("pool", pool.getID());

		jw.startArrayField("objects");

		for(Object o : objects)
			pool.toJSON(o, jw);

		jw.endArray();

		jw.endObject();
		jw.flush();
		jw.close();

		String json = sw.toString();

		byte[] data = new byte[json.length() + 4];
		Converter.putString(data, 0, json, ByteOrder.BIG_ENDIAN);

		this.session.asyncSend(msgType, data);
	}

	private void sendErrorMsg(int msgType, String description) throws IOException
	{
		byte[] data = new byte[description.length() + 4];
		Converter.putString(data, 0, description, ByteOrder.BIG_ENDIAN);

		this.session.asyncSend(msgType, data);
	}

	@Override
	public void dispose()
	{
		for(IPool<?> pool : this.pools)
			pool.removeListener(this);

		this.session = null;
	}


	public abstract IPool<?> getPool(String id);


	public static interface IPool<T> extends ITalker
	{
		public static final int EVENT_NEW_OBJECT = StringID.id("EVENT_NEW_OBJECTS");
		public static final int EVENT_REMOVED_OBJECT = StringID.id("EVENT_REMOVED_OBJECTS");


		public String getID();

		public Collection<T> getContent();

		public void toJSON(Object o, JSONWriter writer) throws IOException;
	}

}
