/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.console;

import github.javaappplatform.platform.job.JobInfo;
import github.javaappplatform.platform.job.JobPlatform;

import java.io.PrintStream;
import java.util.Collection;

/**
 * TODO javadoc
 * @author funsheep
 */
public class JobCommand implements ICommand
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(String[] args, PrintStream out)
	{
		if (args.length == 0 || "list".equals(args[0]))
		{
			out.println("Currently running jobs:");
			Collection<JobInfo> jobs = JobPlatform.getAllJobs();
			for (JobInfo info : jobs)
			{
				out.println(info);
			}
		}
		else if (args.length > 1 && "shutdown".equals(args[0]))
		{
			StringBuilder sb = new StringBuilder(15);
			for (int i = 1; i < args.length-1; i++)
			{
				sb.append(args[i]);
				sb.append(' ');
			}
			sb.append(args[args.length-1]);
			final String name = sb.toString();
			Collection<JobInfo> jobs = JobPlatform.getAllJobs();
			for (JobInfo job : jobs)
			{
				if (job.job.name().equals(name))
					job.job.shutdown();
			}
		}
	}

}
