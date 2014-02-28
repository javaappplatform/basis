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
import github.javaappplatform.commons.util.Strings;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.msg.IMessage;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * TODO javadoc
 * @author funsheep
 */
public class Message implements IMessage, Comparable<Message>
{

	public static final int BODYTYPE_BYTEARRAY = 0;
	public static final int BODYTYPE_SEMIARRAY = 1;


	private static final LinkedBlockingDeque<Message> CACHE = new LinkedBlockingDeque<>(200);


	private int clientID;
	private int sessionID;
	private long orderID;
	private int type;
	private Object body;
	private int off;
	private int len;
	private int bodyType;
	private int protocol;

	private IListener callback;
//	private Object source;


	private void set(int clientID, int sessionID, long orderID, int type)
	{
		this.clientID = clientID;
		this.sessionID = sessionID;
		this.orderID = orderID;
		this.type = type;
		this.protocol = INetworkAPI.FAST_UNRELIABLE_PROTOCOL;
		this.callback = null;
	}

	/**
	 *
	 */
	private void set(int sessionID, long orderID, int type)
	{
		this.clientID = 0;
		this.sessionID = sessionID;
		this.orderID = orderID;
		this.type = type;
		this.protocol = INetworkAPI.RELIABLE_PROTOCOL;
	}

	private void set(IListener callback)
	{
		this.callback = callback;
//		this.source = source;
	}

	private void set(byte[] body, int off, int len)
	{
		this.bodyType = BODYTYPE_BYTEARRAY;
		this.body = body;
		this.off = off;
		this.len = len;
	}

	private void set(SemiDynamicByteArray body, int len)
	{
		this.bodyType = BODYTYPE_SEMIARRAY;
		this.body = body;
		this.len = len;
	}


	public int clientID()
	{
		return this.clientID;
	}

	public long orderID()
	{
		return this.orderID;
	}

	public int protocol()
	{
		return this.protocol;
	}

	public IListener callback()
	{
		return this.callback;
	}

//	public Object source()
//	{
//		return this.source;
//	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int type()
	{
		return this.type;
	}


	public int bodyType()
	{
		return this.bodyType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] body()
	{
		return (byte[]) this.body;
	}

	public int off()
	{
		return this.off;
	}

	public int len()
	{
		return this.len;
	}

	public SemiDynamicByteArray body2()
	{
		return (SemiDynamicByteArray) this.body;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int session()
	{
		return this.sessionID;
	}

	void _setSession(int ID)
	{
		this.sessionID = ID;
	}

	public void dispose()
	{
		this.body = null;
		this.callback = null;
//		this.source = null;
		CACHE.offer(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Message o)
	{
		final long t = this.orderID - o.orderID;
		if (t < 0)
			return -1;
		if (t > 0)
			return +1;
		if (this.protocol == INetworkAPI.RELIABLE_PROTOCOL)
			return -1;
		else if (o.protocol == INetworkAPI.RELIABLE_PROTOCOL)
			return +1;
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return "Message["+this.type()+"] '" + Strings.toHexString(this.bodyType() == BODYTYPE_BYTEARRAY ? this.body() : this.body2().getData(), this.off, this.len) + "'";
	}

	private static final Message get()
	{
		final Message m = CACHE.poll();
		if (m != null)
			return m;
		return new Message();
	}


	public static Message send(int clientID, int sessionID, long orderID, int type, byte[] body)
	{
		return send(clientID, sessionID, orderID, type, body, 0, body.length, null);
	}

	public static Message send(int clientID, int sessionID, long orderID, int type, byte[] body, int off, int len, IListener callback)
	{
		final Message m = get();
		m.set(clientID, sessionID, orderID, type);
		m.set(callback);
		m.set(body, off, len);
		return m;
	}

	public static Message send(int clientID, int sessionID, long orderID, int type, SemiDynamicByteArray body, int len, IListener callback)
	{
		final Message m = get();
		m.set(sessionID, orderID, type);
		m.set(callback);
		m.set(body, len);
		return m;
	}


	public static Message receiveTCP(int sessionID, long orderID, int type, byte[] body, int off, int len)
	{
		final Message m = get();
		m.set(sessionID, orderID, type);
		m.set(body, off, len);
		return m;
	}

	public static Message receiveTCP(int sessionID, long orderID, int type, SemiDynamicByteArray body)
	{
		final Message m = get();
		m.set(sessionID, orderID, type);
		m.set(body, body.size());
		return m;
	}


	public static Message receiveUDP(int clientID, int sessionID, long orderID, int type, byte[] body, int off, int len)
	{
		final Message m = get();
		m.set(clientID, sessionID, orderID, type);
		m.set(body, off, len);
		return m;
	}

}
