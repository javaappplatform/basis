/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package de.d3fact.platform;

import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.commons.events.ITalker;
import github.javaappplatform.network.IClientUnit;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.ISession;
import github.javaappplatform.network.msg.IMessage;
import github.javaappplatform.network.server.IServer;
import github.javaappplatform.network.server.Server;
import github.javaappplatform.network.server.tcp.TCPUnit;

import java.io.IOException;

public class PingServer
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Server su = new Server(new TCPUnit(60321, 60322));

		su.addListener(INetworkAPI.EVENT_CLIENT_CONNECTED, serverListener, ITalker.PRIORITY_HIGH);

		try
		{
			su.start();

			System.out.println("PingServer started");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	private static IListener serverListener = new IListener()
		{

			@Override
			public void handleEvent(Event e)
			{
				if(e.type() == INetworkAPI.EVENT_CLIENT_CONNECTED)
				{
					PingServer.clientConnected(((IServer) e.getSource()).getClient(e.<Integer>getData().intValue()));
				}
				else if(e.type() == INetworkAPI.EVENT_MSG_RECEIVED)
				{
					IMessage msg = ((ISession) e.getSource()).receiveMSG();
					System.out.println(msg.session() + " " + msg.type());
				}
			}
		};

	private static void clientConnected(IClientUnit client)
	{
		System.out.println("Client connected on PingServer");

		ISession session = client.startSession();
		System.out.println("Started session to client: " + session);
		session.addListener(INetworkAPI.EVENT_MSG_RECEIVED, serverListener, ITalker.PRIORITY_HIGH);



		try
		{
			System.out.println("sending PING");
			session.asyncSend(10000, new byte[0]);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
