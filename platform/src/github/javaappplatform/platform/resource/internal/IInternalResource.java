/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.internal;

import github.javaappplatform.platform.resource.IResource;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO javadoc
 * @author funsheep
 */
public interface IInternalResource extends IResource, Closeable
{

	public abstract InputStream openStreamToRead(int options) throws IOException;

	public abstract OutputStream openStreamToWrite(int options) throws IOException;


	public boolean delete() throws IOException;

}
