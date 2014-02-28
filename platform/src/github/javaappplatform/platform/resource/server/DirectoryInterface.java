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
class DirectoryInterface implements ISessionInterface
{

	static final String INTERFACE_ID = DirectoryInterface.class.getName();


	static final int[] MESSAGE_TYPES =
	{
		IMessageAPI.MSGTYPE_RESOURCE_DELETE_URI, IMessageAPI.MSGTYPE_RESOURCE_URI_DELETED, IMessageAPI.MSGTYPE_RESOURCE_DELETE_URI_ERROR,
		IMessageAPI.MSGTYPE_RESOURCE_CREATE_DIRECTORY, IMessageAPI.MSGTYPE_RESOURCE_DIRECTORY_CREATED, IMessageAPI.MSGTYPE_RESOURCE_CREATE_DIRECTORY_ERROR
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
			case IMessageAPI.MSGTYPE_RESOURCE_URI_DELETED:
				uri = this.msgReader.readString();
				this.results.tryPushResult(uri, Boolean.TRUE);
				break;
			case IMessageAPI.MSGTYPE_RESOURCE_DELETE_URI_ERROR:
				uri = this.msgReader.readString();
				errmsg = this.msgReader.readString();
				LOGGER.info("Could not delete directory "+uri+". Error: " + errmsg);
				this.results.tryPushResult(uri, Boolean.FALSE);
				break;
			case IMessageAPI.MSGTYPE_RESOURCE_DIRECTORY_CREATED:
				uri = this.msgReader.readString();
				this.results.tryPushResult(uri, Boolean.TRUE);
				break;
			case IMessageAPI.MSGTYPE_RESOURCE_CREATE_DIRECTORY_ERROR:
				uri = this.msgReader.readString();
				errmsg = this.msgReader.readString();
				LOGGER.info("Could not create directory "+uri+". Error: " + errmsg);
				this.results.tryPushResult(uri, Boolean.FALSE);
				break;
			default:
				LOGGER.warn("Recieved unknown message "+msg+". Will be ignored.");
		}
	}


	public boolean createDirectory(URI uri, boolean ensureExistence) throws IOException
	{
		Concurrent result;
		this.lock.lock();
		try
		{
			result = this.results.openConcurrent(uri.toString());

			SendMessage.ofType(IMessageAPI.MSGTYPE_RESOURCE_CREATE_DIRECTORY).
				with(uri.toString()).
				with(ensureExistence).
				over(this.localSession).
				usingReliableProtocol();
		}
		finally
		{
			this.lock.unlock();
		}

		return ServerTools.<Boolean>retrieveNormalizedResult(result).booleanValue();
	}

	public boolean delete(URI uri) throws IOException
	{
		Concurrent result;
		this.lock.lock();
		try
		{
			result = this.results.openConcurrent(uri.toString());

			SendMessage.ofType(IMessageAPI.MSGTYPE_RESOURCE_DELETE_URI).
				with(uri.toString()).
				over(this.localSession).
				usingReliableProtocol();

		}
		finally
		{
			this.lock.unlock();
		}

		return ServerTools.<Boolean>retrieveNormalizedResult(result).booleanValue();
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
