/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.job;


/**
 * TODO javadoc
 * @author funsheep
 */
public final class JobStub implements IJob
{

	private final String name;
	private long overallSize = IJob.LENGTH_UNKNOWN;
	private long done = IJob.PROGRESS_UNKNOWN;
	private boolean finished;
	private final Thread jobthread;


	/**
	 *
	 */
	public JobStub(String name)
	{
		this(name, IJob.LENGTH_UNKNOWN);
	}

	public JobStub(String name, long size)
	{
		this(name, size, Thread.currentThread());
	}

	public JobStub(String name, long size, Thread jobthread)
	{
		this.overallSize = size;
		this.name = name;
		this.jobthread = jobthread;
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

	public synchronized void setLength(long length)
	{
		this.overallSize = length;
		this.setDone(this.done);
	}

	public synchronized void setDone(long count)
	{
		this.done = count;
		if (this.overallSize != LENGTH_UNKNOWN)
			this.done = Math.min(count, this.overallSize);
	}

	public synchronized void addDone(long addCount)
	{
		this.setDone(this.absoluteProgress() + addCount);
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
		return this.finished || (this.length() != LENGTH_UNKNOWN && this.length() == this.absoluteProgress());
	}

	public synchronized void finish()
	{
		this.finished = true;
		if (this.length() != LENGTH_UNKNOWN)
			this.setDone(this.length());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		if (this.jobthread != null && this.jobthread != Thread.currentThread())
			this.jobthread.interrupt();
	}

}
