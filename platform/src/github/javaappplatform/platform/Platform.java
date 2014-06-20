/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform;

import github.javaappplatform.commons.collection.SmallMap;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Arrays2;
import github.javaappplatform.platform.boot.Bootup;
import github.javaappplatform.platform.boot.IBootEntry;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionLoader;
import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.extension.ServiceInstantiationException;
import github.javaappplatform.platform.job.JobPlatform;
import github.javaappplatform.platform.time.ITimeService;
import github.javaappplatform.platform.utils.CmdLineTools;
import github.javaappplatform.platform.utils.ConfigFileTools;
import github.javaappplatform.platform.utils.OptionTools;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ModHelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

/**
 * TODO javadoc
 * @author funsheep
 */
public class Platform
{

	private static final Logger LOGGER = Logger.getLogger();

	public enum State
	{
		NOT_INITIALIZED,
		RUNNING,
		SHUTDOWN
	}


	private static State STATE = State.NOT_INITIALIZED;
	private static final SmallMap<String, Object> OPTIONS = new SmallMap<String, Object>();


	public static final boolean hasOption(String name)
	{
		return OPTIONS.containsKey(name);
	}

	public static final String getOptionValue(String name)
	{
		if (!hasOption(name))
			return null;
		return String.valueOf(OPTIONS.get(name));
	}

	public static final String getOptionValue(String name, String deFault)
	{
		if (!hasOption(name))
			return deFault;
		try
		{
			return getOptionValue(name);
		}
		catch (Exception ex)
		{
			LOGGER.debug("Could not convert option " + name + " with value " +  getOptionValue(name) + " properly into an int.", ex);
		}
		return deFault;
	}

	public static final int getOptionValue(String name, int deFault)
	{
		if (!hasOption(name))
			return deFault;
		try
		{
			return Integer.parseInt(getOptionValue(name));
		}
		catch (Exception ex)
		{
			LOGGER.debug("Could not convert option " + name + " with value " +  getOptionValue(name) + " properly into an int.", ex);
		}
		return deFault;
	}

	public static final double getOptionValue(String name, double deFault)
	{
		if (!hasOption(name))
			return deFault;
		try
		{
			return Double.parseDouble(getOptionValue(name));
		}
		catch (Exception ex)
		{
			LOGGER.debug("Could not convert option " + name + " with value " +  getOptionValue(name) + " properly into an double.", ex);
		}
		return deFault;
	}

	public static final String[] getOptionValues(String name)
	{
		Object o = OPTIONS.get(name);
		if (o instanceof String)
			return new String[] { o.toString() };
		return (String[]) o;
	}

	public static final void setOption(String name, String value)
	{
		OPTIONS.put(name, value);
	}

	public static final void setOption(String name, String[] value)
	{
		OPTIONS.put(name, value);
	}


	public static final State state()
	{
		return STATE;
	}

	public static final long currentTime()
	{
		Extension e = ExtensionRegistry.getExtension(ITimeService.EXT_POINT);
		if (e == null)
			return System.currentTimeMillis();
		try
		{
			return e.<ITimeService>getService().currentTime();
		} catch (ServiceInstantiationException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public static final void boot() throws PlatformException
	{
		if (STATE != State.NOT_INITIALIZED)
			return;


		Bootup bootjob = new Bootup();
		JobPlatform.runJob(bootjob, JobPlatform.MAIN_THREAD);
		try
		{
			bootjob.get();
		} catch (Exception e)
		{
			throw new PlatformException("Could not probably boot up.", e);
		}

		STATE = State.RUNNING;

		LOGGER.info("Platform started");
	}

	private static final void parseOptions(String[] args) throws PlatformException, IOException
	{
		LOGGER.info("Options: {}", Arrays.toString(args));
		final String configpath = CmdLineTools.getValue("config", args);
		if (configpath != null)
			args = ConfigFileTools.read(configpath);

		if (Arrays2.indexOf(args, "-help") != -1)
		{
			ModHelpFormatter formatter = new ModHelpFormatter();
			formatter.printHelp("Platform", OptionTools.getOptions(), true);
			throw new PlatformException();
		}

		ExtensionLoader.loadExtensionsFromArgs(args);

		try
		{
			CommandLine line = OptionTools.parseOptions(args);
			for (Option o : line.getOptions())
			{
				String name = o.getOpt();
				String[] value = o.getValues();
				if (value == null || o.hasArgs())
					OPTIONS.put(name, value);
				else
					OPTIONS.put(name, value[0]);
			}
		}
		catch (ParseException e)
		{
			ModHelpFormatter formatter = new ModHelpFormatter();
			formatter.printHelp("Platform", OptionTools.getOptions(), true);
			throw new PlatformException(e);
		}
	}

	public static final void shutdown()
	{
		shutdown(0);
	}

	public static final void shutdown(final int millis)
	{
		if (STATE != State.RUNNING)
			return;

		JobPlatform.shutdown();

		Thread monitor = null;
		if (millis > 0)
		{
			monitor = new Thread(new Runnable()
			{

				/**
				 * {@inheritDoc}
				 */
				@Override
				public void run()
				{
					try
					{
						Thread.sleep(millis);
						System.exit(1);
					}
					catch (InterruptedException e)
					{
						//do nothing - we expect that.
					}
				}
			});
			monitor.start();
		}
		Set<Extension> bootEntries = ExtensionRegistry.getExtensions("github.javaappplatform.platform.boot");
		for (Extension entry : bootEntries)
		{
			try
			{
				LOGGER.debug("Trying to shut down entry: {}", entry.name);
				entry.<IBootEntry>getService().shutdown();
			}
			catch (ServiceInstantiationException e)
			{
				LOGGER.warn("Could not propably shut down down " + entry.name, e);
			}
			catch (PlatformException e)
			{
				LOGGER.warn("Could not propably shut down down " + entry.name, e);
			}
		}
		STATE = State.SHUTDOWN;
		LOGGER.info("Platform shut down.");
		if (monitor != null)
			monitor.interrupt();
		System.exit(0);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			parseOptions(args);
			try
			{
				boot();
				JobPlatform.waitForShutdown();
			} catch (Exception e)
			{
				LOGGER.severe("Exception catched. Doing a hard shutdown. ", e);
				shutdown();
			}
		}
		catch (PlatformException e1)
		{
			//do nothing die quietly
		}
		catch (IOException e1)
		{
			LOGGER.severe("Exception catched. Doing a hard shutdown. ", e1);
		}
	}

}
