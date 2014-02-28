/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.weblink;

import github.funsheep.javadiskcache.FileCache;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.network.utils.Connect;
import github.javaappplatform.network.utils.HTTPInputStream;
import github.javaappplatform.platform.resource.IResource;
import github.javaappplatform.platform.resource.IResourceAPI;
import github.javaappplatform.platform.resource.IResourceAPI.OpenOption;
import github.javaappplatform.platform.resource.internal.IInternalResource;
import github.javaappplatform.platform.resource.internal.IManagedResource;
import github.javaappplatform.platform.resource.internal.JobInputStream;
import github.javaappplatform.platform.resource.internal.ResourceTools;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;

/**
 * TODO javadoc
 * @author funsheep
 */
public class InternalWeblink implements IInternalResource, IManagedResource
{

	private static final Logger LOGGER = Logger.getLogger();

	private final URI uri;
	private URLConnection connection;
	private volatile int openStreams = 0;


	public InternalWeblink(URI uri)
	{
		this.uri = uri;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized InputStream openStreamToRead(int options) throws IOException
	{
		if (!this.exists() && OpenOption.TRY_TO_OPEN.isSetIn(options))
			return null;

		InputStream cached = FileCache.instance().getCachedInputStream(this.uri().toString(), null, this.size(), this.lastTimeModified());
		if (cached == null)
			cached = FileCache.instance().getCachedInputStream(this.uri().toString(), new HTTPInputStream(this.connection), this.size(), this.lastTimeModified());

		this.openStreams++;
		return new JobInputStream("Read " + this.uri, this.size(), cached, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI uri()
	{
		return this.uri;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int type()
	{
		return IResourceAPI.TYPE_UNKNOWN_RESOURCE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists()
	{
		if(this.connection != null)
			return true;
		try
		{
			this.connection = Connect.lightlyTo(this.uri);
		} catch (IOException e)
		{
			LOGGER.warn("Could not connect to '" + this.uri + "'.", e);
			this.connection = null;
		}
		return this.connection != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String mimetype()
	{
		String mime = ResourceTools.mimetype(this.uri);
		if (mime != null)
			return mime;
		if (this.exists())
			return this.connection.getContentType();
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long size()
	{
		if (!this.exists() || this.connection.getContentLengthLong() == -1)
			return IResource.UNKNOWN_SIZE;
		return this.connection.getContentLengthLong();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long lastTimeModified()
	{
		if (!this.exists())
			return IResource.UNKNOWN_MODIFIED_TIME;
		long lastModified = this.connection.getLastModified();
		return lastModified > 0 ? lastModified : IResource.UNKNOWN_MODIFIED_TIME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadable()
	{
		return this.exists();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWritable()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void discard()
	{
		//do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close()
	{
		this.connection = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputStream openStreamToWrite(int options) throws IOException
	{
		if (OpenOption.TRY_TO_OPEN.isSetIn(options))
			return null;
		throw new UnsupportedOperationException("Writing to files is not permitted.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean delete() throws IOException
	{
		throw new UnsupportedOperationException("Deleting files is not permitted.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasOpenStreams()
	{
		return this.openStreams > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void _notifyAboutClose(Closeable close)
	{
		this.openStreams--;
	}

}
