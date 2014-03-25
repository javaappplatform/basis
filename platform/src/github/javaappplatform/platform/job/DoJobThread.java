/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.job;

import github.javaappplatform.commons.collection.SmallSet;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.Platform;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO javadoc
 * @author funsheep
 */
final class DoJobThread extends Thread
{

	private static final Logger LOGGER = Logger.getLogger();



	private static class Entry implements Comparable<Entry>
	{
		private static volatile long DECIPHER = Long.MIN_VALUE;


		public static final long NO_LOOP = -1;

		public long time;
		private long _decipher;
		public IDoJob job;
		public long loop;

		/**
		 * @param time The execution time of this entry (event).
		 * @param event The event which has to be scheduled.
		 * @param listener The listener to call.
		 * @param loop The information if the given entry encapsulates a loop event.
		 */
		public Entry(IDoJob job, long time)
		{
			this(job, time, NO_LOOP);
		}

		/**
		 * @param time The execution time of this entry (event).
		 * @param event The event which has to be scheduled.
		 * @param listener The listener to call.
		 * @param loop The information if the given entry encapsulates a loop event.
		 */
		public Entry(IDoJob job, long time, long loop)
		{
			this.job  = job;
			this.time = time;
			this.loop = loop;
			this._decipher = DECIPHER++;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compareTo(Entry o)
		{
			final long t = this.time - o.time;
			if (t < 0)
				return -1;
			if (t > 0)
				return +1;

			final long d = this._decipher - o._decipher;
			if (d < 0)
				return -1;
			if (d > 0)
				return +1;
			return 0;
		}

	}


	private final ReentrantLock addLock = new ReentrantLock();
	private final Condition threadIsEmpty = this.addLock.newCondition();
	private final ReentrantLock removeLock = new ReentrantLock();

	private final SmallSet<IDoJob> add = new SmallSet<>(1);
	private final SmallSet<IDoJob> remove = new SmallSet<>(1);
	private final SmallSet<IDoJob> pool = new SmallSet<>(2);
	private final SmallSet<Entry> addQueue = new SmallSet<>(1);
	private final PriorityQueue<Entry> queue = new PriorityQueue<>(2);


	private boolean isShutdown = false;


	/**
	 * @param name
	 */
	public DoJobThread(String name)
	{
		super(name);
		this.setDaemon(true);
		this.start();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run()
	{
		while(!this.isShutdown)
		{

			//handle "everything is empty"
			this.addLock.lock();
			try
			{
				//sleep if there is nothing to do
				while (this.pool.size() + this.queue.size() + this.add.size() + this.addQueue.size() == 0)
				{
					if (this.threadIsEmpty.awaitNanos(TimeUnit.MINUTES.toNanos(2)) <= 0 || this.isShutdown)
					{
						this.isShutdown = true;
						this.pool.clear();
						return;
					}
				}
				this.pool.addAll(this.add);
				this.add.clear();
				this.queue.addAll(this.addQueue);
				this.addQueue.clear();
			} catch (InterruptedException e)
			{
				this.isShutdown = true;
				this.pool.clear();
				this.queue.clear();
				return;
			}
			finally
			{
				this.addLock.unlock();
			}

			//remove jobs on request
			this.removeLock.lock();
			try
			{
				this.pool.removeAll(this.remove);
				Iterator<Entry> iter = this.queue.iterator();
				while (iter.hasNext())
					if (this.remove.contains(iter.next().job))
						iter.remove();
				this.remove.clear();
			}
			finally
			{
				this.removeLock.unlock();
			}

			//first handle normal jobs
			Iterator<IDoJob> poolIT = this.pool.iterator();
			while (poolIT.hasNext())
			{
				IDoJob job = poolIT.next();
				if (!job.isfinished())
					try
					{
						job.doJob();
					}
					catch (Exception e)
					{
						LOGGER.severe("The job " + job.name() + " threw an exception and has been subsequently removed.", e);
						poolIT.remove();
						this._shutdownJob(job);
					}
				else
					poolIT.remove();
			}

			if (!this.queue.isEmpty())
			{
				final long ctime = Platform.currentTime();
				//first work on jobs that are overdue
				Iterator<Entry> queueIT = this.queue.iterator();
				while (queueIT.hasNext())
				{
					Entry entry = queueIT.next();
					if (entry.time > ctime)
						break;
					queueIT.remove();
					if (!entry.job.isfinished())
					{
						try
						{

							if (entry.loop != Entry.NO_LOOP)
							{
								entry.job.doJob();
								entry.time = Math.max(entry.time+entry.loop, ctime);
								entry._decipher = Entry.DECIPHER++;
								this.addQueue.add(entry);
							}
							else
								this.addJob(entry.job);
						}
						catch (Exception e)
						{
							LOGGER.severe("The job " + entry.job.name() + " threw an exception and has been subsequently removed.", e);
							this._shutdownJob(entry.job);
						}
					}
				}
			}
			this.addLock.lock();
			try
			{
				long time = 0;
				//we need to sleep when there are jobs in the queue that should not be worked on yet, but there are no jobs in the pool
				while (this.pool.size() + this.add.size() + this.addQueue.size() == 0 && !this.queue.isEmpty() && (time = this.queue.peek().time - Platform.currentTime()) > 5)
				{
					this.threadIsEmpty.await(time, TimeUnit.MILLISECONDS);
				}
			} catch (InterruptedException e)
			{
				//do nothing just check the conditions above
			}
			finally
			{
				this.addLock.unlock();
			}
		}
	}


	private void _shutdownJob(final IDoJob job)
	{
		Thread th = new Thread()
		{

			@Override
			public void run()
			{
				try
				{
					job.shutdown();
				}
				catch (Exception e)
				{
					LOGGER.info("Shutting down job '{}' caused an exception: {}", job.name(), e);
				}
			}

		};
		th.start();
		this.removeJob(job);
	}


	public boolean addJob(IDoJob job)
	{
		this.addLock.lock();
		try
		{
			if (this.isShutdown)
				return false;

			this.add.add(job);
			this.threadIsEmpty.signal();
			return true;
		}
		finally
		{
			this.addLock.unlock();
		}
	}

	public boolean scheduleJob(IDoJob job, long delay)
	{
		this.addLock.lock();
		try
		{
			if (this.isShutdown)
				return false;

			this.addQueue.add(new Entry(job, Platform.currentTime()+delay));
			this.threadIsEmpty.signal();
			return true;
		}
		finally
		{
			this.addLock.unlock();
		}
	}

	public boolean loopJob(IDoJob job, long interExecTime)
	{
		this.addLock.lock();
		try
		{
			if (this.isShutdown)
				return false;

			this.addQueue.add(new Entry(job, Platform.currentTime(), interExecTime));
			this.threadIsEmpty.signal();
			return true;
		}
		finally
		{
			this.addLock.unlock();
		}
	}

	public void removeJob(IDoJob job)
	{
		this.removeLock.lock();
		try
		{
			this.remove.add(job);
		}
		finally
		{
			this.removeLock.unlock();
		}
	}

	public void shutdown()
	{
		this.addLock.lock();
		try
		{
			this.isShutdown = true;
			for (IDoJob job : this.pool)
				this._shutdownJob(job);
			for (Entry entry : this.queue)
				this._shutdownJob(entry.job);
			this.pool.clear();
			this.queue.clear();
			this.threadIsEmpty.signal();
		}
		finally
		{
			this.addLock.unlock();
		}
	}

	public boolean isShutdown()
	{
		this.addLock.lock();
		try
		{
			return this.isShutdown;
		}
		finally
		{
			this.addLock.unlock();
		}
	}

}
