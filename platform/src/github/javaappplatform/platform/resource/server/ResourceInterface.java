/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.server;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.GenericsToolkit;
import github.javaappplatform.network.ISession;
import github.javaappplatform.network.interfaces.IInterfaceType;
import github.javaappplatform.network.interfaces.ISessionInterface;
import github.javaappplatform.network.interfaces.impl.IMessageAPI;
import github.javaappplatform.network.interfaces.impl.StreamingInterface;
import github.javaappplatform.network.msg.IMessage;
import github.javaappplatform.network.msg.MessageReader;
import github.javaappplatform.network.msg.SendMessage;
import github.javaappplatform.platform.network.interfaces.GetInterface;
import github.javaappplatform.platform.utils.concurrent.Concurrent;
import github.javaappplatform.platform.utils.concurrent.ConcurrentMap;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO javadoc
 * @author funsheep
 */
class ResourceInterface implements ISessionInterface
{

	static final String INTERFACE_ID = ResourceInterface.class.getName();


	static final int[] MESSAGE_TYPES =
	{
		IMessageAPI.MSGTYPE_RESOURCE_OPEN_STREAM, IMessageAPI.MSGTYPE_RESOURCE_STREAM_OPENED, IMessageAPI.MSGTYPE_RESOURCE_OPEN_STREAM_ERROR,
		IMessageAPI.MSGTYPE_RESOURCE_DELETE_URI, IMessageAPI.MSGTYPE_RESOURCE_URI_DELETED, IMessageAPI.MSGTYPE_RESOURCE_DELETE_URI_ERROR
	};


	private static final Logger LOGGER = Logger.getLogger();


	private static AtomicInteger STREAM_ID = new AtomicInteger(Integer.MIN_VALUE);


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
			case IMessageAPI.MSGTYPE_RESOURCE_STREAM_OPENED:
				this.results.tryPushResult(Integer.valueOf(this.msgReader.readInt()), Boolean.TRUE);
				break;
			case IMessageAPI.MSGTYPE_RESOURCE_OPEN_STREAM_ERROR:
				int streamID = this.msgReader.readInt();
				errmsg = this.msgReader.readString();
				this.results.tryPushResult(Integer.valueOf(streamID), new IOException("Could not open stream for ID "+streamID+". Threw error "+errmsg));
				break;
			case IMessageAPI.MSGTYPE_RESOURCE_URI_DELETED:
				uri = this.msgReader.readString();
				this.results.tryPushResult(uri, Boolean.TRUE);
				break;
			case IMessageAPI.MSGTYPE_RESOURCE_DELETE_URI_ERROR:
				uri = this.msgReader.readString();
				errmsg = this.msgReader.readString();
				LOGGER.info("Could not delete resource "+uri+". Error: " + errmsg);
				this.results.tryPushResult(uri, Boolean.FALSE);
				break;
			default:
				LOGGER.warn("Recieved unknown message "+msg+". Will be ignored.");
		}
	}


	public <C extends Closeable> C openStream(URI uri, boolean _forRead, int options) throws IOException
	{
		int streamID = STREAM_ID.incrementAndGet();
		Concurrent result;
		this.lock.lock();
		try
		{
			result = this.results.openConcurrent(Integer.valueOf(streamID));

			SendMessage.ofType(IMessageAPI.MSGTYPE_RESOURCE_OPEN_STREAM).
				with(uri.toString()).
				with(streamID).
				with((byte) (_forRead ? 0 : 1)).
				with(options).
				over(this.localSession).
				usingReliableProtocol();
		}
		finally
		{
			this.lock.unlock();
		}

		ServerTools.retrieveNormalizedResult(result);
		StreamingInterface face = GetInterface.with(StreamingInterface.ID).<StreamingInterface>ffor(this.localSession);
		if (_forRead)
			return GenericsToolkit.<C>convertUnchecked(face.request(streamID));
		C stream = GenericsToolkit.<C>convertUnchecked(face.send(streamID));
		face.keepAlive(streamID);
		return stream;
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
