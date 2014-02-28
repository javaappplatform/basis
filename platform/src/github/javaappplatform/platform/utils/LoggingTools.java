/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.utils;

import github.javaappplatform.commons.collection.SmallMap;
import github.javaappplatform.commons.log.ClassRenamer;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.Platform;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.LogManager;

/**
 * TODO javadoc
 * @author funsheep
 */
public class LoggingTools
{

	private static final Logger LOGGER = Logger.getLogger();


	public static final void configureAliases()
	{
		SmallMap<String, String> aliases = new SmallMap<String, String>(15);

		Collection<Extension> set = ExtensionRegistry.getExtensions("github.javaappplatform.platform.logging.Alias");
		for (Extension e : set)
			aliases.put(e.getProperty("package").toString(), e.getProperty("substitute").toString());

		ClassRenamer.addRuleset(aliases);
	}


	public static final void configureLoglevel()
	{
		configureLogLevel(Platform.hasOption("loglevel") ? Platform.getOptionValue("loglevel") : "WARNING");
	}

	public static final void configureLogLevel(String lvl)
	{
		SmallMap<String, String> config = new SmallMap<String, String>(10);
		config.put(".level", "WARNING");

		if ("NONE".equals(lvl))
		{
			lvl = "OFF";
			config.put(".level", "OFF");
		}
		else if ("ERROR".equals(lvl))
		{
			lvl = "SEVERE";
		}
		config.put("github.javaappplatform.platform.level", lvl);

		if (!Platform.hasOption("logfile"))
		{
			config.put("handlers", "java.util.logging.ConsoleHandler");
			config.put("java.util.logging.ConsoleHandler.formatter", "github.javaappplatform.platform.commons.platform.log.SmartFormatter");
			config.put("java.util.logging.ConsoleHandler.level", "ALL");
		}
		else
		{
			config.put("handlers", "java.util.logging.FileHandler");
			config.put("java.util.logging.FileHandler.pattern", Platform.getOptionValue("logfile"));
			config.put("java.util.logging.FileHandler.count", "2"); 			//2 rotating logfiles
			config.put("java.util.logging.FileHandler.limit", "10485760"); 		//10MB per file
			config.put("java.util.logging.FileHandler.formatter", "github.javaappplatform.platform.commons.platform.log.SmartFormatter");
			config.put("java.util.logging.FileHandler.level", "ALL");
		}

		try
		{
			LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(stringLogConfig(config).getBytes("UTF-8")));
		}
		catch (IOException e)
		{
			throw new RuntimeException("Should not happen", e);
		}
	}

	private static final String stringLogConfig(Map<String, String> props)
	{
		StringBuilder sb = new StringBuilder(props.size()*30);
		for (Map.Entry<String, String> e : props.entrySet())
		{
			sb.append(e.getKey());
			sb.append('=');
			sb.append(e.getValue());
			sb.append('\n');
		}
		final String logconf = sb.toString();
		LOGGER.fine("Configuring the logger with the following options:\n" + logconf);
		return logconf;
	}


	private LoggingTools()
	{
		//no instance
	}

}
