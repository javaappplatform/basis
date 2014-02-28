/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.localfile;

import github.javaappplatform.platform.resource.Directory;
import github.javaappplatform.platform.resource.IDirectory;
import github.javaappplatform.platform.resource.IResource;
import github.javaappplatform.platform.resource.IResourceAPI;
import github.javaappplatform.platform.resource.IResourceAPI.OpenOption;
import github.javaappplatform.platform.resource.internal.AOutputStreamJoin;
import github.javaappplatform.platform.resource.internal.IInternalFile;
import github.javaappplatform.platform.resource.internal.IManagedResource;
import github.javaappplatform.platform.resource.internal.JobInputStream;
import github.javaappplatform.platform.resource.internal.JobOutputStream;
import github.javaappplatform.platform.resource.internal.ResourceTools;
import github.javaappplatform.platform.utils.URIs;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;



class InternalLocalFile implements IInternalFile, IManagedResource
{

//	private static final Logger LOGGER = Logger.getLogger();


	private final URI uri;
	protected final Path path;
	private volatile int openStreams = 0;
	private final AOutputStreamJoin out = new AOutputStreamJoin()
	{

		@Override
		protected OutputStream openJoin(int options) throws IOException
		{
			final Set<java.nio.file.OpenOption> oo = FileTools.parseWrite(options);
			oo.add(StandardOpenOption.WRITE);
			final java.nio.file.OpenOption[] ooarr = oo.toArray(new java.nio.file.OpenOption[oo.size()]);

			return Files.newOutputStream(InternalLocalFile.this.path, ooarr);
		}

	};

	protected InternalLocalFile(URI uri)
	{
		this.uri = uri;
		this.path = Paths.get(uri);
	}

	protected InternalLocalFile(URI uri, Path path)
	{
		this.uri = uri;
		this.path = path;
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
		return IResourceAPI.TYPE_FILE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists()
	{
		return Files.exists(this.path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWritable()
	{
		return Files.isWritable(this.path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadable()
	{
		return Files.isReadable(this.path);
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
		try
		{
			return Files.probeContentType(this.path);
		} catch (IOException e)
		{
			//die silently
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long size()
	{
		if (!this.exists() || !this.isReadable())
			return IResource.UNKNOWN_SIZE;
		try
		{
			return Files.size(this.path);
		} catch (IOException e)
		{
			return IResource.UNKNOWN_SIZE;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long lastTimeModified()
	{
		if (!this.exists() || !this.isReadable())
			return IResource.UNKNOWN_SIZE;
		try
		{
			return Files.getLastModifiedTime(this.path).toMillis();
		} catch (IOException e)
		{
			return IResource.UNKNOWN_SIZE;
		}
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
	public void close()
	{
		//do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String name()
	{
		return URIs.extractName(this.uri);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String fileExtension()
	{
		return ResourceTools.getRawFileExt(this.name());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDirectory getParent()
	{
		return Directory.at(URIs.resolveParent(this.uri));
	}


	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("resource")
	@Override
	public synchronized InputStream openStreamToRead(int options) throws IOException
	{
		if (OpenOption.TRY_TO_OPEN.isSetIn(options) && !this.isReadable())
			return null;

		try
		{
			final Set<java.nio.file.OpenOption> oo = FileTools.parseWrite(options);
			oo.add(StandardOpenOption.READ);
			final java.nio.file.OpenOption[] ooarr = oo.toArray(new java.nio.file.OpenOption[oo.size()]);

			InputStream stream = Files.newInputStream(InternalLocalFile.this.path, ooarr);
			this.openStreams++;
			return new JobInputStream("Read " + this.uri, this.size(), stream, this);
		}
		catch (IOException e)
		{
			FileTools.deleteOnFail(options, this.path);
			if (!OpenOption.TRY_TO_OPEN.isSetIn(options))
				throw e;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("resource")
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
			this.openStreams++;
			return new JobOutputStream("Write " + this.uri, this.size(), stream, this);
		}
		catch (IOException e)
		{
			FileTools.deleteOnFail(options, this.path);
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
		return Files.deleteIfExists(this.path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasOpenStreams()
	{
		assert this.openStreams >= 0;
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
