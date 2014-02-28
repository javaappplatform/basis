/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.server;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.network.ISession;
import github.javaappplatform.network.interfaces.IInterfaceType;
import github.javaappplatform.network.interfaces.ISessionInterface;
import github.javaappplatform.network.interfaces.impl.IMessageAPI;
import github.javaappplatform.network.msg.IMessage;
import github.javaappplatform.network.msg.MessageReader;
import github.javaappplatform.network.msg.SendMessage;
import github.javaappplatform.platform.utils.concurrent.Concurrent;
import github.javaappplatform.platform.utils.concurrent.ConcurrentMap;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO javadoc
 * @author funsheep
 */
class SystemInterface implements ISessionInterface
{

	static final String INTERFACE_ID = SystemInterface.class.getName();


	static final int[] MESSAGE_TYPES =
	{
		IMessageAPI.MSGTYPE_RESOURCE_RESOLVE_URI, IMessageAPI.MSGTYPE_RESOURCE_URI_RESOLVED, IMessageAPI.MSGTYPE_RESOURCE_RESOLVE_URI_ERROR,
		IMessageAPI.MSGTYPE_RESOURCE_COPY_RESOURCES, IMessageAPI.MSGTYPE_RESOURCE_RESOURCES_COPIED, IMessageAPI.MSGTYPE_RESOURCE_COPY_RESOURCE_ERROR
	};


	private static final Logger LOGGER = Logger.getLogger();


	private ISession localSession;
	private final MessageReader msgReader = new MessageReader();
	private final ReentrantLock lock = new ReentrantLock();
	private final ConcurrentMap results = new ConcurrentMap();


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(ISession session)
	{
		this.localSession = session;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int type()
	{
		return IInterfaceType.SESSION_INTERFACE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(IMessage msg)
	{
		String uri;
		String errmsg;

		this.msgReader.reset(msg);
		switch (msg.type())
		{
			case IMessageAPI.MSGTYPE_RESOURCE_URI_RESOLVED:
				uri   = this.msgReader.readString();
				byte type    = this.msgReader.readByte();
				String props = this.msgReader.readString();
				//create internal Resource/file object based on type and props
				this.results.tryPushResult(uri, new Object[] { Byte.valueOf(type), props });
				break;
			case IMessageAPI.MSGTYPE_RESOURCE_RESOLVE_URI_ERROR:
				uri = this.msgReader.readString();
				errmsg = this.msgReader.readString();
				this.results.tryPushResult(uri, new IOException("Resource "+uri+" could not be resolved. Threw error "+errmsg));
				break;
			case IMessageAPI.MSGTYPE_RESOURCE_RESOURCES_COPIED:
				String from = this.msgReader.readString();
				String to   = this.msgReader.readString();
				this.results.tryPushResult(from+to, new Object());
				break;
			case IMessageAPI.MSGTYPE_RESOURCE_COPY_RESOURCE_ERROR:
				from = this.msgReader.readString();
				to   = this.msgReader.readString();
				errmsg = this.msgReader.readString();
				this.results.tryPushResult(from+to, new IOException("Resource "+from+" could not be copied to "+to+". Threw error "+errmsg));
				break;
			default:
				LOGGER.warn("Received unknown message "+msg+". Will be ignored.");
		}
	}


	//making this synchronized may be inefficient but is easy to implement.
	public Object[] resolveURIAsResource(URI uri) throws IOException
	{
		Concurrent result = null;
		this.lock.lock();
		try
		{
			result = this.results.openConcurrent(uri.toString());

			SendMessage.
				ofType(IMessageAPI.MSGTYPE_RESOURCE_RESOLVE_URI).
				with(uri.toString()).
				with(IMessageAPI.RESOURCE_RESOLVE_TYPE_RESOURCE).
				over(this.localSession).
				usingReliableProtocol();
		}
		finally
		{
			this.lock.unlock();
		}

		return ServerTools.retrieveNormalizedResult(result);
	}

	//making this synchronized may be inefficient but is easy to implement.
	public Object[] resolveURIAsDirectory(URI uri) throws IOException
	{
		Concurrent result = null;
		this.lock.lock();
		try
		{
			result = this.results.openConcurrent(uri.toString());

			SendMessage.
				ofType(IMessageAPI.MSGTYPE_RESOURCE_RESOLVE_URI).
				with(uri.toString()).
				with(IMessageAPI.RESOURCE_RESOLVE_TYPE_DIRECTORY).
				over(this.localSession).
				usingReliableProtocol();
		}
		finally
		{
			this.lock.unlock();
		}

		return ServerTools.retrieveNormalizedResult(result);
	}

	//making this synchronized may be inefficient but is easy to implement.
	public void copy(URI from, URI to, int options) throws IOException
	{
		Concurrent result = null;
		this.lock.lock();
		try
		{
			result = this.results.openConcurrent(from.toString()+to.toString());

			SendMessage.
				ofType(IMessageAPI.MSGTYPE_RESOURCE_COPY_RESOURCES).
				with(from.toString()).
				with(to.toString()).
				with(options).
				over(this.localSession).
				usingReliableProtocol();
		}
		finally
		{
			this.lock.unlock();
		}

		ServerTools.retrieveNormalizedResult(result, 0);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose()
	{
		//try to release threads that are locked in and are still waiting
		this.results.releaseConcurrents();
	}

}
