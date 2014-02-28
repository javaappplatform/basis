/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource;

import github.javaappplatform.commons.io.InPipeOut;
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
public class CopyResource
{

	private final IResource source;
	private int _options = 0;


	/**
	 *
	 */
	private CopyResource(IResource source)
	{
		this.source = source;
	}

	public static final CopyResource from(IResource source)
	{
		if (!source.exists())
			throw new IllegalArgumentException("Given source " + source.uri() + " does not exist.");
		return new CopyResource(source);
	}

	public final CopyResource withOptions(int options)
	{
		this._options = options;
		return this;
	}


	public final void to(IResource destination) throws IOException
	{
		final IResourceSystem rsfrom = ResourceTools.getRS(this.source.uri().getScheme());
		final IResourceSystem rsto   = ResourceTools.getRS(destination.uri().getScheme());
		if (rsfrom == rsto)
		{
			rsfrom.copy(this.source, destination, this._options);
			return;
		}

		InputStream in = Stream.open(this.source).toRead();
		OutputStream out = Stream.open(destination).with(this._options).toWrite();

		try
		{
			InPipeOut.pipe(in, out);
		}
		catch (InterruptedException e)
		{
			if (OpenOption.DELETE_ON_FAIL.isSetIn(this._options))
				Resource.deleteIfExists(destination);
			throw new IOException("Copying data from " + this.source.uri() + " to " + destination.uri() +  " was interrupted.");
		}
		catch (IOException e)
		{
			if (OpenOption.DELETE_ON_FAIL.isSetIn(this._options))
				Resource.deleteIfExists(destination);
			throw e;
		}
	}

}
