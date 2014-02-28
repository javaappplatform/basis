/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.network;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.network.PortRange;
import github.javaappplatform.network.client.ClientUnit;
import github.javaappplatform.platform.Platform;
import github.javaappplatform.platform.PlatformException;
import github.javaappplatform.platform.boot.IBootEntry;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.network.interfaces.DispatchMessages;

import java.io.IOException;

/**
 * TODO javadoc
 * @author funsheep
 */
public class BootEntry implements IBootEntry
{

	private static final Logger LOGGER = Logger.getLogger();


	private NetworkJob job;


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startup(Extension e) throws PlatformException
	{
		if (!Platform.hasOption("host") || !Platform.hasOption("hostTCP") || !Platform.hasOption("hostUDP"))
			return;

		try
		{
			final String server = Platform.getOptionValue("host", "localhost");
			final int tcp = Platform.getOptionValue("hostTCP", 7000);
			final int udp = Platform.getOptionValue("hostUDP", 7001);
			final int clientID = Platform.getOptionValue("clientID", 0);

			ClientUnit unit = new ClientUnit(clientID, server, tcp, udp, getPortRange(), (byte) 1);
			DispatchMessages.automaticallyFor(unit).inNetworkThread();

			unit.connect();

			ExtensionRegistry.registerSingleton(IPlatformNetworkAPI.EXT_POINT, IPlatformNetworkAPI.EXT_POINT, unit, null);

			this.job = new NetworkJob(unit);
		}
		catch (NumberFormatException ex)
		{
			throw new PlatformException("Could not parse either tcpport or udpport definition", ex);
		}
		catch (IOException ex)
		{
			throw new PlatformException("Could not connect to server. Aborting!", ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		if (this.job != null)
		{
			this.job.shutdown();
			this.job = null;
		}
	}

	private static final PortRange getPortRange()
	{
		PortRange range = null;
		if (Platform.hasOption("portrange"))
		{
			String[] portrange = Platform.getOptionValue("portrange").split("\\.\\.");
			if (portrange.length >= 2)
			{
				int minport = -1;
				int maxport = -1;
				try
				{
					minport = Integer.parseInt(portrange[0]);
				}
				catch (NumberFormatException ex)
				{
					LOGGER.warn("Could not parse minport. Using range 49152..65535!");
					LOGGER.fine("", ex);
					return range;
				}
				try
				{
					maxport = Integer.parseInt(portrange[1]);
				}
				catch (NumberFormatException ex)
				{
					LOGGER.warn("Could not parse maxport. Using range 49152..65535!");
					LOGGER.fine("", ex);
					return range;
				}
				if (minport > maxport)
				{
					int help = minport;
					minport = maxport;
					maxport = help;
				}
				if (minport > 49152)
				{
					range = new PortRange(minport, maxport);
				}

			}
			else
				LOGGER.warn("Portrange format unknown. Should be 'minport'..'maxport'. Using range default range 49152..65535 instead!");
		}
		return range;
	}

}
