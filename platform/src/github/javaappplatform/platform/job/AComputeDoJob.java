/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.job;

import github.javaappplatform.commons.util.GenericsToolkit;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * TODO javadoc
 * @author funsheep
 */
public abstract class AComputeDoJob extends ADoJob implements IDoJob
{


	private final ReentrantLock lock = new ReentrantLock();
	private final Condition waitForFinish = this.lock.newCondition();
	private String threadName;
	private Object result;
	private Exception error;


	/**
	 * @param name
	 */
	public AComputeDoJob(String name)
	{
		super(name);
	}

	public AComputeDoJob(String name, String thread)
	{
		this(name);
		this.schedule(thread, false, 0);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void schedule(String thread, boolean loop, long delay)
	{
		this.threadName = thread;
		super.schedule(thread, loop, delay);
	}


	protected final void finished(Object object)
	{
		this.lock.lock();
		try
		{
			this.result = object;
			this.shutdown();
			this.waitForFinish.signalAll();
		}
		finally
		{
			this.lock.unlock();
		}
	}


	protected final void finishedWithError(Exception e)
	{
		this.lock.lock();
		try
		{
			this.error = e;
			this.shutdown();
			this.waitForFinish.signalAll();
		}
		finally
		{
			this.lock.unlock();
		}
	}


	public final <O> O get() throws Exception
	{
		this.lock.lock();
		try
		{
			if (!Thread.currentThread().getName().equals(this.threadName))
			{
				while (!this.isfinished())
					this.waitForFinish.await();
			}
			else
			{
				try
				{
					while (!this.isfinished())
						this.doJob();
					this.shutdown();
				}
				catch (Exception e)
				{
					this.finishedWithError(e);
				}
			}
			if (this.error != null)
				throw this.error;
			return GenericsToolkit.<O>convertUnchecked(this.result);
		}
		finally
		{
			this.lock.unlock();
		}
	}

}
