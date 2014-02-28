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
public final class ThreadJob extends Thread
{

	private final JobStub stub = new JobStub(this.getName(), IJob.LENGTH_UNKNOWN, this);


	/**
	 * @param target
	 * @param name
	 */
	public ThreadJob(Runnable target, String name)
	{
		super(target, name);
	}

	/**
	 * @param target
	 */
	public ThreadJob(Runnable target)
	{
		super(target);
	}

	/**
	 * @param group
	 * @param target
	 * @param name
	 */
	public ThreadJob(ThreadGroup group, Runnable target, String name)
	{
		super(group, target, name);
	}

	/**
	 * @param group
	 * @param target
	 */
	public ThreadJob(ThreadGroup group, Runnable target)
	{
		super(group, target);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run()
	{
		try
		{
			super.run();
		}
		finally
		{
			ThreadJob.this.stub.finish();
		}
	}

}
