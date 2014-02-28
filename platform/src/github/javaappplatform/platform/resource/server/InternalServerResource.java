/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.server;

import github.funsheep.javadiskcache.FileCache;
import github.javaappplatform.commons.json.JSONReader;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Arrays2;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.network.IClientUnit;
import github.javaappplatform.network.ISession;
import github.javaappplatform.platform.Platform;
import github.javaappplatform.platform.network.interfaces.DispatchMessages;
import github.javaappplatform.platform.network.interfaces.RegisterInterface;
import github.javaappplatform.platform.resource.IResource;
import github.javaappplatform.platform.resource.IResourceAPI.OpenOption;
import github.javaappplatform.platform.resource.internal.AOutputStreamJoin;
import github.javaappplatform.platform.resource.internal.IInternalResource;
import github.javaappplatform.platform.resource.internal.IManagedResource;
import github.javaappplatform.platform.resource.internal.JobInputStream;
import github.javaappplatform.platform.resource.internal.JobOutputStream;
import github.javaappplatform.platform.resource.internal.ResourceTools;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import com.fasterxml.jackson.core.JsonToken;


class InternalServerResource implements IInternalResource, IManagedResource
{

	protected static final Logger LOGGER = Logger.getLogger();


	protected final URI uri;
	protected final ISession session;

	protected int type;
	private boolean exists;
	private String mimetype;
	private long size;
	private long lastModTime;
	private boolean isReadable;
	private boolean isWritable;
	protected final ResourceInterface network = new ResourceInterface();
	protected final SystemInterface netinfo = new SystemInterface();
	private final AOutputStreamJoin out = new AOutputStreamJoin()
	{

		@Override
		protected OutputStream openJoin(int options) throws IOException
		{
			return InternalServerResource.this.network.openStream(InternalServerResource.this.uri, false, options);
		}
	};
	private volatile int openStreams = 0;


	InternalServerResource(URI uri, String props, IClientUnit unit) throws IOException
	{
		this.uri = uri;
		this.session = unit.startSession();
		DispatchMessages.automaticallyFor(this.session).inNetworkThread();
		RegisterInterface.instance(this.network).with(ResourceInterface.INTERFACE_ID, ResourceInterface.MESSAGE_TYPES).at(this.session);
		RegisterInterface.instance(this.netinfo).with(SystemInterface.INTERFACE_ID, SystemInterface.MESSAGE_TYPES).at(this.session);
		this.update(props);
	}

	private final void update(String props) throws IOException
	{
		final JSONReader read = new JSONReader(props);
		read.nextToken(JsonToken.START_OBJECT);
		this.parseProps(read);
		read.nextToken(JsonToken.END_OBJECT);
	}

	protected void parseProps(JSONReader props) throws IOException
	{

		this.type = props.nextIntField("type");
		this.exists = props.nextBooleanField("exists");
		this.mimetype = props.nextTextField("mimetype");
		this.size = props.nextLongField("size");
		this.lastModTime = props.nextLongField("timestamp");
		this.isReadable = props.nextBooleanField("isReadable");
		this.isWritable = props.nextBooleanField("isWritable");
	}

	protected synchronized final void updateData() throws IOException
	{
		String props = this.netinfo.resolveURIAsResource(this.uri)[1].toString();
		this.update(props);
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
		return this.type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists()
	{
		try
		{
			this.updateData();
		}
		catch (IOException e)
		{
			LOGGER.warn("Could not retrieve information.", e);
			return false;
		}
		return this.exists;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWritable()
	{
		try
		{
			this.updateData();
		}
		catch (IOException e)
		{
			LOGGER.warn("Could not retrieve information.", e);
			return false;
		}
		return this.isWritable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadable()
	{
		try
		{
			this.updateData();
		}
		catch (IOException e)
		{
			LOGGER.warn("Could not retrieve information.", e);
			return false;
		}
		return this.isReadable;
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
		return this.mimetype;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long size()
	{
		if (!this.exists() || !this.isReadable())
			return IResource.UNKNOWN_SIZE;
		return this.size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long lastTimeModified()
	{
		if (!this.exists() || !this.isReadable())
			return IResource.UNKNOWN_SIZE;
		return this.lastModTime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void discard()
	{
		//do nothing - this method is never actually called and just inherited from IResource
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void close()
	{
		Close.close(this.session);
		this.openStreams = 0;
	}

	protected InputStream internalInputStream(int options) throws IOException
	{
		InputStream cached = this.getCachedStream(null);
		if (cached != null)
			return cached;
		return this.getCachedStream(this.network.<InputStream>openStream(this.uri, true, options));
	}

	private InputStream getCachedStream(InputStream stream) throws IOException
	{
		if (!Arrays2.contains(new String[] { "file", "ws" }, this.uri().getScheme().toLowerCase()) || Platform.hasOption("networkfileandwscache"))
			return FileCache.instance().getCachedInputStream(this.uri().toString(), stream, this.size(), this.lastTimeModified());
		return stream;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized InputStream openStreamToRead(int options) throws IOException
	{
		if (!this.isReadable())
		{
			InputStream stream = this.getCachedStream(null);

			if (stream != null)
			{
				this.openStreams++;
				return new JobInputStream("Read " + this.uri, this.size(), stream, this);
			}

			if (OpenOption.TRY_TO_OPEN.isSetIn(options))
				return null;

			throw new IOException("Resource " + this.uri + " cannot be read.");
		}
		try
		{

			InputStream stream = this.internalInputStream(options);
			this.openStreams++;
			return new JobInputStream("Read " + this.uri, this.size(), stream, this);
		}
		catch (IOException e)
		{
			if (!OpenOption.TRY_TO_OPEN.isSetIn(options))
				throw e;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized OutputStream openStreamToWrite(int options) throws IOException
	{
		if (OpenOption.CREATE_ONLY_WHEN_NEW.isSetIn(options) && !OpenOption.APPEND_TO_RESOURCE.isSetIn(options) && this.exists())
		{
			if (OpenOption.TRY_TO_OPEN.isSetIn(options))
				return null;
			throw new IOException("Resource does already exist. URI: " + this.uri());
		}

		try
		{

			OutputStream stream = this.out.open(options);
			this.updateData();
			this.openStreams++;
			return new JobOutputStream("Write " + this.uri, this.size(), stream, this);
		}
		catch (IOException e)
		{
			if (!OpenOption.TRY_TO_OPEN.isSetIn(options))
				throw e;
		}
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean delete() throws IOException
	{
		boolean deleted = this.network.delete(this.uri());
		this.updateData();
		return deleted;
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
