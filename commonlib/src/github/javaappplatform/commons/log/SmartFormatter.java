/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Formats the log output and replaces classnames using {@link ClassRenamer}.
 *
 * @see ClassRenamer
 * @author joda
 */
public class SmartFormatter extends Formatter
{
	private final static Map<Level, String>	logLevel	= new HashMap<Level, String>();
	private final static String				FORMAT		= "{0,date,mm:ss.SSS}";		// yyyy.MM.dd

	private final MessageFormat				formatter	= new MessageFormat(FORMAT);

	static
	{
		logLevel.put(Level.INFO, "INFO");
		logLevel.put(Level.FINE, "FINE");
		logLevel.put(Level.FINER, "FINR");
		logLevel.put(Level.FINEST, "FINT");
		logLevel.put(Level.SEVERE, "SEVE");
		logLevel.put(Level.WARNING, "WARN");
		logLevel.put(Level.CONFIG, "CONF");
	}

	private static String getLevelString(Level l)
	{
		final Object o = logLevel.get(l);
		return o == null ? l.toString() : o.toString();
	}

	/**
	 * Constructs a new instance.
	 */
	public SmartFormatter()
	{
		// empty
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String format(LogRecord record)
	{
		final Date dat = new Date(record.getMillis());
		final Object args[] = new Object[] { dat };

		final String fullName = record.getSourceClassName();

		final String message = this.formatMessage(record);

		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		pw.print(getLevelString(record.getLevel()));
		pw.print(' ');
		this.formatter.format(args, sw.getBuffer(), null);
		pw.print(' ');
		pw.print(ClassRenamer.alias(fullName));
		pw.print('.');
		pw.print(record.getSourceMethodName());
		pw.print(": ");
		pw.print(message);
		pw.println();

		final Throwable t = record.getThrown();
		if (t != null)
			this.printStackTrace(t, pw);

		pw.close();
		return sw.toString();
	}

	private void printStackTrace(Throwable t, PrintWriter pw)
	{
		pw.println(t.toString());

		final StackTraceElement[] st = t.getStackTrace();
		for (final StackTraceElement element : st)
			pw.println("\tat " + element);

		final Throwable tc = t.getCause();
		if (tc != null)
		{
			pw.print("Caused by: ");
			this.printStackTrace(tc, pw);
		}
	}
}
