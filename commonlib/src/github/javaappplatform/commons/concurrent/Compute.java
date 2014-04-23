package github.javaappplatform.commons.concurrent;

import github.javaappplatform.commons.util.GenericsToolkit;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Compute
{
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition waitForResult = this.lock.newCondition();
	private boolean resulted = false;
	private Object result = null;
	
	
	public void put(Object result)
	{
		this.lock.lock();
		try
		{
			this.result = result;
			this.resulted = true;
			this.waitForResult.signalAll();
		}
		finally
		{
			this.lock.unlock();
		}
	}
	
	public void error(Exception e)
	{
		this.put(e);
	}

	public <O extends Object> O poll() throws Exception
	{
		return this.get(-1);
	}
	
	public <O extends Object> O get() throws Exception
	{
		return this.get(0);
	}
	
	public <O extends Object> O get(int waitMillis) throws Exception
	{
		this.lock.lock();
		try
		{
			if (waitMillis < 0 && !this.resulted)
				return null;
				
			while (!this.resulted)
			{
				if (waitMillis > 0 && !this.waitForResult.await(waitMillis, TimeUnit.MILLISECONDS))
					throw new TimeoutException();
				else if (waitMillis == 0)
					this.waitForResult.await();
			}
			if (this.result instanceof Exception)
				throw (Exception) this.result;
			return GenericsToolkit.convertUnchecked(this.result);
		}
		finally
		{
			this.lock.unlock();
		}
	}
}