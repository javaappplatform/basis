/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.localfile;

import github.javaappplatform.platform.resource.IResourceAPI.OpenOption;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * TODO javadoc
 * @author funsheep
 */
final class RestrictedInternalLocalFile extends InternalLocalFile
{

	/**
	 * @param uri
	 */
	public RestrictedInternalLocalFile(URI uri)
	{
		super(uri);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputStream openStreamToWrite(int options) throws IOException
	{
		return super.openStreamToWrite(options ^ OpenOption.APPEND_TO_RESOURCE.flag);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean delete() throws IOException
	{
		throw new UnsupportedOperationException("Deleting files is not permitted.");
	}

}
