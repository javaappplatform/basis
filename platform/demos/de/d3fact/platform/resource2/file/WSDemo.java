/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package de.d3fact.platform.resource2.file;

import github.javaappplatform.platform.resource.IDirectory;
import github.javaappplatform.platform.resource.localfile.WorkspaceFileSystem;

import java.net.URI;

/**
 * TODO javadoc
 * @author funsheep
 */
public class WSDemo
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		WorkspaceFileSystem sys = new WorkspaceFileSystem();
		URI uri = new URI("ws:/../../workspaces/");
		IDirectory file = sys.resolveAsDirectory(uri);
		System.out.println("Exists: " + file.exists());
	}

}
