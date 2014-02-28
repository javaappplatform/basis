/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.internal.tcp;

import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.internal.InternalNetTools;
import github.javaappplatform.network.internal.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * TODO javadoc
 * @author funsheep
 */
class TCPHandler
{

	protected static final Logger LOGGER = Logger.getLogger();


	private final Thread _receiver = new Thread()
	{

		{
			this.setDaemon(true);
		}

		@Override
		public void run()
		{
			InputStream in = null;
			try
			{
				in = TCPHandler.this._socket.getInputStream();

				while (TCPHandler.this.isConnected())
				{
					final Message msg = InternalNetTools.readTCPMSG(in);
					if (msg == null)
						break;

					assert LOGGER.fine("Received " + msg);
					TCPHandler.this._con.received(msg);
				}
				TCPHandler.this.shutdown();
			}
			catch (final IOException e)
			{
				TCPHandler.this.errorHandling(e);
			}
			finally
			{
				Close.close(in);
			}
		}

	};


	private final Thread _sender = new Thread()
	{
		{
			this.setDaemon(true);
		}
//		MessageReader read = new MessageReader();

		@Override
		public void run()
		{
			Message msg = null;
			OutputStream out = null;
			try
			{
				out = TCPHandler.this._socket.getOutputStream();
				while (TCPHandler.this.isConnected())
				{
					msg = TCPHandler.this._con.take();
					if (msg != null)
					{
						assert LOGGER.fine("Send " + msg);
//						if (msg.type() == 200 || msg.type() == 201 || msg.type() == 202)
//						{
//							read.reset(msg);
//							switch (msg.type())
//							{
//								case 200:
//									System.out.println("Send Head for " + read.readInt());
//									break;
//								case 201:
//									System.out.println("Send Inter for " + read.readInt());
//									break;
//								case 202:
//									System.out.println("Send Tail for " + read.readInt());
//									break;
//							}
//							
//						}
						InternalNetTools.sendMSG(out, msg);
						if (msg.callback() != null)
							msg.callback().handleEvent(new Event(Integer.valueOf(msg.session()), INetworkAPI.EVENT_MSG_SEND, Long.valueOf(msg.orderID())));
						msg.dispose();
					}
				}
				//cleanup
				TCPHandler.this.shutdown();
			}
			catch (IOException ex)
			{
				if (msg != null)
					TCPHandler.this._con.sendSystem(msg);
				TCPHandler.this.errorHandling(ex);
			}
			finally
			{
				Close.close(out);
			}
		}
	};


	private final Socket _socket;
	private final TCPConnection _con;


	/**
	 *
	 */
	public TCPHandler(Socket tcpSocket, TCPConnection con)
	{
		this._socket = tcpSocket;
		this._con = con;
		
		if (!this.isConnected())
			return;

		this._receiver.setName("TCPReceiver for: " + tcpSocket.getLocalAddress());
		this._sender.setName("TCPSender for: " + tcpSocket.getLocalAddress());
		this._receiver.start();
		this._sender.start();
	}


	public InetSocketAddress getAddress()
	{
		return new InetSocketAddress(this._socket.getInetAddress(), this._socket.getPort());
	}

	public boolean isConnected()
	{
		return this._socket.isConnected() && !this._socket.isClosed();
	}

	private void errorHandling(IOException e)
	{
		assert LOGGER.fine("TCP MSG Handler shutdown.", e);
		this.shutdown();
	}

	public void shutdown()
	{
		this._sender.interrupt();
		Close.close(TCPHandler.this._socket);
		this._con.validateState();
	}

}
