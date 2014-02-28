/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.console;

import github.javaappplatform.platform.console.ICommand;
import github.javaappplatform.platform.resource.Directory;
import github.javaappplatform.platform.resource.IDirectory;
import github.javaappplatform.platform.resource.IResource;
import github.javaappplatform.platform.resource.Resource;
import github.javaappplatform.platform.utils.URIs;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;

/**
 * TODO javadoc
 * @author funsheep
 */
public class LSCommand implements ICommand
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(String[] args, PrintStream out) throws Exception
	{
		URI uri;
		boolean recursive = false;
		boolean large = false;
		if (args.length == 2)
		{
			uri = new URI(args[1]);
			recursive = args[0].indexOf('r') >= 0;
			large = args[0].indexOf('l') >= 0;
		}
		else if (args.length == 1)
		{
			uri = new URI(args[0]);
		}
		else
		{
			out.println("Parameters not recognized.");
			return;
		}

		out.print(uri);
		if (URIs.isDirectory(uri))
		{
			processDirectory(Directory.at(uri), recursive, large, out);
		}
		else
			processResource(Resource.at(uri), large, out);
	}


	private static final void processDirectory(IDirectory dir, boolean recursive, boolean large, PrintStream out) throws IOException
	{
		if (!dir.exists())
		{
			out.println(" Exists: " + dir.exists());
			return;
		}
		out.println();
		for (URI uri : dir.getChildren())
		{
			out.print(uri);
			if (URIs.isDirectory(uri))
			{
				if (recursive)
					processDirectory(Directory.at(uri), recursive, large, out);
			}
			else
				processResource(Resource.at(uri), large, out);
			out.println();
		}
	}

	private static final void processResource(IResource res, boolean large, PrintStream out)
	{
		if (large)
		{
			out.print(" Type: " + res.type());
			out.print(" mimetype: " + res.mimetype());
			out.print(" Exists: " + res.exists());
			if (res.exists())
			{
				out.print(" Size: " + res.size());
				out.print(" LastModified: " + res.lastTimeModified());
				out.print(" isReadable: " + res.isReadable());
				out.print(" isWritable: " + res.isWritable());
			}
		}
	}

}
