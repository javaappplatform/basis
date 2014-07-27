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
final class RunnableJob extends ADoJob implements IDoJob
{

	private long progress = 0;
	private final Runnable run;


	public RunnableJob(String name, Runnable run)
	{
		super(name);
		this.run = run;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		//do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long absoluteProgress()
	{
		return this.progress;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doJob()
	{
		this.run.run();
		this.progress = 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long length()
	{
		return 1;
	}

}
