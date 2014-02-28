/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.log;

import github.javaappplatform.commons.util.Close;
import github.javaappplatform.commons.util.GenericsToolkit;
import github.javaappplatform.commons.util.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;

/**
 * This tool set loads the logging configuration for the d3fact logging framework.
 * @author funsheep
 */
public class LoggerTools
{

	private static final String COMMON_RES = "de/d3fact/common";
	private static final Logger LOGGER = Logger.getLogger();

	/**
	 * Loads the configuration from the standard package. The standard package is <code>github.javaappplatform.commons.platform.log</code> this package must
	 * be accessible by the classloader (therefore it has to be in the classpath). In this package there must be a logger configuration file
	 * named <code>logger.properties</code> and a smartformatter configuration <code>logger-alias.properties</code>.
	 */
	public static final void loadConfig()
	{
		if (!Resources.packageExists(COMMON_RES))
			Resources.addPackage(COMMON_RES, COMMON_RES);
		loadConfig(Resources.getResource(COMMON_RES, "platform/log/logger.properties"), Resources.getResource(COMMON_RES, "platform/log/logger-alias.properties"));
	}

	/**
	 * Loads the logger and the smartformatter configuration from the given urls.
	 * @param loggerProperties The configuration for the d3fact logger.
	 * @param aliasProperties The configuration for the d3fact smartformatter.
	 */
	public static final void loadConfig(URL loggerProperties, URL aliasProperties)
	{
			InputStream inLogger = null;
			try
			{
				inLogger = loggerProperties.openStream();

				Logger.loadConfiguration(inLogger);
				LOGGER.log(Level.INFO, "Logger configuration loaded, source was {0}", loggerProperties.toString());

			}
			catch (IOException ex)
			{
				LOGGER.log(Level.WARNING, "Logger Configuration can not be read.", ex);
			}
			catch (SecurityException ex)
			{
				LOGGER.log(Level.WARNING, "Changing Logger Configuration denied.", ex);
			} finally
			{
				Close.close(inLogger);
			}

			InputStream inAlias = null;
			try
			{
				inAlias = aliasProperties.openStream();

				Properties p = new Properties();
				p.load(inAlias);
				ClassRenamer.addRuleset(GenericsToolkit.convert(p));
				LOGGER.log(Level.INFO, "Classname aliasses loaded, source was {0}", aliasProperties.toString());
			} catch (IOException ex)
			{
				LOGGER.log(Level.WARNING, "Could not load classname aliasses", ex);
			} finally
			{
				Close.close(inAlias);
			}

	}


	private LoggerTools()
	{
		//no instance
	}

}
