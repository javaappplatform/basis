/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package de.d3fact.platform;

import github.javaappplatform.platform.Platform;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.extension.ServiceInstantiationException;
import github.javaappplatform.platform.network.SyncedTime;

import java.util.Date;

public class SyncTimeTest
{
	public static void doit()
	{
		try
		{
			Extension ext = ExtensionRegistry.getExtension(SyncedTime.EXT_POINT);

			long synced = ext.<SyncedTime>getService().syncedTimeMillis();
			long now = Platform.currentTime();

			System.out.println("synced: " + synced);
			System.out.println(new Date(synced));
			System.out.println("now: " + now);
			System.out.println(new Date(now));

		} catch (ServiceInstantiationException e)
		{
			e.printStackTrace();
		}
	}
}
