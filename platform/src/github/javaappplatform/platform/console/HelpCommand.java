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
import java.util.Set;

/**
 * TODO javadoc
 * @author funsheep
 */
public class HelpCommand implements ICommand
{

	private static final int MAX_DESC_LINE = 60;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(String[] args, PrintStream out)
	{
		if (args.length == 0)
		{
			Set<Extension> cmds = ExtensionRegistry.getExtensions(ICommand.EXT_POINT);
			String[] arr = new String[cmds.size()];
			int i = 0;
			for (Extension e : cmds)
			{
				arr[i++] = e.<String>getProperty("command");
			}
			Arrays.sort(arr);
			out.println("Available Commands: ");
			out.println(formatLongString(Arrays.toString(arr)));
			out.println("Type help <command> for a more detailed description of the command.");
		}
		else
		{
			Extension cmd = ExtensionRegistry.getExtension(ICommand.EXT_POINT, "command="+args[0]);
			out.println(cmd.getProperty("description"));
		}

	}


	private static final String formatLongString(String s)
	{
		StringBuilder sb = new StringBuilder(s.length()+3);
		while (s.length() > MAX_DESC_LINE)
		{
			String intervall = s.substring(MAX_DESC_LINE-5, Math.min(s.length(), MAX_DESC_LINE+5));
			int next = intervall.lastIndexOf('\n');
			next = Math.max(next, intervall.lastIndexOf('.'));
			next = Math.max(next, intervall.lastIndexOf(','));
			next = Math.max(next, intervall.lastIndexOf(';'));
			next = Math.max(next, intervall.lastIndexOf(' '));

			next = MAX_DESC_LINE + (next != -1 ? -5+next+1 : 0);

			sb.append(s.substring(0, next));
			sb.append('\n');
			s = s.substring(next);
		}
		sb.append(s);
		return sb.toString();
	}

}
