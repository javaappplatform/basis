/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.console;

import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * TODO javadoc
 * @author funsheep
 */
public class ExtensionCommand implements ICommand
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(String[] args, PrintStream out)
	{
		Set<Extension> exts;
		if (args.length == 2)
			exts = ExtensionRegistry.getExtensions(args[0], args[1]);
		else if (args.length == 1)
			exts = ExtensionRegistry.getExtensions(args[0]);
		else
			exts = ExtensionRegistry.getExtensions();

		String[] arr = new String[exts.size()];
		int i = 0;
		for (Extension e : exts)
		{
			StringBuilder sb = new StringBuilder(40);
			sb.append(e.name);
			sb.append('{');
			for (Map.Entry<String, Object> prop : e.getProperties())
			{
				sb.append('"');
				sb.append(prop.getKey());
				sb.append("\"=\"");
				sb.append(String.valueOf(prop.getValue()));
				sb.append("\",");
			}
			sb.append('}');
			arr[i++] = sb.toString();
		}
		Arrays.sort(arr);
		for (String s : arr)
			out.println(s);
	}

}
