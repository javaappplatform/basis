/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.job;

import github.javaappplatform.platform.Platform;

import java.text.NumberFormat;

/**
 * TODO javadoc
 * @author funsheep
 */
public final class JobInfo
{

	public final IJob job;
	public final long executeTime;
	public final boolean loop;
	public final String thread;


	/**
	 *
	 */
	JobInfo(IJob job, long execIn, boolean loop, String thread)
	{
		this.job = job;
		this.executeTime = execIn;
		this.loop = loop;
		this.thread = thread;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(20);
		sb.append(this.loop ? '\u21BB' : ' ');
		sb.append(this.job.name());
		sb.append(" \t");
		final long exec = this.executeTime - Platform.currentTime();
		if (exec > 0)
		{
			sb.append(" >");
			sb.append(exec);
		}
		else
		{
			sb.append('[');
			if (this.job.isfinished())
			{
				sb.append("100%");
			}
			else if (this.job.length() != IJob.LENGTH_UNKNOWN && this.job.length() > 0 && this.job.absoluteProgress() != IJob.PROGRESS_UNKNOWN)
			{
				NumberFormat nf = NumberFormat.getPercentInstance();
				sb.append(nf.format((float) this.job.absoluteProgress() / this.job.length()));
			}
			else if (this.job.absoluteProgress() != IJob.PROGRESS_UNKNOWN)
				sb.append(this.job.absoluteProgress());
			else
				sb.append('!');
			sb.append(']');
		}
		if (this.thread != null)
		{
			sb.append(" \tin '");
			sb.append(this.thread);
			sb.append('\'');
		}
		return sb.toString();
	}

}
