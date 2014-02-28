/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package de.d3fact.platform.resource2.server;

import github.javaappplatform.commons.util.Close;
import github.javaappplatform.platform.PlatformException;
import github.javaappplatform.platform.boot.IBootEntry;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.job.ADoJob;
import github.javaappplatform.platform.job.IDoJob;
import github.javaappplatform.platform.job.JobPlatform;
import github.javaappplatform.platform.resource.Resource;
import github.javaappplatform.platform.resource.Stream;
import github.javaappplatform.platform.resource.IResourceAPI.OpenOption;

import java.io.IOException;
import java.io.OutputStream;

/**
 * TODO javadoc
 * @author funsheep
 */
public class ServerFileSystemDemo extends ADoJob implements IBootEntry, IDoJob
{

	private OutputStream stream;


	/**
	 * @param name
	 */
	public ServerFileSystemDemo()
	{
		super("ServerFileWriterDemo");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startup(Extension e) throws PlatformException
	{
		try
		{
			this.stream = Stream.open(Resource.at("ws:/platform/debug/Test.txt")).with(OpenOption.CREATE_IN_ANY_CASE).toWrite();
			this.schedule(JobPlatform.MAIN_THREAD, true, 1000);
		} catch (IOException e1)
		{
			throw new PlatformException(e1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		super.shutdown();
		Close.close(this.stream);
	}

	private int count = 0;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doJob()
	{
		try
		{
			this.stream.write(String.valueOf(this.count++).getBytes());
			this.stream.write('*');
		}
		catch (IOException e)
		{
			e.printStackTrace();
			this.shutdown();
		}
	}

}
