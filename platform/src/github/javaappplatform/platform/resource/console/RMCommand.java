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
import github.javaappplatform.platform.resource.Resource;
import github.javaappplatform.platform.utils.URIs;

import java.io.PrintStream;
import java.net.URI;

/**
 * TODO javadoc
 * @author funsheep
 */
public class RMCommand implements ICommand
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(String[] args, PrintStream out) throws Exception
	{
		if (args.length == 0)
		{
			out.println("Parameters not recognized.");
			return;
		}

		URI uri = new URI(args[0]);
		if (URIs.isDirectory(uri))
		{
			Directory.delete(Directory.at(uri));
		}
		else
			Resource.delete(Resource.at(uri));
	}

}
