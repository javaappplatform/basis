/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource;

import java.io.IOException;
import java.net.URI;

/**
 * TODO javadoc
 * @author funsheep
 */
public interface IDirectory
{

//	public static final int EVENT_CREATED  = StringID.id("DIRECTORY_CREATED");
//	public static final int EVENT_DELETED  = StringID.id("DIRECTORY_DELETED");
//	public static final int EVENT_OVERFLOW = IResource.EVENT_OVERFLOW;
//
//	public static final int EVENT_CHILD_CREATED = StringID.id("CHILD_CREATED");
//	public static final int EVENT_CHILD_DELETED = StringID.id("CHILD_DELETED");


	public URI uri();

	public int resourceType();

	public boolean exists();

	public String name();

	public boolean isWritable();

	public URI[] getChildren() throws IOException;

	public void discard();

}
