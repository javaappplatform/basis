/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.console;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.extension.ServiceInstantiationException;
import github.javaappplatform.platform.job.JobPlatform;

import java.io.PrintStream;

/**
 * TODO javadoc
 * @author MeisterYeti
 */
public class RunCommand
{
	private static final Logger LOGGER = Logger.getLogger();

	private String name;
	private String[] args;

	public RunCommand(String name)
	{
		this.name = name;
	}

	public static final RunCommand from(String line)
	{
		String[] inputSplit = line.trim().split("\\s+");
		final String[] args = new String[inputSplit.length-1];
		if (args.length > 0)
			System.arraycopy(inputSplit, 1, args, 0, args.length);
		return new RunCommand(inputSplit[0]).with(args);
	}

	public static final RunCommand named(String name)
	{
		return new RunCommand(name);
	}

	public RunCommand with(String[] arguments)
	{
		this.args = arguments;
		return this;
	}

	public void on(final PrintStream out)
	{
		if(out == null || out.checkError())
			return;

		// get command extension
		final Extension cmde = ExtensionRegistry.getExtension(ICommand.EXT_POINT, "command="+this.name);
		if (cmde == null)
		{
			out.println("Command unknown. Type 'help' for a list of commands.");
			return;
		}

		// get command
		Object serv = null;
		try
		{
			serv = cmde.getService();
		}
		catch (ServiceInstantiationException e1)
		{
			//do nothing
		}
		final ICommand cmd = (ICommand) serv;

		// run command
		if (cmde.<Boolean>getProperty("sync").booleanValue())
		{
			JobPlatform.runJob(new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						cmd.run(RunCommand.this.args, out);
						out.flush();
					}
					catch (Exception e)
					{
						LOGGER.severe("Last command caused an exception: ", e);
					}
				}
			}, JobPlatform.MAIN_THREAD);
		}
		else
		{
			try
			{
				cmd.run(this.args, out);
				out.flush();
			}
			catch (Exception e)
			{
				LOGGER.severe("Last command caused an exception: ", e);
			}
		}
	}
}
