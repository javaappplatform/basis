/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.webdav;

import github.javaappplatform.platform.resource.IResource;
import github.javaappplatform.platform.resource.internal.AFileSystem;
import github.javaappplatform.platform.resource.internal.IInternalDirectory;
import github.javaappplatform.platform.resource.internal.IInternalFile;

import java.io.IOException;
import java.net.URI;

/**
 * TODO javadoc
 * @author MeisterYeti
 */
public class WebDavFileSystem extends AFileSystem
{

	/**
	 * @param sslTrustAll
	 */
	public WebDavFileSystem()
	{
		super("dav", "davs");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IInternalDirectory createDirectory(URI uri)
	{
		return new InternalWebDavDirectory(uri);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IInternalFile createResource(URI uri)
	{
		return new InternalWebDavFile(uri);
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
