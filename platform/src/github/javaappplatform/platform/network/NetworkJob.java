/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.network;

import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.network.IClientUnit;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.client.ClientUnit;
import github.javaappplatform.platform.Platform;
import github.javaappplatform.platform.job.IJob;
import github.javaappplatform.platform.job.JobPlatform;

/**
 * TODO javadoc
 * @author funsheep
 */
class NetworkJob implements IJob, IListener
{


	private final ClientUnit unit;


	/**
	 * @param _unit
	 */
	public NetworkJob(ClientUnit _unit)
	{
		this.unit = _unit;
		if (Platform.hasOption("network_shutdown"))
			this.unit.addListener(INetworkAPI.EVENT_STATE_CHANGED, this);
		JobPlatform.registerJob(this);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String name()
	{
		return "Network Unit";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long length()
	{
		return IJob.LENGTH_UNKNOWN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long absoluteProgress()
	{
		return IJob.PROGRESS_UNKNOWN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isfinished()
	{
		return this.unit.state() == INetworkAPI.STATE_NOT_CONNECTED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		this.unit.close();
		this.unit.shutdown();
		JobPlatform.removeJob(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEvent(Event e)
	{
		if (e.<IClientUnit>getSource().state() == INetworkAPI.STATE_NOT_CONNECTED)
			Platform.shutdown(15 * 1000);	//fifteen seconds
	}

}
