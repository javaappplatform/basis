/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource;

import github.javaappplatform.commons.collection.SmallSet;
import github.javaappplatform.platform.resource.IResourceAPI.OpenOption;
import github.javaappplatform.platform.resource.internal.IResourceSystem;
import github.javaappplatform.platform.resource.internal.ResourceTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO javadoc
 * @author funsheep
 */
public class Stream
{

	private final IResource resource;
	private final SmallSet<OpenOption> oo = new SmallSet<>();


	private Stream(IResource resource)
	{
		this.resource = resource;
	}


	public static final Stream open(IResource resource)
	{
		return new Stream(resource);
	}

	public Stream with(OpenOption option)
	{
		this.oo.add(option);
		return this;
	}

	public Stream with(int options)
	{
		this.oo.addAll(OpenOption.resolve(options));
		return this;
	}

	public InputStream toRead() throws IOException
	{
		if (this.oo.size() == 0)
			this.oo.addAll(ResourceTools.extractOptions(this.resource.uri()));

		final IResourceSystem rs = ResourceTools.getRS(this.resource.uri().getScheme());
		return rs.open(this.resource).openStreamToRead(OpenOption.resolve(this.oo));
	}

	public OutputStream toWrite() throws IOException
	{
		if (this.oo.size() == 0)
			this.oo.addAll(ResourceTools.extractOptions(this.resource.uri()));

		final IResourceSystem rs = ResourceTools.getRS(this.resource.uri().getScheme());
		return rs.open(this.resource).openStreamToWrite(OpenOption.resolve(this.oo));
	}

}
