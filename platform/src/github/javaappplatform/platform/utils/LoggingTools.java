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
import github.javaappplatform.commons.log.LoggerRenamer;
import github.javaappplatform.commons.util.Arrays2;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RollingPolicyBase;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.TriggeringPolicyBase;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * TODO javadoc
 * @author funsheep
 */
public class LoggingTools
{
	
	public static final String EP_ALIAS = "github.javaappplatform.platform.logging.Alias";
	public static final String EP_LOGCONFIG = "github.javaappplatform.platform.logging.Config";

	private static final Logger LOGGER = Logger.getLogger();
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'_'Hmmss");

	public static final void configureAliases()
	{
		SmallMap<String, String> aliases = new SmallMap<String, String>(15);

		Collection<Extension> set = ExtensionRegistry.getExtensions(EP_ALIAS);
		for (Extension e : set)
			aliases.put(e.getProperty("package").toString(), e.getProperty("substitute").toString());

		ClassRenamer.addRuleset(aliases);
	}


	public static final void configureLogging()
	{
		Collection<Extension> exts = ExtensionRegistry.getExtensions(EP_LOGCONFIG);
		if (exts.size() == 0)
		{
			LOGGER.severe("Could not find a logging configuration. Default configuration is used.");
			return;
		}
		if (exts.size() > 1)
			LOGGER.warn("Found several logging configurations. Will use " + exts.iterator().next().name);
		configureLogging(exts.iterator().next());
	}

	public static final void configureLogging(String logConfig)
	{
		Extension e = ExtensionRegistry.getExtensionByName(logConfig);
		if (e == null)
		{
			LOGGER.severe("Could not find logging configuration {}. Default configuration is used.", logConfig);
			return;
		}
		if (!Arrays2.contains(e.points, EP_LOGCONFIG))
		{
			LOGGER.severe("Extension {} is not a logging configuration. Default configuration is used.", logConfig);
			return;
		}
		configureLogging(e);
	}

	private static final void configureLogging(Extension extension)
	{
		LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
		context.reset();
		StatusManager sm = context.getStatusManager();
		if (sm != null)
			sm.add(new InfoStatus("Setting up "+extension.name+" configuration.", context));

		Encoder<ILoggingEvent> encoder = configureEncoder(ExtensionRegistry.getExtensionByName(extension.getProperty("encoder")), context);
		encoder.setContext(context);
		encoder.start();

		Appender<ILoggingEvent> appender = configureAppender(ExtensionRegistry.getExtensionByName(extension.getProperty("appender")), encoder);
		appender.setContext(context);
		appender.start();

		ch.qos.logback.classic.Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		rootLogger.setLevel(Level.toLevel(extension.<String>getProperty("level"), Level.TRACE));
		rootLogger.addAppender(appender);
		
		 StatusPrinter.printIfErrorsOccured(context);
	}

	private static final int F_SIZE = 1 << 1;
	private static final int F_TIME = 1 << 2;
	private static final int F_FILECOUNT = 1 << 3;
	@SuppressWarnings("rawtypes")
	private static final Appender<ILoggingEvent> configureAppender(Extension ext, Encoder<ILoggingEvent> encoder)
	{
		switch (ext.<String>getProperty("type"))
		{
			case "console":
				ConsoleAppender<ILoggingEvent> ca = new ConsoleAppender<>();
				ca.setName("console");
				ca.setTarget("System.err");
				ca.setEncoder(encoder);
				return ca;
			//FIXME support this with the resource plugin
			case "file":
				int flags = 0;
				flags |= ext.hasProperty("filesize") ? F_SIZE : 0;
				flags |= ext.getProperty("filepattern", "").contains("%d{") ? F_TIME : 0;
				flags |= ext.hasProperty("filecount") ? F_FILECOUNT : 0;
				
				if (flags == 0 || flags == F_FILECOUNT)
				{
					FileAppender<ILoggingEvent> fa = new FileAppender<>();
					fa.setAppend(ext.getProperty("append", false));
					fa.setEncoder(encoder);
					fa.setFile(ext.getProperty("file", "run"+DATE_FORMAT.format(LocalDateTime.now())+".log"));
					return fa;
				}
				
				RollingFileAppender<ILoggingEvent> rfa = new RollingFileAppender<>();
				rfa.setFile(ext.getProperty("file", ".log"));
				rfa.setEncoder(encoder);
				
				RollingPolicyBase rolling = null;
				TriggeringPolicyBase<ILoggingEvent> trigger = null;
				switch (flags)
				{
					case F_SIZE:
					case F_SIZE | F_FILECOUNT:
						rolling = new FixedWindowRollingPolicy();
						((FixedWindowRollingPolicy) rolling).setFileNamePattern(ext.getProperty("filepattern", "%i.log.zip"));
						((FixedWindowRollingPolicy) rolling).setMaxIndex(ext.getProperty("filecount", 20));
						
						trigger = new SizeBasedTriggeringPolicy<>();
						((SizeBasedTriggeringPolicy<ILoggingEvent>) trigger).setMaxFileSize(ext.getProperty("filesize", "10MB"));
						break;
					case F_TIME:
					case F_TIME | F_FILECOUNT:
						rolling = new TimeBasedRollingPolicy<>();
						((TimeBasedRollingPolicy) rolling).setFileNamePattern(ext.getProperty("filepattern", "%d{yyyy-MM-dd}.log.zip"));
						if (flags == (F_TIME | F_FILECOUNT))
							((TimeBasedRollingPolicy) rolling).setMaxHistory(ext.getProperty("filecount", 10));
						break;
					case F_TIME | F_SIZE:
					case F_TIME | F_SIZE | F_FILECOUNT:
						rolling = new TimeBasedRollingPolicy<>();
						SizeAndTimeBasedFNATP<ILoggingEvent> sizeandtime = new SizeAndTimeBasedFNATP<ILoggingEvent>();
						sizeandtime.setMaxFileSize(ext.getProperty("filesize", "10MB"));
						sizeandtime.setTimeBasedRollingPolicy((TimeBasedRollingPolicy<ILoggingEvent>) rolling);
						((TimeBasedRollingPolicy<ILoggingEvent>) rolling).setTimeBasedFileNamingAndTriggeringPolicy(sizeandtime);
						((TimeBasedRollingPolicy) rolling).setFileNamePattern(ext.getProperty("filepattern", "%d{yyyy-MM-dd}.%i.log.zip"));
						if (flags == (F_TIME | F_SIZE | F_FILECOUNT))
							((TimeBasedRollingPolicy) rolling).setMaxHistory(ext.getProperty("filecount", 10));
						sizeandtime.start();
						break;
					default:
						throw new IllegalStateException();
				}
				rolling.setContext(encoder.getContext());
				rolling.setParent(rfa);
				rolling.start();
				rfa.setRollingPolicy(rolling);
				if (trigger != null)
				{
					trigger.start();
					rfa.setTriggeringPolicy(trigger);
				}
				return rfa;
			case "syslog":
				throw new UnsupportedOperationException();
			default:
				throw new IllegalStateException();
		}
	}
	
	private static final Encoder<ILoggingEvent> configureEncoder(Extension ext, LoggerContext context)
	{
		switch (ext.<String>getProperty("type"))
		{
			case "alias":
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
				configureAliases();
				
				PatternLayoutEncoder pl = new PatternLayoutEncoder();
				if (!ext.getProperty("highlight", false))
					pl.setPattern("%-5level %-20([%thread]) %-36alias{36} - %msg%n");
				else
					pl.setPattern("%highlight(%-5level) %red(%-20([%thread])) %cyan(%-36alias{36}) %red(- %msg) %n");
				return pl;
			default:
				throw new UnsupportedOperationException();
		}
	}

	public static final void closeLogger()
	{
		((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
	}

	private LoggingTools()
	{
		//no instance
	}

}
