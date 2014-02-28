/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.java;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.resource.IDirectory;
import github.javaappplatform.platform.resource.IResource;
import github.javaappplatform.platform.resource.internal.DummyFile;
import github.javaappplatform.platform.resource.internal.IInternalDirectory;
import github.javaappplatform.platform.resource.internal.IInternalResource;
import github.javaappplatform.platform.resource.internal.IResourceSystem;

import java.io.IOException;
import java.net.URI;

/**
 * TODO javadoc
 * @author funsheep
 */
public class JavaResourceSystem implements IResourceSystem
{

	private static final Logger LOGGER = Logger.getLogger();


	/**
	 * {@inheritDoc}
	 */
	@Override
	public IResource resolveAsResource(URI uri)
	{
		Object[] call = JavaRSTools.detectJavaCall(uri);
		if (call != null)
			return new JavaCall(uri, call);
		LOGGER.fine("Could not detect appropriate java call for "+uri+". Will be handled as a file.");
		return new DummyFile(uri, this);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void copy(IResource from, IResource to, int options) throws IOException
	{
		throw new UnsupportedOperationException("Java methods cannot be copied.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDirectory resolveAsDirectory(URI uri)
	{
		throw new UnsupportedOperationException("Java methods are not organized in directories.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IInternalDirectory open(IDirectory dir)
	{
		throw new UnsupportedOperationException("Java methods are not organized in directories.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IInternalResource open(IResource file)
	{
		return new ClasspathFile(file.uri());
	}

}
