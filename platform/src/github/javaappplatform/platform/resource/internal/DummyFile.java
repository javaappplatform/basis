/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.internal;

import github.javaappplatform.platform.resource.IDirectory;
import github.javaappplatform.platform.resource.IFile;
import github.javaappplatform.platform.resource.IResourceAPI;
import github.javaappplatform.platform.utils.URIs;

import java.io.IOException;
import java.net.URI;

/**
 * TODO javadoc
 * @author funsheep
 */
public class DummyFile extends DummyResource implements IFile
{

	private final String name;

	/**
	 * @param uri
	 * @param sys
	 */
	public DummyFile(URI uri, IResourceSystem sys)
	{
		super(uri, sys);
		this.name = URIs.extractName(this.uri());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int type()
	{
		return IResourceAPI.TYPE_FILE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String name()
	{
		return this.name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String fileExtension()
	{
		return ResourceTools.getRawFileExt(this.name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDirectory getParent()
	{
		try
		{
			return ((IInternalFile) this.sys.open(this)).getParent();
		}
		catch (IOException e)
		{
			return null;
		}
	}

}
