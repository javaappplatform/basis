/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.internal;

import github.javaappplatform.platform.job.IJob;
import github.javaappplatform.platform.job.JobPlatform;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * TODO javadoc
 *
 * @author funsheep
 */
public class JobInputStream extends FilterInputStream implements IJob
{

	private final String name;
	private final long overallSize;
	private final IManagedResource resource;
	private volatile long done = IJob.PROGRESS_UNKNOWN;
	private volatile boolean closed = false;


	/**
	 *
	 */
	public JobInputStream(String name, InputStream stream, IManagedResource resource)
	{
		this(name, IJob.LENGTH_UNKNOWN, stream, resource);

	}

	/**
	 *
	 */
	public JobInputStream(String name, long size, InputStream in, IManagedResource resource)
	{
		super(in);
		this.name = name;
		this.overallSize = size;
		this.resource = resource;
		JobPlatform.registerJob(this);
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
	public synchronized long absoluteProgress()
	{
		return this.done;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized long length()
	{
		return this.overallSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean isfinished()
	{
		return this.closed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		this.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException
	{
		try
		{
			int read = super.read();
			this.done++;
			return read;
		}
		catch (IOException ex)
		{
			this.closed = true;
			this.resource._notifyAboutClose(this);
			throw ex;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte b[], int off, int len) throws IOException
	{
		try
		{
			int read = super.read(b, off, len);
			this.done += read;
			return read;
		}
		catch (IOException ex)
		{
			this.closed = true;
			this.resource._notifyAboutClose(this);
			throw ex;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close()
	{
		if (!this.closed)
		{
			this.closed = true;
			try
			{
				super.close();
			}
			catch (IOException e)
			{
				//do nothing
			}
			this.resource._notifyAboutClose(this);
		}
	}


}
