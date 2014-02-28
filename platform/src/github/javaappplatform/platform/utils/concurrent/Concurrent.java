/*
	This file is part of the d3fact common library.
	Copyright (C) 2005-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.utils.concurrent;

import github.javaappplatform.commons.util.GenericsToolkit;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO javadoc
 * @author funsheep
 */
public class Concurrent
{

	public static final long DEFAULT_TIMEOUT = 10 * 1000;


	private final ReentrantLock lock;
	private final Condition waitingForResult;
	private Object result = null;


	public Concurrent()
	{
		this(new ReentrantLock());
	}

	public Concurrent(ReentrantLock lock)
	{
		this.lock = lock;
		this.waitingForResult = this.lock.newCondition();
	}


	public void pushResult(Object _result)
	{
		if (_result == null)
			throw new IllegalArgumentException("No null elements allowed.");
		this.lock.lock();
		try
		{
			this.result = _result;
			this.waitingForResult.signal();
		}
		finally
		{
			this.lock.unlock();
		}
	}

	public <O extends Object> O retrieveResult() throws TimeoutException, ExecutionException
	{
		return this.retrieveResult(DEFAULT_TIMEOUT);
	}

	public <O extends Object> O retrieveResult(long timeout) throws TimeoutException, ExecutionException
	{
		this.lock.lock();
		try
		{
			Exception error = null;
			while (this.result == null && error == null)
				try
				{
					if (timeout <= 0)
						this.waitingForResult.await();
					else if (!this.waitingForResult.await(timeout, TimeUnit.MILLISECONDS))
						error = new TimeoutException();
				} catch (InterruptedException e)
				{
					error = new ExecutionException(e);
				}

			if (error != null)
			{
				if (error instanceof ExecutionException)
					throw (ExecutionException) error;
				throw (TimeoutException) error;
			}
			if (this.result instanceof Exception)
				throw new ExecutionException((Exception) this.result);
			return GenericsToolkit.<O>convertUnchecked(this.result);
		}
		finally
		{
			this.result = null;
			this.lock.unlock();
		}
	}

}
