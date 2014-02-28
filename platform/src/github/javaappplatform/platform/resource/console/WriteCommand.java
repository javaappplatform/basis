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
import github.javaappplatform.platform.resource.Resource;
import github.javaappplatform.platform.resource.Stream;
import github.javaappplatform.platform.resource.IResourceAPI.OpenOption;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * TODO javadoc
 * @author funsheep
 */
public class WriteCommand implements ICommand
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(String[] args, PrintStream out) throws Exception
	{
		if (args.length < 2)
		{
			out.println("Parameters not recognized.");
			return;
		}

		URI uri = new URI(args[0]);
		int options = options(args[1]);

		OutputStream stream = Stream.open(Resource.at(uri)).with(Math.max(0, options)).toWrite();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, Charset.forName("UTF-8")));
		try
		{
			for (int i = (options < 0 ? 1 : 2); i < args.length; i++)
			{
				writer.append(args[i]);
				writer.append(" ");
			}
			writer.flush();
		}
		finally
		{
			Close.close(writer);
		}
	}


	private static final int options(String arg)
	{
		int options = -1;
		String[] stropts = arg.split("\\|");
		for (String opt : stropts)
		{
			OpenOption o = OpenOption.resolve(opt);
			if (options == -1)
				options = 0;
			if (o != null)
				options = options | o.flag;
		}
		return options;
	}

}
