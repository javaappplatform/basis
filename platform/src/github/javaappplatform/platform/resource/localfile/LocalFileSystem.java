/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.localfile;

import github.javaappplatform.platform.Platform;
import github.javaappplatform.platform.resource.IResource;
import github.javaappplatform.platform.resource.internal.AFileSystem;
import github.javaappplatform.platform.resource.internal.IInternalDirectory;
import github.javaappplatform.platform.resource.internal.IInternalFile;

import java.io.IOException;
import java.net.URI;

/**
 * TODO javadoc
 * @author funsheep
 */
public class LocalFileSystem extends AFileSystem
{

	public static final String URI_SCHEME = "file";

	private static final boolean RESTRICT = Platform.hasOption("restrictlocalfilesystem");


	public LocalFileSystem()
	{
		super(URI_SCHEME);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IInternalDirectory createDirectory(URI uri)
	{
		if (!RESTRICT)
			return new InternalLocalFileDirectory(uri);
		return new RestrictedInternalLocalDirectory(uri);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IInternalFile createResource(URI uri)
	{
		if (!RESTRICT)
			return new InternalLocalFile(uri);
		return new RestrictedInternalLocalFile(uri);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void copy(IResource from, IResource to, int options) throws IOException
	{
		InternalLocalFile fromfile = (InternalLocalFile) this.open(from);
		InternalLocalFile tofile = (InternalLocalFile) this.open(to);

		FileTools.copy(fromfile.path, tofile.path, options);
	}

}
