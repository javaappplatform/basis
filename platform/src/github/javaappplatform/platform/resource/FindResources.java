/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource;

import github.javaappplatform.platform.utils.URIs;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * TODO javadoc
 * @author funsheep
 */
public class FindResources
{
	private static final int FILTER_MIMETYPE = 0;
	private static final int FILTER_FILENAME = 1;
	private static final int FILTER_MATCH = 2;
	private static final int FILTER_ALL = 3;


	private final String filter;
	private final int type;


	private FindResources(String filter, int type)
	{
		this.filter = filter;
		this.type = type;
	}


	public static final FindResources ofType(String mimetype)
	{
		return new FindResources(mimetype, FILTER_MIMETYPE);
	}

	public static final FindResources withName(String filename)
	{
		return new FindResources(filename, FILTER_FILENAME);
	}

	public static final FindResources containingString(String match)
	{
		return new FindResources(match, FILTER_MATCH);
	}

	public static final FindResources withOutFilter()
	{
		return new FindResources(null, FILTER_ALL);
	}

	public final Set<IResource> in(IDirectory directory) throws IOException
	{
		return this.search(directory, false);
	}

	public final Set<IResource> beneath(IDirectory directory) throws IOException
	{
		return this.search(directory, true);
	}

	private final Set<IResource> search(IDirectory dir, boolean recursive) throws IOException
	{
		LinkedHashSet<IResource> set = new LinkedHashSet<>();

		ArrayDeque<URI> todo = new ArrayDeque<>();
		todo.addAll(Arrays.asList(dir.getChildren()));

		while (!todo.isEmpty())
		{
			final URI uri = todo.removeFirst();
			if (!URIs.isDirectory(uri))
			{
				IResource res = Resource.at(uri);
				if (this.type == FILTER_ALL)
					set.add(res);
				else if (this.type == FILTER_MIMETYPE)
				{
					if (res.mimetype().equalsIgnoreCase(this.filter))
						set.add(res);
				}
				else
				{
					String name;
					if (res instanceof IFile)
						name = ((IFile) res).name();
					else
						name = URIs.extractName(uri);
					if (this.type == FILTER_FILENAME && name.equalsIgnoreCase(this.filter))
						set.add(res);
					else if (name.toLowerCase().contains(this.filter.toLowerCase()))
						set.add(res);
				}
			}
			else if (recursive)
			{
				IDirectory dir2 = Directory.at(uri);
				todo.addAll(Arrays.asList(dir2.getChildren()));
			}
		}
		return set;
	}

}
