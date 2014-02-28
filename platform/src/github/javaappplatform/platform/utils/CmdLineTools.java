/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.utils;

import github.javaappplatform.commons.util.Arrays2;

/**
 * TODO javadoc
 * @author funsheep
 */
public class CmdLineTools
{

	public static final String getValue(String optionName, String[] args)
	{
		if (optionName.charAt(0) != '-')
			optionName = '-' + optionName;
		int index = Arrays2.indexOf(args, optionName);
		if (index != -1 && index < args.length-1 && args[index+1].charAt(0) != '-')
		{
			return args[index+1];
		}
		return null;
	}

	private CmdLineTools()
	{
		//no instance
	}

}
