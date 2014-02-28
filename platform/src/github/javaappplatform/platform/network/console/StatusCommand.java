/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.network.console;

import github.javaappplatform.network.IClientUnit;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.ISession;
import github.javaappplatform.platform.console.ICommand;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.job.JobPlatform;
import github.javaappplatform.platform.network.IPlatformNetworkAPI;

import java.io.PrintStream;

/**
 * TODO javadoc
 * @author funsheep
 */
public class StatusCommand implements ICommand
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(final String[] args, final PrintStream out) throws Exception
	{
		Extension e = ExtensionRegistry.getExtension(IPlatformNetworkAPI.EXT_POINT);
		if (e == null)
		{
			out.println("Network not available.");
			return;
		}
		final IClientUnit unit = e.getService();
		if (unit == null)
		{
			out.println("Network not started.");
			return;
		}

		JobPlatform.runJob(new Runnable()
		{

			@Override
			public void run()
			{
				out.print("Network State: ");
				switch (unit.state())
				{
					case INetworkAPI.STATE_CLOSING:
						out.println("Closing.");
						break;
					case INetworkAPI.STATE_CONNECTED:
						out.println("Connected.");
						break;
					case INetworkAPI.STATE_CONNECTION_PENDING:
						out.println("Connection pending.");
						break;
					case INetworkAPI.STATE_NOT_CONNECTED:
						out.println("Not Connected.");
						break;
					case INetworkAPI.STATE_NOT_STARTED:
						out.println("Not Started.");
						break;
					case INetworkAPI.STATE_RUNNING:
						out.println("Running.");
						break;
					case INetworkAPI.STATE_STARTING:
						out.println("Starting.");
						break;
				}
				out.print("Current #Sessions: ");
				out.println(unit.getAllSessions() != null ? unit.getAllSessions().size() : 0);
				if (args.length > 0)
				{
					for (ISession session : unit.getAllSessions())
					{
						out.println("Session [" + session.sessionID() + "] Has received messages: " + session.hasReceivedMSGs() + " and is closed: " + (session.state() != INetworkAPI.STATE_CONNECTED));
					}
				}
			}
		}, IPlatformNetworkAPI.NETWORK_THREAD);
	}

}
