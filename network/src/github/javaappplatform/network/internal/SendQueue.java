package github.javaappplatform.network.internal;

import github.javaappplatform.network.INetworkAPI;

import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO javadoc
 * @author funsheep
 */
public class SendQueue
{

	private final int maxcapacity;
	private final ArrayDeque<Message> queue;
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition isEmpty = this.lock.newCondition();
	private final Condition isFull_Normal = this.lock.newCondition();
	private final Condition isFull_VIP = this.lock.newCondition();


	/**
	 * 
	 */
	public SendQueue(int maxcapacity)
	{
		this.maxcapacity = maxcapacity;
		this.queue = new ArrayDeque<>(this.maxcapacity);
	}

	
	public Message take()
	{
		return this.poll(INetworkAPI.CONNECTION_TIMEOUT);
	}

	public Message poll()
	{
		return this.poll(0);
	}
	
	private Message poll(int wait)
	{
		this.lock.lock();
		try
		{
			if (wait == 0 && this.queue.isEmpty())
				return null;
			
			while (this.queue.isEmpty())
				if (!this.isEmpty.await(wait, TimeUnit.MILLISECONDS))
					return null;
			
			Message msg = this.queue.removeFirst();
			if (this.lock.hasWaiters(this.isFull_VIP))
				this.isFull_VIP.signalAll();
			else
				this.isFull_Normal.signalAll();
			return msg;
		} catch (InterruptedException e)
		{
			return null;
		}
		finally
		{
			this.lock.unlock();
		}
	}

	public void put(Message msg) throws InterruptedException
	{
		this.lock.lock();
		try
		{
			while (this.queue.size() == this.maxcapacity)
				if (!this.isFull_Normal.await(INetworkAPI.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS))
					throw new InterruptedException("Timeout");
			
			this.queue.add(msg);
			this.isEmpty.signalAll();
		}
		finally
		{
			this.lock.unlock();
		}
	}

	public void priorityPut(Message msg) throws InterruptedException
	{
		this.lock.lock();
		try
		{
			while (this.queue.size() == this.maxcapacity)
				if (!this.isFull_VIP.await(InternalNetTools.TIMEOUT, TimeUnit.MILLISECONDS))
					throw new InterruptedException("Timeout");
			
			this.queue.add(msg);
			this.isEmpty.signalAll();
		}
		finally
		{
			this.lock.unlock();
		}
	}

	public int size()
	{
		return this.queue.size();
	}
}
