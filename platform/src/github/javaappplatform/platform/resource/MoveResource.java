/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource;

import github.javaappplatform.platform.resource.IResourceAPI.OpenOption;

import java.io.IOException;

/**
 * TODO javadoc
 * @author funsheep
 */
public class MoveResource
{

	private final IResource source;
	private int _options = 0;


	/**
	 *
	 */
	private MoveResource(IResource source)
	{
		this.source = source;
	}

	public static final MoveResource from(IResource source)
	{
		if (!source.exists())
			throw new IllegalArgumentException("Given source " + source.uri() + " does not exist.");
		if (!source.isWritable())
			throw new IllegalArgumentException("Given source " + source.uri() + " cannot be deleted after finished copying.");
		return new MoveResource(source);
	}

	public final MoveResource withOptions(int options)
	{
		this._options = options;
		return this;
	}


	public final void to(IResource destination) throws IOException
	{
		try
		{
			CopyResource.from(this.source).withOptions(this._options).to(destination);
			Resource.delete(this.source);
		}
		catch (IOException e)
		{
			if (OpenOption.DELETE_ON_FAIL.isSetIn(this._options))
				Resource.deleteIfExists(destination);
			throw e;
		}
	}

}
