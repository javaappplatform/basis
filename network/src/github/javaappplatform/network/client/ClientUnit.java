/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.client;

import github.javaappplatform.commons.util.Close;
import github.javaappplatform.commons.util.Strings;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.PortRange;
import github.javaappplatform.network.internal.AClientUnit;
import github.javaappplatform.network.internal.InternalMessageAPI;
import github.javaappplatform.network.internal.InternalNetTools;
import github.javaappplatform.network.internal.Message;
import github.javaappplatform.network.internal.tcp.TCPConnection;
import github.javaappplatform.network.msg.Converter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This works as follows:
 * 1) If TCP MSG Connection down -> Client is disconnected
 * 2) If UDP MSG Connection down -> Send it through TCP
 * 3) If StreamingPool empty (no more streaming possible) -> Client is disconnected
 * @author funsheep
 */
public class ClientUnit extends AClientUnit
{

	private final PortRange localPortRange;
	private final SocketAddress remoteTCP;
	private final SocketAddress remoteUDP;

	private final AtomicInteger state = new AtomicInteger(INetworkAPI.STATE_NOT_CONNECTED);
	
	private TCPConnection con;
	private UDPHandler udp;


	public ClientUnit(String remoteHost, int remoteTCP, int remoteUDP, PortRange localPortRange, byte type)
	{
		this(0, remoteHost, remoteTCP, remoteUDP, localPortRange, type);
	}

	public ClientUnit(int clientID, String remoteHost, int remoteTCP, int remoteUDP, PortRange localPortRange, byte type)
	{
		super(type);
		this.set(clientID);
		this.remoteTCP = new InetSocketAddress(remoteHost, remoteTCP);
		this.remoteUDP = new InetSocketAddress(remoteHost, remoteUDP);
		this.localPortRange = localPortRange;
	}


	public void connect() throws IOException
	{
		if (!this.state.compareAndSet(INetworkAPI.STATE_NOT_CONNECTED, INetworkAPI.STATE_CONNECTION_PENDING))
			return;
		this.postEvent(INetworkAPI.EVENT_STATE_CHANGED);

		try
		{
			this.con = new TCPConnection(this.createTCPSocket(), this)
			{
				/**
				 * {@inheritDoc}
				 */
				@Override
				protected void validateState()
				{
					if (ClientUnit.this.state.get() == INetworkAPI.STATE_CONNECTED && this.state() == INetworkAPI.STATE_NOT_CONNECTED)
					{
						ClientUnit.this.shutdown();
					}
				}
			};
		}
		catch (IOException | IllegalStateException e)
		{
			assert LOGGER.fine("Could not establish connection to client.", e);
		}
		
		this.state.set(this.con != null ? this.con.state() : INetworkAPI.STATE_NOT_CONNECTED);

		if (this.state.get() == INetworkAPI.STATE_NOT_CONNECTED)
			throw new IOException("Could not connect to server");
			
		if (this.clientID() == 0)
		{
			this.shutdown();
			throw new IOException("Could not retreive clientID.");
		}

		try
		{
			this.udp = new UDPHandler(InternalNetTools.newDatagramSocket(this.localPortRange), this.remoteUDP, this);
		}
		catch (IOException e)
		{
			LOGGER.fine("UDP Handler could not be initiated", e);
		}

		this.postEvent(INetworkAPI.EVENT_STATE_CHANGED);
	}

	private Socket createTCPSocket() throws IOException
	{
		for (int i = 0; i < 3; i++)
		{
			LOGGER.fine("TRYING TO CONNECT TO: " + this.remoteTCP);
			final Socket socket = InternalNetTools.newSocket(this.localPortRange);
			socket.connect(this.remoteTCP, INetworkAPI.CONNECTION_TIMEOUT);
			final byte[] initMSG = new byte[4 + InternalMessageAPI.INIT_MSGBODY_LENGTH];
			Converter.putIntBig(initMSG, InternalMessageAPI.OFFSET_LENGTH, InternalMessageAPI.INIT_MSGBODY_LENGTH);	//length
			initMSG[InternalMessageAPI.INIT_OFFSET_PROTOCOL_VERSION+4] = INetworkAPI.PROTOCOL_VERSION;
			Converter.putIntBig(initMSG, InternalMessageAPI.INIT_OFFSET_CLIENT_ID+4, this.clientID());
			Converter.putIntBig(initMSG, InternalMessageAPI.INIT_OFFSET_UDPPORT+4, ((InetSocketAddress) this.remoteUDP).getPort());
			initMSG[InternalMessageAPI.INIT_OFFSET_TYPE+4] = this.type();

			socket.getOutputStream().write(initMSG);
			socket.getOutputStream().flush();

			//init and ack messages only intern and not bound to message count - just know with which values to start
			final byte[] ackMSG = InternalNetTools.readRawMSG(socket.getInputStream(), InternalNetTools.TIMEOUT);
			if (ackMSG != null)
			{
				if (ackMSG.length == InternalMessageAPI.INITRET_MSGBODY_LENGTH)
				{
					final int field2 = Converter.getIntBig(ackMSG, InternalMessageAPI.INITRET_OFFSET_FIELD2);
					if (ackMSG[InternalMessageAPI.INITRET_OFFSET_TYPE] == InternalMessageAPI.INITACK_MSG_TYPE)
					{
						if (this.clientID() == 0)
							this.set(field2);
						else if (this.clientID() != field2)
						{
							try
							{
								throw new IOException("Got the wrong ClientID from the server: " + field2);
							}
							finally
							{
								Close.close(socket);
							}
						}
						return socket;
					}
					LOGGER.warn("TCPSocket got wrong Error-MSG with error ID: " + field2);
				}
				else
					LOGGER.fine("TCPSocket got wrong Ack-MSG: " + Strings.toHexString(ackMSG));
			}
			else
				LOGGER.fine("TCPSocket requesting Ack-MSG timed out.");
		}
		throw new IOException("Several problems occured when trying to connect to server, giving up");
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
		this.con.send(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean sendFast(Message msg)
	{
		if (this.state() != INetworkAPI.STATE_CONNECTED || this.udp == null || this.udp.isClosed())
			return false;
		this.udp.send(msg);
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
		this.con.close();
		this.closeAllSessions();
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


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("ClientUnit: ");
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
			case INetworkAPI.STATE_CONNECTION_PENDING:
				sb.append("Connection Pending");
				break;
		}
		sb.append('\n');
		sb.append(super.toString());
		return sb.toString();
	}

}
