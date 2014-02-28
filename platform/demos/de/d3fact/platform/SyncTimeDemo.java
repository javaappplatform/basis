/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package de.d3fact.platform;

import github.javaappplatform.network.ISession;
import github.javaappplatform.network.client.ClientUnit;
import github.javaappplatform.network.interfaces.impl.IMessageAPI;
import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.extension.ServiceInstantiationException;
import github.javaappplatform.platform.network.IPlatformNetworkAPI;
import github.javaappplatform.platform.network.interfaces.DispatchMessages;

import java.io.IOException;

public class SyncTimeDemo
{
	public static void doit()
	{
		System.out.println("Starting SYNC demo.");
		try
		{
			ClientUnit cu = ExtensionRegistry.getExtension(IPlatformNetworkAPI.EXT_POINT).getService();

			ISession sh = cu.startSession();
			DispatchMessages.automaticallyFor(sh).inNetworkThread();

			sh.asyncSend(IMessageAPI.MSGTYPE_PING, new byte[0]);

//			long sync = ((PingInterface)sh.getInterface(PingInterface.EXT_ID)).getSynchedTimeMillis();
//
//			System.out.println(System.currentTimeMillis() + " | synced: " + sync);
		} catch (ServiceInstantiationException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
