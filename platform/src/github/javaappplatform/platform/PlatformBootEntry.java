/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform;

import github.javaappplatform.platform.boot.IBootEntry;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.utils.LoggingTools;


public class PlatformBootEntry implements IBootEntry
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startup(Extension e) throws PlatformException
	{
		String logconfig = Platform.getOptionValue("logconfig");
		if (logconfig != null)
			LoggingTools.configureLogging(logconfig);
		else
			LoggingTools.configureDefault();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown() throws PlatformException
	{
		LoggingTools.closeLogger();
	}

}
