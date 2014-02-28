/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.utils;

import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;

import java.io.File;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class OptionTools
{

	public static final Options getOptions()
	{
		Options options = new Options();
		Set<Extension> exts = ExtensionRegistry.getExtensions("github.javaappplatform.platform.option");
		for (Extension e : exts)
		{
			if (!e.hasProperty("name"))
				break;
			if (e.hasProperty("description"))
				OptionBuilder.withDescription(e.<String>getProperty("description"));
			if (e.hasProperty("isRequired"))
				OptionBuilder.isRequired(e.<Boolean>getProperty("isRequired").booleanValue());
			if (e.hasProperty("ArgName"))
				OptionBuilder.withArgName(e.<String>getProperty("ArgName"));
			if (e.hasProperty("ArgsSeparator") && e.<String>getProperty("ArgsSeparator").length() > 0)
			{
				if (e.<String>getProperty("ArgsSeparator").equals("OS-PATH-SEP"))
					OptionBuilder.withValueSeparator(File.pathSeparatorChar);
				else
					OptionBuilder.withValueSeparator(e.<String>getProperty("ArgsSeparator").charAt(0));
			}
			if (e.hasProperty("numOfArgs"))
			{
				Object value = e.getProperty("numOfArgs");
				if (value instanceof Integer)
					OptionBuilder.hasArgs(((Integer) value).intValue());
				else if ("inf".equals(value))
					OptionBuilder.hasArgs();
			}
			if (e.hasProperty("(numOfArgs)"))
			{
				Object value = e.getProperty("(numOfArgs)");
				if (value instanceof Integer)
					OptionBuilder.hasOptionalArgs(((Integer) value).intValue());
				else if ("inf".equals(value))
					OptionBuilder.hasOptionalArgs();
			}
			options.addOption(OptionBuilder.create(e.<String>getProperty("name")));
		}
		return options;
	}

	public static final CommandLine parseOptions(String[] args) throws ParseException
	{
		GnuParser parser = new GnuParser();

		return parser.parse(getOptions(), args, false);
	}


	private OptionTools()
	{
		//no instance
	}

}
