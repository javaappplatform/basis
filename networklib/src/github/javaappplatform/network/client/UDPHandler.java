/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.client;

import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.internal.AClientUnit;
import github.javaappplatform.network.internal.InternalMessageAPI;
import github.javaappplatform.network.internal.InternalNetTools;
import github.javaappplatform.network.internal.Message;
import github.javaappplatform.network.internal.tcp.AUDPReceiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * TODO javadoc
 * @author funsheep
 */
class UDPHandler
{

	private static final Logger LOGGER = Logger.getLogger();

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
				packet.setSocketAddress(UDPHandler.this.remoteUDP);
				while (!UDPHandler.this.isClosed())
				{
					final Message msg = UDPHandler.this._sendQueue.takeFirst();
					InternalNetTools.prepUDPMSG(buffer, msg);
					packet.setLength(InternalMessageAPI.LENGTH_UDP_HEADER + msg.body().length + 4);
					UDPHandler.this._socket.send(packet);
					if (msg.callback() != null)
						msg.callback().handleEvent(new Event(Integer.valueOf(msg.session()), INetworkAPI.EVENT_MSG_SEND, Long.valueOf(msg.orderID())));
					msg.dispose();
				}
			}
			catch (InterruptedException e)
			{
				//do nothing we are shutting down
			}
			catch (IOException ex)
			{
				LOGGER.debug("UDP connection closed.", ex);
			}
		}

	};


	private final LinkedBlockingDeque<Message> _sendQueue = new LinkedBlockingDeque<>(INetworkAPI.MAX_SEND_MESSAGE_COUNTER);
	private final DatagramSocket _socket;
	private final SocketAddress remoteUDP;
	private final AUDPReceiver _receiver;


	/**
	 *
	 */
	public UDPHandler(DatagramSocket socket, SocketAddress remoteUDP, final AClientUnit unit)
	{
		this.remoteUDP = remoteUDP;
		this._socket = socket;
		this._receiver = new AUDPReceiver(socket)
		{

			@Override
			protected void received(Message msg)
			{
				unit.distributeReceivedMSG(msg);
			}
			
		};
		
		if (this.isClosed())
			return;
		
		this._receiver.setName("UDPReceiver for client " + unit.clientID());
		this._sender.setName("UDPSender for client " + unit.clientID());
		this._receiver.start();
		this._sender.start();
	}


	public void send(Message msg)
	{
		this._sendQueue.add(msg);
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
		for (Message msg : this._sendQueue)
			msg.dispose();
	}

}
