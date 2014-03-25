/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.log;

import github.javaappplatform.commons.collection.SmallMap;

import java.util.Map;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;

/**
 * slf4j based logger implementation, that can be used with asserts.
 * @author funsheep
 */
public class Logger
{

	private static final String	CLASSNAME = Logger.class.getName();

	private final org.slf4j.Logger logger;
	private boolean assertsEnabled = false;

	
	static
	{
		try
		{
			LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
			context.reset();
			StatusManager sm = context.getStatusManager();
			if (sm != null)
				sm.add(new InfoStatus("Setting up default configuration.", context));
			
			@SuppressWarnings("unchecked")
			Map<String, String> patternreg = (Map<String, String>) context.getObject(CoreConstants.PATTERN_RULE_REGISTRY);
			if (patternreg == null)
			{
				patternreg = new SmallMap<>();
				context.putObject(CoreConstants.PATTERN_RULE_REGISTRY, patternreg);
			}
			patternreg.put("alias", LoggerRenamer.class.getName());
			ClassRenamer.addRule("github.javaappplatform", "+[jap]");
			ClassRenamer.addRule("github.javaappplatform.commons", "+[com]");
			
			PatternLayoutEncoder pl = new PatternLayoutEncoder();
			pl.setContext(context);
			pl.setPattern("%-5level %-20([%thread]) %-36alias{36} - %msg%n");
//			pl.setPattern("%highlight(%-5level) %red(%-20([%thread])) %cyan(%-36alias{36}) %red(- %msg) %n");
			pl.start();

			ConsoleAppender<ILoggingEvent> ca = new ConsoleAppender<ILoggingEvent>();
			ca.setContext(context);
			ca.setName("console");
			ca.setEncoder(pl);
			ca.setTarget("System.err");
			ca.start();
			ch.qos.logback.classic.Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
			rootLogger.setLevel(Level.TRACE);
			rootLogger.addAppender(ca);
		}
		catch (Exception e)
		{
			LoggerFactory.getLogger(Logger.class).error("Could not setup the default logging rules.", e);
		}
	}


	/**
	 * Constructor.
	 * @param name
	 */
	protected Logger(final String name)
	{
		assert this.assertsEnabled = true;
		this.logger = LoggerFactory.getLogger(name);
	}


	/**
	 * Log on the {@link Level#FINE} level.
	 * @param s
	 * @return true
	 */
	public boolean trace(final String msg, Object... objects)
	{
		this.logger.trace(msg, objects);
		if (!this.assertsEnabled)
			this.logger.warn("The 'trace' option of the logger should be used only with assertions.");
		return true;
	}

	/**
	 * Log on the {@link Level#INFO} level.
	 * @param s
	 * @return true
	 */
	public boolean info(final String format, Object... objects)
	{
		this.logger.info(format, objects);
		return true;
	}

	/**
	 * Log on the {@link Level#WARNING} level.
	 * @param s
	 * @return true
	 */
	public boolean warn(final String format, Object...arguments)
	{
		this.logger.warn(format, arguments);
		return true;
	}

	/**
	 * Log on the {@link Level#WARNING} level.
	 * @param s
	 * @param e
	 * @return true
	 */
	public boolean warn(final String msg, final Throwable t)
	{
		this.logger.warn(msg, t);
		return true;
	}

	/**
	 * Log on the {@link Level#SEVERE} level. Stringsequences like '{X}' (where X is a number greater than null) are replaced by the objects string representation of
	 * the object at index X in the given array.
	 * @param s
	 * @param e
	 * @return true
	 */
	public boolean severe(final String format, Object... arguments)
	{
		this.logger.error(format, arguments);
		return true;
	}

	/**
	 * Log on the {@link Level#SEVERE} level.
	 * @param s
	 * @param e
	 * @return true
	 */
	public boolean severe(final String msg, final Throwable t)
	{
		this.logger.error(msg, t);
		return true;
	}

	/**
	 * Log on the {@link Level#FINE} level.
	 * @param s
	 * @param e
	 * @return true
	 */
	public boolean debug(final String msg, final Throwable t)
	{
		this.logger.debug(msg, t);
		return true;
	}

	/**
	 * Log on the {@link Level#FINE} level.
	 * @param s
	 * @param e
	 * @return true
	 */
	public boolean debug(final String format, Object... arguments)
	{
		this.logger.debug(format, arguments);
		return true;
	}

	/**
	 * Returns the calling class' name.
	 *
	 * @return the calling class' name.
	 * @see #getStackItem()
	 */
	protected static String getClassName()
	{
		final StackTraceElement st = getStackItem();
		return st.getClassName();
	}

	/**
	 * Returns the calling class' packagename.
	 *
	 * @return the calling class' packagename.
	 * @see #getClassName
	 */
	protected static String getPackageName()
	{
		final String s = getClassName();
		final int i = s.lastIndexOf('.');
		return i < 0 ? s : s.substring(0, i);
	}

	/**
	 * Returns the first stack element which is not part of the Logger hierarchie.
	 * @return the first stack element which is not part of the Logger hierarchie
	 */
	private static StackTraceElement getStackItem()
	{
		final StackTraceElement[] st = new Exception().getStackTrace();

		int i = 1;
		while (i < st.length)
		{
			final String n = st[i].getClassName();
			if (CLASSNAME.equals(n))
				break;
			i++;
		}
		i++; // skip first occurence of class
		while (i < st.length)
		{
			final String n = st[i].getClassName();
			if (!CLASSNAME.equals(n))
				return st[i];
			i++;
		}

		throw new RuntimeException("the roof is on fire");
	}

	/**
	 * Returns a new logger instance. The logger automatically detects the name of the calling
	 * class.
	 *
	 * @return a new logger instance
	 */
	public static Logger getLogger()
	{
		return new Logger(getClassName());
	}

	/**
	 * Returns a new logger instance. The logger automatically detects it's calling class'
	 * packagename.
	 *
	 * @return a new logger instance
	 */
	public static Logger getPackageLogger()
	{
		return new Logger(getPackageName());
	}

	/**
	 * Returns a new logger instance.
	 *
	 * @param c class to obtain the name from
	 * @return a new logger instance.
	 */
	public static <T> Logger getLogger(final Class<T> c)
	{
		return new Logger(c.getName());
	}

	/**
	 * Returns a new logger instance.
	 *
	 * @param c class to obtain it's package name from
	 * @return a new logger instance.
	 */
	public static <T> Logger getPackageLogger(final Class<T> c)
	{
		return new Logger(c.getPackage().getName());
	}

}
