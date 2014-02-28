/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package de.d3fact.network;

import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.ISession;
import github.javaappplatform.network.client.ClientUnit;
import github.javaappplatform.network.server.Server;
import github.javaappplatform.network.server.tcp.TCPUnit;



/**
 * TODO javadoc
 * @author funsheep
 */
public class NetworkDemo
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		final IListener clientPrinter = new IListener()
		{

			@Override
			public synchronized void handleEvent(Event e)
			{
				System.out.print("Client: ");
				switch (e.type())
				{
					case INetworkAPI.EVENT_SESSION_STARTED:
						System.out.println("EVENT_SESSION_STARTED");
					case INetworkAPI.EVENT_MSG_RECEIVED:
						System.out.println("EVENT_MSG_RECEIVED");
						break;
					case INetworkAPI.EVENT_MSG_SEND:
						System.out.println("EVENT_MSG_SEND");
						break;
					case INetworkAPI.EVENT_STATE_CHANGED:
						System.out.println("EVENT_STATE_CHANGED");
						break;
				}

				if (e.type() == INetworkAPI.EVENT_STATE_CHANGED && e.getSource() instanceof ClientUnit)
					System.out.println("State Changed: " + ((ClientUnit) e.getSource()).state());
				System.out.println(e);
			}
		};


//		final IListener serverPrinter = new IListener()
//		{
//
//			@Override
//			public synchronized void handleEvent(Event e)
//			{
//				System.out.print("Server: ");
//				switch (e.type())
//				{
//					case INetworkAPI.EVENT_CLIENT_CONNECTED:
//						System.out.println("EVENT_CLIENT_CONNECTED");
//						IClientUnit unit = e.<IServer>getSource().getClient(e.<Integer>getData().intValue());
//						unit.addListener(INetworkAPI.EVENT_SESSION_STARTED, clientPrinter);
//						unit.addListener(INetworkAPI.EVENT_STATE_CHANGED, clientPrinter);
//						unit.addListener(INetworkAPI.EVENT_MSG_RECEIVED, clientPrinter);
//						unit.addListener(INetworkAPI.EVENT_MSG_SEND, clientPrinter);
//						break;
//					case INetworkAPI.EVENT_STATE_CHANGED:
//						System.out.println("EVENT_STATE_CHANGED");
//						break;
//				}
//
//				if (e.type() == INetworkAPI.EVENT_STATE_CHANGED && e.getSource() instanceof ClientUnit)
//					System.out.println("State Changed: " + ((ClientUnit) e.getSource()).state());
//				System.out.println(e);
//			}
//		};


		final Server server = new Server(new TCPUnit(60321, 60322));
		server.start();
//		server.addListener(INetworkAPI.EVENT_CLIENT_CONNECTED, serverPrinter);
//		server.addListener(INetworkAPI.EVENT_STATE_CHANGED, serverPrinter);
		System.out.println("Server started");
//		SmallSet<ClientUnit> units = new SmallSet<ClientUnit>();
//		for (int i = 0; i < 2; i++)
//		{
//			final ClientUnit client = new ClientUnit("127.0.0.1", 60321, 60322, PortRange.DEFAULT);
//			units.add(client);
//			try
//			{
//				client.connect();
//			}
//			catch (IOException e)
//			{
//				System.out.println(e);
//			}
//		}
//		for (ClientUnit cu : units)
//			cu.close();
//		server.shutdown();
		final ClientUnit client = new ClientUnit("127.0.0.1", 60321, 60322, null, (byte) 0);
		client.addListener(INetworkAPI.EVENT_STATE_CHANGED, clientPrinter);
		client.connect();
		System.out.println(client.startSession());
		System.out.println(client.startSession());
		ISession s = client.startSession();
		s.close();
		System.out.println(server.getAllClients().iterator().next().startSession());
		server.getAllClients().iterator().next().close();
	}

}
