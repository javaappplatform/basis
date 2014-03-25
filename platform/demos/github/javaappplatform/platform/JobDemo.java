/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform;

import github.javaappplatform.platform.Platform;
import github.javaappplatform.platform.job.ADoJob;
import github.javaappplatform.platform.job.JobInfo;
import github.javaappplatform.platform.job.JobPlatform;

/**
 * TODO javadoc
 * @author funsheep
 */
public class JobDemo
{


	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		final ADoJob runjob = new ADoJob("Runjob")
		{

			@Override
			public void doJob()
			{
				try
				{
					Thread.sleep(100);
					System.out.println(this.name());
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		runjob.schedule(JobPlatform.MAIN_THREAD, false, 0);

		JobPlatform.loopJob(new ADoJob("Loopjob")
		{
			private final long start = Platform.currentTime();

			@Override
			public void doJob()
			{
				System.out.println(this.name() + " executed at: " + (Platform.currentTime() - this.start));
			}
		}, JobPlatform.MAIN_THREAD, 1000);

		JobPlatform.scheduleJob(new ADoJob("Removejob")
		{

			private final long start = Platform.currentTime();

			@Override
			public void doJob()
			{
				System.out.println(this.name() + " executed at: " + (Platform.currentTime() - this.start));
				JobPlatform.removeJob(runjob);
				this.shutdown = true;
			}
		}, JobPlatform.MAIN_THREAD, 4000);

		JobPlatform.scheduleJob(new ADoJob("Schedulejob")
		{

			private final long start = Platform.currentTime();

			@Override
			public void doJob()
			{
				System.out.println(this.name() + " executed at: " + (Platform.currentTime() - this.start));
				this.shutdown = true;

				for (JobInfo info : JobPlatform.getAllJobs())
					System.out.println(info);
			}
		}, JobPlatform.MAIN_THREAD, 5000);

		for (JobInfo info : JobPlatform.getAllJobs())
			System.out.println(info);

		JobPlatform.waitForShutdown();
	}

}
