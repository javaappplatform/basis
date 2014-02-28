/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.console;

import github.javaappplatform.commons.util.Close;
import github.javaappplatform.platform.console.ICommand;
import github.javaappplatform.platform.resource.IResource;
import github.javaappplatform.platform.resource.Resource;
import github.javaappplatform.platform.resource.Stream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;

/**
 * TODO javadoc
 * @author funsheep
 */
public class CATCommand implements ICommand
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(String[] args, PrintStream out) throws Exception
	{
		if (args.length < 1 || args.length > 2)
		{
			out.print("Parameters not recognized.");
			return;
		}

		URI uri;
		int lines = -1;
		if (args.length == 1)
			uri = new URI(args[0]);
		else
		{
			lines = Integer.parseInt(args[0]);
			uri = new URI(args[1]);
		}

		IResource res = Resource.at(uri);
		if (!res.exists())
		{
			out.print("Given Resource does not exist.");
			return;
		}

		BufferedReader in = null;
		try
		{
			in = new BufferedReader(new InputStreamReader(Stream.open(res).toRead()));
			String line = null;
			while ((lines < 0 | lines > 0) & (line = in.readLine()) != null)
			{
				out.println(line);
				lines--;
			}
		}
		finally
		{
			Close.close(in);
		}
	}

}
