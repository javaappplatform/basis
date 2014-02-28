/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.server.tcp;

import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.internal.AClientUnit;
import github.javaappplatform.network.internal.InternalMessageAPI;
import github.javaappplatform.network.internal.InternalNetTools;
import github.javaappplatform.network.internal.Message;
import github.javaappplatform.network.internal.tcp.AUDPReceiver;
import github.javaappplatform.network.server.IServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * TODO javadoc
 * @author funsheep
 */
class UDPHandler
{

	private static final Logger LOGGER = Logger.getLogger();

	private static final class SendPacket
	{

		private static final LinkedBlockingDeque<SendPacket> CACHE = new LinkedBlockingDeque<>(200);


		public SocketAddress remoteUDP;
		public Message msg;

		private void set(SocketAddress remoteUDP, Message msg)
		{
			this.remoteUDP = remoteUDP;
			this.msg = msg;
		}

		public void dispose()
		{
			this.msg.dispose();
			CACHE.offer(this);
		}

		public static SendPacket get(SocketAddress remoteUDP, Message msg)
		{
			SendPacket p = CACHE.poll();
			if (p == null)
				p = new SendPacket();
			p.set(remoteUDP, msg);
			return p;
		}
	}

	private final Thread _sender = new Thread()
	{

		{
			this.setDaemon(true);
		}

		@Override
		public void run()
		{
			try
			{
				final byte[] buffer = new byte[INetworkAPI.MAX_UDP_PAKET_SIZE];
				final DatagramPacket packet = new DatagramPacket(buffer, 0);
				while (!UDPHandler.this.isClosed())
				{
					final SendPacket pak = UDPHandler.this._sendQueue.takeFirst();
					packet.setSocketAddress(pak.remoteUDP);
					InternalNetTools.prepUDPMSG(buffer, pak.msg);
					packet.setLength(InternalMessageAPI.LENGTH_UDP_HEADER + pak.msg.body().length + 4);
					UDPHandler.this._socket.send(packet);
					if (pak.msg.callback() != null)
						pak.msg.callback().handleEvent(new Event(Integer.valueOf(pak.msg.session()), INetworkAPI.EVENT_MSG_SEND, Long.valueOf(pak.msg.orderID())));
					pak.dispose();
				}
			}
			catch (InterruptedException e)
			{
				//do nothing we are shutting down
			}
			catch (IOException ex)
			{
				LOGGER.fine("UDP connection closed.", ex);
			}
			
		}

	};


	private final LinkedBlockingDeque<SendPacket> _sendQueue = new LinkedBlockingDeque<>(INetworkAPI.MAX_SEND_MESSAGE_COUNTER);
	private final DatagramSocket _socket;
	private final AUDPReceiver _receiver;


	/**
	 *
	 */
	public UDPHandler(DatagramSocket socket, final IServer unit)
	{
		this._socket = socket;
		this._receiver = new AUDPReceiver(socket)
		{

			@Override
			protected void received(Message msg)
			{
				((AClientUnit) unit.getClient(msg.clientID())).distributeReceivedMSG(msg);
			}
		};
		this._receiver.setName("Server UDP Socket Receiver");
		this._sender.setName("Server UDP Socket Sender");
	}


	public void start()
	{
		this._receiver.start();
		this._sender.start();
	}

	public void send(SocketAddress remoteUDP, Message msg)
	{
		try
		{
			this._sendQueue.offer(SendPacket.get(remoteUDP, msg), INetworkAPI.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e)
		{
			LOGGER.fine("Connection Timeout.");
		}
	}
	
	public boolean isClosed()
	{
		return this._socket.isClosed();
	}

	public void shutdown()
	{
		if (this.isClosed())
			return;

		this._receiver.interrupt();
		this._sender.interrupt();
		Close.close(this._socket);
	}

}
