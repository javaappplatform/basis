/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.project;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.platform.Platform;
import github.javaappplatform.platform.resource.Resource;
import github.javaappplatform.platform.resource.Stream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * TODO javadoc
 * @author funsheep
 */
@SuppressWarnings("resource")
public class Project
{

	public static final String NAME;
	public static final String OUTPUT;

	private static final Logger LOGGER = Logger.getLogger();


	static
	{
		InputStream in = null;
		Properties props = new Properties();
		try
		{
			if (!Platform.hasOption("project"))
			{
				Path path = Paths.get(".d3factproject");
				if (Files.exists(path))
					in = Files.newInputStream(path);
			}
			else
			{
				String projectfile = Platform.getOptionValue("project");
				in = Stream.open(Resource.at(projectfile)).toRead();
			}

			if (in != null)
			{
				props.load(in);
			}
		}
		catch (IOException e)
		{
			Close.close(in);
			LOGGER.info("Could not read project file.", e);
		}
		NAME = props.getProperty("name");
		OUTPUT = props.getProperty("output");
	}


	/**
	 *
	 */
	private Project()
	{
		//do nothing
	}

}
