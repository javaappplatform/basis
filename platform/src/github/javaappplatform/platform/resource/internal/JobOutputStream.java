/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.internal;

import github.javaappplatform.commons.util.Close;
import github.javaappplatform.platform.job.IJob;
import github.javaappplatform.platform.job.JobPlatform;

import java.io.IOException;
import java.io.OutputStream;

/**
 * TODO javadoc
 *
 * @author funsheep
 */
public class JobOutputStream extends OutputStream implements IJob
{

	private final OutputStream out;
	private final String name;
	private final long overallSize;
	private final IManagedResource resource;
	private volatile long done = 0;


	/**
	 *
	 */
	public JobOutputStream(String name, OutputStream stream, IManagedResource resource)
	{
		this(name, IJob.LENGTH_UNKNOWN, stream, resource);
	}

	/**
	 *
	 */
	public JobOutputStream(String name, long size, OutputStream out, IManagedResource resource)
	{
		this.out = out;
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
	public boolean isfinished()
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
	public void write(int b) throws IOException
	{
		try
		{
			this.out.write(b);
			this.done++;
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
	public void write(byte b[], int off, int len) throws IOException
	{
		try
		{
			this.out.write(b, off, len);
			this.done += len;
		}
		catch (IOException ex)
		{
			this.closed = true;
			this.resource._notifyAboutClose(this);
			throw ex;
		}
	}


	private volatile boolean closed = false;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close()
	{
		if (!this.closed)
		{
			this.closed = true;
			Close.close(this.out);
			this.resource._notifyAboutClose(this);
		}
	}


}
