/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource;

import github.javaappplatform.platform.resource.internal.IResourceSystem;
import github.javaappplatform.platform.resource.internal.ResourceTools;
import github.javaappplatform.platform.utils.URIs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Directory
{

	public static final IDirectory at(String string)
	{
		try
		{
			return at(new URI(string));
		} catch (URISyntaxException e)
		{
			return null;
		}
	}

	public static final IDirectory at(URI uri)
	{
		if (!URIs.isDirectory(uri))
			throw new IllegalArgumentException("URI " + uri + " does not refer to a directory.");
		final IResourceSystem rs = ResourceTools.getRS(uri.getScheme());
		if (rs == null)
			throw new IllegalArgumentException("Could not find resource-system for scheme " + uri.getScheme());
		return rs.resolveAsDirectory(uri);
	}

	public static final boolean create(IDirectory dir) throws IOException
	{
		final IResourceSystem rs = ResourceTools.getRS(dir.uri().getScheme());
		return rs.open(dir).create();
	}

	public static final void ensureThatExists(IDirectory dir) throws IOException
	{
		final IResourceSystem rs = ResourceTools.getRS(dir.uri().getScheme());
		rs.open(dir).ensureExistence();
	}


	public static final boolean delete(IDirectory dir) throws IOException
	{
		final IResourceSystem rs = ResourceTools.getRS(dir.uri().getScheme());
		return rs.open(dir).delete();
	}

	public static final void deleteIfExists(IDirectory dir) throws IOException
	{
		if (dir.exists())
			delete(dir);
	}


	private Directory()
	{
		//no instance
	}
}
