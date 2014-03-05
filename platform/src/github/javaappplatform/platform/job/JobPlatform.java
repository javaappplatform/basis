/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.job;

import github.javaappplatform.commons.collection.SmallMap;
import github.javaappplatform.commons.log.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO javadoc
 * @author funsheep
 */
public class JobPlatform
{

	public static final String MAIN_THREAD = "JAP Main Thread";


	private static final Logger LOGGER = Logger.getLogger();


	protected static volatile boolean IS_SHUTDOWN = false;

	private static final ReentrantLock LOCK_ALLJOBS = new ReentrantLock();
	private static final HashSet<JobInfo> SET_ALLJOBS_INFO = new HashSet<>();
	private static final SmallMap<String,DoJobThread> MAP_DOJOBTHREADS = new SmallMap<>();

	protected static final Condition WAIT_FOR_SHUTDOWN = LOCK_ALLJOBS.newCondition();


	private static final Thread CLEANUP_SHUTDOWN_THREAD = new Thread("JobPlatform-Cleanup-Shutdown-Thread")
	{
		@Override
		public void run()
		{
			while (!IS_SHUTDOWN)
			{
				try
				{
					Thread.sleep(3000);
				} catch (InterruptedException e)
				{
					//do nothing
				}
				LOCK_ALLJOBS.lock();
				try
				{
					_cleanup();
					IS_SHUTDOWN |= SET_ALLJOBS_INFO.size() == 0;
				}
				finally
				{
					LOCK_ALLJOBS.unlock();
				}
			}
			this._shutdownAll();

		}

		private void _shutdownAll()
		{
			LOCK_ALLJOBS.lock();
			try
			{
				for (DoJobThread thread : MAP_DOJOBTHREADS.values())
					thread.shutdown();
				for (final JobInfo jobinfo : SET_ALLJOBS_INFO)
				{
					if (!(jobinfo.job instanceof IDoJob))
					{
						Thread th = new Thread()
						{

							@Override
							public void run()
							{
								try
								{
									jobinfo.job.shutdown();
								}
								catch (Exception e)
								{
									LOGGER.info("Shutting down job '" + jobinfo.job.name() + "' caused an exception: ", e);
								}
							}

						};
						th.start();
					}
				}
				WAIT_FOR_SHUTDOWN.signalAll();
			}
			finally
			{
				LOCK_ALLJOBS.unlock();
			}
		}
	};


	public static void registerJob(IJob job)
	{
		if (job instanceof IDoJob)
			LOGGER.warn("The job " + job.name() + " is a IDoJob and should not be registered. Instead he should be added as a Todo.");
		LOCK_ALLJOBS.lock();
		try
		{
			SET_ALLJOBS_INFO.add(new JobInfo(job, 0, false, null));
		}
		finally
		{
			LOCK_ALLJOBS.unlock();
		}
	}

	public static IJob runJob(final Runnable run, String thread)
	{
		IDoJob job = new RunnableJob(run);
		runJob(job, thread);
		return job;
	}

	public static void runJob(IDoJob job, String thread)
	{
		LOCK_ALLJOBS.lock();
		try
		{
			SET_ALLJOBS_INFO.add(new JobInfo(job, 0, false, thread));

			DoJobThread djt = MAP_DOJOBTHREADS.get(thread);
			if (djt == null || !djt.addJob(job))
			{
				djt = new DoJobThread(thread);
				djt.addJob(job);
				MAP_DOJOBTHREADS.put(thread, djt);
			}
		}
		finally
		{
			LOCK_ALLJOBS.unlock();
		}
	}

	public static void scheduleJob(IDoJob job, String thread, long futureMillis)
	{
		LOCK_ALLJOBS.lock();
		try
		{
			SET_ALLJOBS_INFO.add(new JobInfo(job, 0, false, thread));

			DoJobThread djt = MAP_DOJOBTHREADS.get(thread);
			if (djt == null || !djt.scheduleJob(job, futureMillis))
			{
				djt = new DoJobThread(thread);
				djt.scheduleJob(job, futureMillis);
				MAP_DOJOBTHREADS.put(thread, djt);
			}
		}
		finally
		{
			LOCK_ALLJOBS.unlock();
		}
	}

	public static void loopJob(IDoJob job, String thread, long interExecTime)
	{
		LOCK_ALLJOBS.lock();
		try
		{
			SET_ALLJOBS_INFO.add(new JobInfo(job, 0, true, thread));

			DoJobThread djt = MAP_DOJOBTHREADS.get(thread);
			if (djt == null || !djt.loopJob(job, interExecTime))
			{
				djt = new DoJobThread(thread);
				djt.loopJob(job, interExecTime);
				MAP_DOJOBTHREADS.put(thread, djt);
			}
		}
		finally
		{
			LOCK_ALLJOBS.unlock();
		}
	}


	public static Collection<JobInfo> getAllJobs()
	{
		LOCK_ALLJOBS.lock();
		try
		{
			ArrayList<JobInfo> copy = new ArrayList<JobInfo>(SET_ALLJOBS_INFO);
			_cleanup();
			return copy;
		}
		finally
		{
			LOCK_ALLJOBS.unlock();
		}
	}

	public static JobInfo getJobInfo(IJob job)
	{
		LOCK_ALLJOBS.lock();
		try
		{
			for (JobInfo info : SET_ALLJOBS_INFO)
				if (info.job == job)
					return info;
			return null;
		}
		finally
		{
			LOCK_ALLJOBS.unlock();
		}
	}

	public static void removeJob(IJob job)
	{
		LOCK_ALLJOBS.lock();
		try
		{
			Iterator<JobInfo> iter = SET_ALLJOBS_INFO.iterator();
			while (iter.hasNext())
				if (iter.next().job == job)
					iter.remove();

			if (job instanceof IDoJob)
			{
				for (DoJobThread thread : MAP_DOJOBTHREADS.values())
					if (!thread.isShutdown())
						thread.removeJob((IDoJob) job);
			}
		}
		finally
		{
			LOCK_ALLJOBS.unlock();
		}
	}

	private static void _cleanup()
	{
		Iterator<JobInfo> jit = SET_ALLJOBS_INFO.iterator();
		while (jit.hasNext())
		{
			JobInfo info = jit.next();
			if (info.job.isfinished())
				jit.remove();
		}
		Iterator<Map.Entry<String, DoJobThread>> tit = MAP_DOJOBTHREADS.entrySet().iterator();
		while (tit.hasNext())
		{
			Entry<String, DoJobThread> thread = tit.next();
			if (thread.getValue().isShutdown())
				tit.remove();
		}
	}

	public static int jobCount()
	{
		return SET_ALLJOBS_INFO.size();
	}

	public static String getCurrentThread()
	{
		if (MAP_DOJOBTHREADS.containsKey(Thread.currentThread().getName()))
			return Thread.currentThread().getName();
		return null;
	}

	public static void shutdown()
	{
		IS_SHUTDOWN = true;
		CLEANUP_SHUTDOWN_THREAD.interrupt();
	}

	public static void waitForShutdown() throws InterruptedException
	{
		if (IS_SHUTDOWN)
			return;

		CLEANUP_SHUTDOWN_THREAD.start();
		LOCK_ALLJOBS.lock();
		try
		{
			if (!MAP_DOJOBTHREADS.values().contains(Thread.currentThread()))
			{
				while(!IS_SHUTDOWN)
					WAIT_FOR_SHUTDOWN.await();
			}
		}
		finally
		{
			LOCK_ALLJOBS.unlock();
		}
	}

}
