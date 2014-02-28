/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.internal;

import github.javaappplatform.platform.resource.IDirectory;

import java.io.Closeable;
import java.io.IOException;

/**
 * TODO javadoc
 * @author funsheep
 */
public interface IInternalDirectory extends IDirectory, Closeable
{

	public boolean create() throws IOException;

	public boolean ensureExistence() throws IOException;

	public boolean delete() throws IOException;

}
