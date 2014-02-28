/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.boot;

import github.javaappplatform.platform.PlatformException;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.job.IDoJob;
import github.javaappplatform.platform.job.JobPlatform;

import java.util.Set;

/**
 * TODO javadoc
 * @author funsheep
 */
public class InitialJobStart implements IBootEntry
{

	public static final String EXT_POINT = "github.javaappplatform.platform.boot.job";


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startup(Extension e) throws PlatformException
	{
		final Set<Extension> jobs = ExtensionRegistry.getExtensions(EXT_POINT);
		for (Extension jo : jobs)
		{
			try
			{
				final long delay = jo.hasProperty("delay") ? jo.<Integer>getProperty("delay").longValue() : 0;
				final boolean loop  = jo.hasProperty("loop") ? jo.<Boolean>getProperty("loop").booleanValue() : false;
				final String thread = jo.hasProperty("thread") ? String.valueOf(jo.getProperty("thread")) : JobPlatform.MAIN_THREAD;

				if (loop)
					JobPlatform.loopJob(jo.<IDoJob>getService(), thread, delay);
				else if (delay > 0)
					JobPlatform.scheduleJob(jo.<IDoJob>getService(), thread, delay);
				else
					JobPlatform.runJob(jo.<IDoJob>getService(), thread);
			}
			catch (Exception ex)
			{
				throw new PlatformException("Could not start job " + jo.name, ex);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown() throws PlatformException
	{
		//do nothing
	}

}
