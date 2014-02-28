/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.server.tcp;

import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.internal.AClientUnit;
import github.javaappplatform.network.internal.Message;
import github.javaappplatform.network.internal.tcp.TCPConnection;
import github.javaappplatform.network.server.IRemoteClientUnit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * This works like the following:
 * 1) If TCP MSG Connection down -> Client is disconnected
 * 2) If UDP MSG Connection down -> Send it through TCP
 * 3) If StreamingPool empty (no more streaming possible) -> Client is disconnected
 * @author funsheep
 */
public class TCPRemoteClientUnit extends AClientUnit implements IRemoteClientUnit
{

	private final UDPHandler udp;
	private final InetSocketAddress remoteUDP;
	private final TCPConnection con;
	private final AtomicInteger state = new AtomicInteger(INetworkAPI.STATE_NOT_CONNECTED);


	public TCPRemoteClientUnit(int clientID, byte type, Socket socket, int remoteUDPPort, UDPHandler handler)
	{
		super(type);
		this.set(clientID);
		this.udp = handler;
		this.con = new TCPConnection(socket, this)
		{
			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void validateState()
			{
				if (TCPRemoteClientUnit.this.state.get() == INetworkAPI.STATE_CONNECTED && this.state() == INetworkAPI.STATE_NOT_CONNECTED)
				{
					TCPRemoteClientUnit.this.shutdown();
				}
			}
		};

		if (remoteUDPPort > 0)
			this.remoteUDP = new InetSocketAddress(socket.getInetAddress().getHostAddress(), remoteUDPPort);
		else
			this.remoteUDP = null;

		this.state.set(this.con.state());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public InetSocketAddress address(int protocol)
	{
		if (protocol == INetworkAPI.RELIABLE_PROTOCOL)
			return this.con.address();
		return this.remoteUDP;
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
	protected void sendSystem(Message msg) throws IOException
	{
		this.con.sendSystem(msg);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendReliable(Message msg) throws IOException
	{
		if (this.state() != INetworkAPI.STATE_CONNECTED)
			throw new IOException("Client not connected.");
		this.con.send(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean sendFast(Message msg)
	{
		if (this.state() != INetworkAPI.STATE_CONNECTED || this.remoteUDP == null || this.udp == null || this.udp.isClosed())
			return false;
		this.udp.send(this.remoteUDP, msg);
		return true;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close()
	{
		if (!this.state.compareAndSet(INetworkAPI.STATE_CONNECTED, INetworkAPI.STATE_CLOSING))
			return;

		this.postEvent(INetworkAPI.EVENT_STATE_CHANGED);
		
		this.udp.shutdown();
		this.closeAllSessions();
		this.con.close();
		this.con.waitUntilClosed();
		
		this.state.set(this.con.state());
		this.postEvent(INetworkAPI.EVENT_STATE_CHANGED);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		if (!this.state.compareAndSet(INetworkAPI.STATE_CONNECTED, INetworkAPI.STATE_CLOSING))
			return;
		this.postEvent(INetworkAPI.EVENT_STATE_CHANGED);

		this.closeAllSessions();
		this.con.shutdown();
		this.udp.shutdown();

		this.state.set(this.con.state());
		this.postEvent(INetworkAPI.EVENT_STATE_CHANGED);
		if (this.state.get() != INetworkAPI.STATE_NOT_CONNECTED)
			throw new IllegalStateException("Could not propably shutdown connection");
	}

}
