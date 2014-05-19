/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.job;

import github.javaappplatform.platform.extension.Extension;




/**
 * TODO javadoc
 * @author funsheep
 */
public abstract class ADoJob implements IJob, IDoJob
{

	private final String name;
	protected volatile boolean shutdown = false;


	public ADoJob(String name)
	{
		this.name = name;
	}

	
	public final void schedule(Extension e)
	{
		final long delay = e.getProperty("delay", 0);
		final boolean loop  = e.getProperty("loop", false);
		final String thread = e.getProperty("thread", JobPlatform.MAIN_THREAD);
		this.schedule(thread, loop, delay);
	}

	public void schedule(String thread, boolean loop, long delay)
	{
		if (thread == null)
			throw new IllegalArgumentException("ThreadID may not be null");

		if (loop && delay > 0)
			JobPlatform.loopJob(this, thread, delay);
		else if (delay > 0)
			JobPlatform.scheduleJob(this, thread, delay);
		else
			JobPlatform.runJob(this, thread);
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
	public long length()
	{
		return LENGTH_UNKNOWN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long absoluteProgress()
	{
		return PROGRESS_UNKNOWN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isfinished()
	{
		return this.shutdown || (this.length() != LENGTH_UNKNOWN && this.absoluteProgress() >= this.length());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		this.shutdown = true;
	}

}
