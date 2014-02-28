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
import github.javaappplatform.network.internal.IInternalServer;
import github.javaappplatform.network.internal.IInternalServerUnit;
import github.javaappplatform.network.internal.InternalMessageAPI;
import github.javaappplatform.network.internal.InternalNetTools;
import github.javaappplatform.network.msg.Converter;
import github.javaappplatform.network.server.IServerMessageAPI;
import github.javaappplatform.network.server.ServerUtils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TODO javadoc
 * @author funsheep
 */
public class TCPUnit extends Thread implements IInternalServerUnit
{

	private static final Logger LOGGER = Logger.getLogger();

	private final int _localTCP;
	private final int _localUDP;
	private IInternalServer _server;
	private ServerSocket _socket;
	private UDPHandler _udp;


	public TCPUnit(int localTCP, int localUDP)
	{
		super("TCP/UDP Serverunit");
		this._localTCP = localTCP;
		this._localUDP = localUDP;
		this.setDaemon(true);
	}

	
	public int tcpPort()
	{
		return this._localTCP;
	}
	
	public int udpPort()
	{
		return this._localUDP;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start(IInternalServer server) throws IOException
	{
		if (!this.isShutdown())
			return;

		this._server = server;
		this._socket = new ServerSocket();
		this._socket.bind(new InetSocketAddress(TCPUnit.this._localTCP), INetworkAPI.MAX_SERVER_CONNECTION_QUEUE);
		this._udp = new UDPHandler(new DatagramSocket(TCPUnit.this._localUDP), TCPUnit.this._server);
		this.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run()
	{
		try
		{
			while (!TCPUnit.this.isShutdown())
			{
				assert LOGGER.fine("Accepting connections on " + TCPUnit.this._socket);
				TCPUnit.this.socketAccepted(TCPUnit.this._socket.accept());
			}
		}
		catch (Exception e)
		{
			if (!TCPUnit.this.isShutdown())
			{
				LOGGER.info("TCP unit shutdown on error.", e);
				TCPUnit.this.shutdown();
			}
		}
		LOGGER.fine("TCP unit no longer accepting connections.");
	}


	@SuppressWarnings("resource")
	private void socketAccepted(Socket clientSocket)
	{
		int errorID = IServerMessageAPI._NO_ERROR;
		int clientProtocolVersion = 0;
		try
		{
			//error we are no longer running, send error and close socket
			if (!this.isShutdown())
			{
				InternalNetTools.configureSocket(clientSocket);
				LOGGER.info("Client "+ clientSocket.getInetAddress() +" connects on TCP port.");
				final byte[] initMSG = InternalNetTools.readRawMSG(clientSocket.getInputStream(), InternalNetTools.TIMEOUT);
				if (initMSG != null && initMSG.length == InternalMessageAPI.INIT_MSGBODY_LENGTH)
				{
					clientProtocolVersion = initMSG[InternalMessageAPI.INIT_OFFSET_PROTOCOL_VERSION];
					if (clientProtocolVersion == INetworkAPI.PROTOCOL_VERSION || clientProtocolVersion == 3)
					{
						int clientID = Converter.getIntBig(initMSG, InternalMessageAPI.INIT_OFFSET_CLIENT_ID);
						final int udpPort = Converter.getIntBig(initMSG, InternalMessageAPI.INIT_OFFSET_UDPPORT);
						final byte type = initMSG[InternalMessageAPI.INIT_OFFSET_TYPE];
						if (clientID == 0)
							clientID = this._server.reserveClientID();

						clientSocket.getOutputStream().write(ServerUtils.initAckV3(clientID));
						clientSocket.getOutputStream().flush();

						final TCPRemoteClientUnit client = new TCPRemoteClientUnit(clientID, type, clientSocket, udpPort, this._udp);
						this._server.register(client);
						return;
					}
					errorID = IServerMessageAPI.ERROR_UNSUPPORTED_PROTOCOL_VERSION;
				}
				else
					errorID = IServerMessageAPI.ERROR_WRONG_INIT_FORMAT;
			}
			else
				errorID = IServerMessageAPI.ERROR_SERVER_SHUTTING_DOWN;
		}
		catch (IllegalArgumentException iae)
		{
			errorID = IServerMessageAPI.ERROR_CLIENT_ID_NOLONGER_RESERVED;
		}
		catch (IOException ioe)
		{
			LOGGER.fine("Accepting a client socket threw an exception.", ioe);
			errorID = IServerMessageAPI.ERROR_UNKNOWN;
		}
		
		try
		{
			clientSocket.getOutputStream().write(ServerUtils.initError(errorID));
			clientSocket.getOutputStream().flush();
			LOGGER.fine("Socket not accepted. Error ID: " + errorID);
		}
		catch (IOException ex)
		{
			LOGGER.fine("Socket not accepted. Error ID: " + errorID + ". Additional exception when writing error message: ", ex);
		}
		finally
		{
			Close.close(clientSocket);
		}
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isShutdown()
	{
		return this.isInterrupted() || this._socket == null || this._socket.isClosed();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		if (this.isShutdown())
			return;

		this._udp.shutdown();
		this.interrupt();
		Close.close(this._socket);
		this._server.handleEvent(new Event(this, INetworkAPI.EVENT_STATE_CHANGED));
	}

}
