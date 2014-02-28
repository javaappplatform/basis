/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.internal.tcp;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.internal.InternalNetTools;
import github.javaappplatform.network.internal.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * TODO javadoc
 * @author funsheep
 */
public abstract class AUDPReceiver extends Thread
{

	protected static final Logger LOGGER = Logger.getLogger();


	private final DatagramPacket packet = new DatagramPacket(new byte[INetworkAPI.MAX_UDP_PAKET_SIZE], INetworkAPI.MAX_UDP_PAKET_SIZE);
	private final DatagramSocket _socket;


	/**
	 *
	 */
	public AUDPReceiver(DatagramSocket socket)
	{
		this._socket = socket;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run()
	{
		try
		{
			while (!this.isInterrupted())
			{
				this._socket.receive(this.packet);
				final Message msg = InternalNetTools.readUDPMSG(this.packet);
				assert LOGGER.fine("Received " + msg);
				AUDPReceiver.this.received(msg);
			}
		} catch (final IOException e)
		{
			LOGGER.fine("UDP port closed", e);
		}
	}

	protected abstract void received(Message msg);

}
