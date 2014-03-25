/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)
	
	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the 
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.log;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * @author renken
 *
 */
public class LoggerRenamer extends ClassicConverter
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String convert(ILoggingEvent event)
	{
		return ClassRenamer.alias(event.getLoggerName());
	}

}
