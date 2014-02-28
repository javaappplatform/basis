/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.weblink;

import github.javaappplatform.platform.resource.IResource;
import github.javaappplatform.platform.resource.internal.AResourceSystem;
import github.javaappplatform.platform.resource.internal.IInternalResource;

import java.io.IOException;
import java.net.URI;

/**
 * TODO javadoc
 * @author funsheep
 */
public class WeblinkResourceSystem extends AResourceSystem
{

	/**
	 * @param sslTrustAll
	 */
	public WeblinkResourceSystem()
	{
		super("http", "https");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IInternalResource createResource(URI uri)
	{
		return new InternalWeblink(uri);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void copy(IResource from, IResource to, int options) throws IOException
	{
		throw new UnsupportedOperationException("Copying not supported by this resource system.");
	}

}
