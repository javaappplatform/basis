/*
	This file is part of the javaappplatform platform library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)
	
	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the 
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.console;

import java.io.PrintStream;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

/**
 * logging level of <package> set to <level>
 * @author renken
 */
public class LoggingCommand implements ICommand
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(String[] args, PrintStream out) throws Exception
	{
		if (args.length == 6 && "level".equals(args[0]) && "of".equals(args[1]) && "set".equals(args[3]) && "to".equals(args[4]))
		{
			LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

			ch.qos.logback.classic.Logger logger = context.getLogger(args[2]);
			logger.setLevel(Level.toLevel(args[5], Level.INFO));
		}
		else
			out.println("Arguments not recognized.");
	}

}
