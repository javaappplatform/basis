/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.network.interfaces;

import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.commons.events.ITalker;
import github.javaappplatform.network.IClientUnit;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.ISession;
import github.javaappplatform.platform.network.IPlatformNetworkAPI;

/**
 * TODO javadoc
 * @author funsheep
 */
public class DispatchMessages
{

	private static final class NewSessionListener implements IListener
	{

		private final IClientUnit unit;
		private final String threadID;

		protected NewSessionListener(IClientUnit unit, String threadID)
		{
			this.unit = unit;
			this.threadID = threadID;
			this.unit.addListener(INetworkAPI.EVENT_SESSION_STARTED, this, ITalker.PRIORITY_HIGH);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		public synchronized void handleEvent(Event e)
		{
			ISession session = this.unit.getSession(e.<Integer>getData().intValue());
			DispatchMessages.automaticallyFor(session).inThreadWithID(this.threadID);
		}
	}

	private final ISession session;
	private final IClientUnit unit;

	/**
	 *
	 */
	private DispatchMessages(ISession session)
	{
		this.session = session;
		this.unit = null;
	}

	private DispatchMessages(IClientUnit unit)
	{
		this.session = null;
		this.unit = unit;
	}


	public static final DispatchMessages automaticallyFor(ISession _session)
	{
		return new DispatchMessages(_session);
	}

	public static final DispatchMessages automaticallyFor(IClientUnit _unit)
	{
		return new DispatchMessages(_unit);
	}


	@SuppressWarnings("unused")
	public void inThreadWithID(String threadID)
	{
		if (this.session != null)
			new SessionMessageDispatcher(this.session, threadID);
		else if (this.unit != null)
			new NewSessionListener(this.unit, threadID);
	}

	public void inNetworkThread()
	{
		this.inThreadWithID(IPlatformNetworkAPI.NETWORK_THREAD);
	}


}
