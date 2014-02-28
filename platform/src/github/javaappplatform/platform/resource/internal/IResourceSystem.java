/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.internal;

import github.javaappplatform.platform.resource.IDirectory;
import github.javaappplatform.platform.resource.IResource;

import java.io.IOException;
import java.net.URI;

/**
 * TODO javadoc
 * @author funsheep
 */
public interface IResourceSystem
{

	public static final String EXT_POINT = "github.javaappplatform.platform.ResourceSystem";


//	public static final String RESOURCESYSTEM_THREAD = "ResourceSystem Event Dispatcher";


	public IResource resolveAsResource(URI uri);

	public IDirectory resolveAsDirectory(URI uri);


	public IInternalDirectory open(IDirectory dir) throws IOException;

	public IInternalResource open(IResource file) throws IOException;

	public void copy(IResource from, IResource to, int options) throws IOException;

}
