/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.java;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.platform.resource.IDirectory;
import github.javaappplatform.platform.resource.IResource;
import github.javaappplatform.platform.resource.IResourceAPI;
import github.javaappplatform.platform.resource.internal.IInternalFile;
import github.javaappplatform.platform.resource.internal.IManagedResource;
import github.javaappplatform.platform.resource.internal.JobInputStream;
import github.javaappplatform.platform.resource.internal.ResourceTools;
import github.javaappplatform.platform.utils.URIs;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;

/**
 * TODO javadoc
 * @author funsheep
 */
public class ClasspathFile implements IInternalFile, IManagedResource
{

	private static final ClassLoader CLASSLOADER = ClasspathFile.class.getClassLoader();


	private final URI uri;
	private final String name;
	private volatile int openStreams = 0;


	/**
	 *
	 */
	public ClasspathFile(URI uri)
	{
		this.uri = uri;
		this.name = URIs.extractName(uri);
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
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String mimetype()
	{
		return ResourceTools.mimetype(this.uri);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long size()
	{
		return IResource.UNKNOWN_SIZE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long lastTimeModified()
	{
		return IResource.UNKNOWN_MODIFIED_TIME;
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
	public String name()
	{
		return this.name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String fileExtension()
	{
		return ResourceTools.getRawFileExt(this.name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDirectory getParent()
	{
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized InputStream openStreamToRead(int options) throws IOException
	{
		final URI clean = ResourceTools.cleanURIFromOptions(this.uri());
		URL u1 = CLASSLOADER.getResource(clean.getSchemeSpecificPart());
		if (u1 != null)
		{
			InputStream stream = u1.openStream();
			this.openStreams++;
			return new JobInputStream("Read " + u1, stream, this);
		}
		assert debugThisThing(clean.getSchemeSpecificPart());
		return null;
	}

	private static final Logger LOGGER = Logger.getLogger();
	private static boolean debugThisThing(String l)
	{
		LOGGER.log(Level.INFO, CLASSLOADER.toString());
		if (CLASSLOADER instanceof URLClassLoader)
		{
			final URLClassLoader ucl = (URLClassLoader)CLASSLOADER;
			final URL[] u = ucl.getURLs();
			for (final URL element : u)
			{
				LOGGER.log(Level.FINEST, "URL in ClassPath: {0}", element);
				// TODO does this work with JARs?
				InputStream in = null;
				try
				{
					final URL u2 = new URL(element, l);
					LOGGER.log(Level.FINEST, "URL to check: {0}", u2);
					in = u2.openStream();
				} catch (final Exception e)
				{
					LOGGER.log(Level.WARNING, "Unable to open stream.", e);
				} finally
				{
					Close.close(in);
				}
			}
		}
		return true;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputStream openStreamToWrite(int options) throws IOException
	{
		return null;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean delete() throws IOException
	{
		return false;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		//do nothing
	}

}
