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
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.network.INetworkAPI;
import github.javaappplatform.network.ISession;
import github.javaappplatform.network.interfaces.IClientInterface;
import github.javaappplatform.network.interfaces.IInterfaceType;
import github.javaappplatform.network.interfaces.ISessionInterface;
import github.javaappplatform.network.msg.IMessage;
import github.javaappplatform.platform.job.ADoJob;
import github.javaappplatform.platform.job.JobPlatform;
import github.javaappplatform.platform.network.IPlatformNetworkAPI;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * TODO javadoc
 * @author funsheep
 */
class SessionMessageDispatcher implements IListener
{

	public static final int MAX_ACTIONS_PER_RUN = 4;


	private static final Logger LOGGER = Logger.getLogger();


	private class MessageProcessorJob extends ADoJob
	{

		private final AtomicBoolean dispatched = new AtomicBoolean(false);

		public MessageProcessorJob()
		{
			super("MessageProcessor for Session: " + SessionMessageDispatcher.this.session.sessionID());
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		public void doJob()
		{
			int receiv = 0;

			while (SessionMessageDispatcher.this.session.hasReceivedMSGs() && (receiv++ < MAX_ACTIONS_PER_RUN || SessionMessageDispatcher.this.session.state() == INetworkAPI.STATE_CLOSING))
			{
				IMessage msg = SessionMessageDispatcher.this.session.receiveMSG();
				SessionMessageDispatcher.this.dispatch(msg);
			}

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isfinished()
		{
			this.dispatched.set(!super.isfinished() && SessionMessageDispatcher.this.session.hasReceivedMSGs());
			return !this.dispatched.get();
		}

	}


	private final MessageProcessorJob processor;
	private final ISession session;
	private final String threadID;


	public SessionMessageDispatcher(ISession session)
	{
		this(session, IPlatformNetworkAPI.NETWORK_THREAD);
	}

	public SessionMessageDispatcher(ISession session, String threadID)
	{
		this.session = session;
		this.threadID = threadID;
		this.session.addListener(INetworkAPI.EVENT_MSG_RECEIVED, this, ITalker.PRIORITY_HIGH);
		this.session.addListener(INetworkAPI.EVENT_STATE_CHANGED, this, ITalker.PRIORITY_HIGH);
		this.processor = new MessageProcessorJob();
	}


	/**
	 * Dispatches the message to the appropriate interface. Retrieves the interface by calling {@link #getInterface(int)} with the message type of the message to process.
	 * @param msg The message to process.
	 */
	private void dispatch(IMessage msg)
	{
		try
		{
			IInterfaceType o = GetInterface.ffor(msg.type()).ffor(this.session);
			if (o != null)
			{
				switch (o.type())
				{
					case IInterfaceType.SESSION_INTERFACE:
						((ISessionInterface) o).execute(msg);
						break;
					case IInterfaceType.CLIENT_INTERFACE:
						((IClientInterface) o).execute(this.session, msg);
						break;
					default:
						throw new RuntimeException("Unknown interface type: " + o.type() + " Should not happen");
				}
			}
			else
				LOGGER.warn("Could not find an appropriate interface for message " + msg);
		}
		catch (Exception e)
		{
			try
			{
				GetInterface.dispose(this.session);
			}
			finally
			{
				Close.close(this.session);
			}
			throw e;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEvent(Event e)
	{
		if (e.type() == INetworkAPI.EVENT_MSG_RECEIVED){
			this.processor.dispatched.set(true);
			JobPlatform.runJob(this.processor, this.threadID);
		}
		else if (e.type() == INetworkAPI.EVENT_STATE_CHANGED && this.session.state() == INetworkAPI.STATE_CLOSING)
			GetInterface.dispose(this.session);
	}

}
