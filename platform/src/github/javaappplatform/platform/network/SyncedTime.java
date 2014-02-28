/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.network;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.network.IClientUnit;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.ISession;
import github.javaappplatform.network.interfaces.impl.PingInterface;
import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.network.interfaces.DispatchMessages;
import github.javaappplatform.platform.network.interfaces.GetInterface;


public class SyncedTime
{

	public static final String EXT_POINT = "github.javaappplatform.platform.network.SyncedTime";

	private static final Logger LOGGER = Logger.getLogger();
	private static final String SYNC_TIME_THREAD = "Synctime Thread";


	private PingInterface pi;
	private ISession ses;

	/**
	 * @return The current time - synchronized with a connected server - in millisecons after Jan 1, 1970.
	 */
	public long syncedTimeMillis()
	{
		if (this.pi != null && this.ses.state() == INetworkAPI.STATE_CONNECTED)
			return this.pi.getSynchedTimeMillis();

		try
		{
			IClientUnit unit = ExtensionRegistry.getExtension(IPlatformNetworkAPI.EXT_POINT).<IClientUnit>getService();
			if (unit.state() == INetworkAPI.STATE_CONNECTED)
			{
				this.ses = unit.startSession();
				DispatchMessages.automaticallyFor(this.ses).inThreadWithID(SYNC_TIME_THREAD);
				this.pi = GetInterface.with(PingInterface.EXT_ID).ffor(this.ses);
			}
		}
		catch (Exception e)
		{
			LOGGER.fine("Could not sync with server. Using local time", e);
			//do nothing we just use system currenttime millis
		}
		return System.currentTimeMillis();
	}

}
