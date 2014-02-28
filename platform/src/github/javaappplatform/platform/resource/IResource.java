/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource;

import java.net.URI;

/**
 * TODO javadoc
 * @author funsheep
 */
public interface IResource
{

//	public static final int EVENT_CREATED = StringID.id("RESOURCE_CREATED");
//	public static final int EVENT_DELETED = StringID.id("RESOURCE_DELETED");
//	public static final int EVENT_CONTENT_CHANGED = StringID.id("CONTENT_CHANGED");
//
//	public static final int EVENT_OVERFLOW = StringID.id("RS_EVENT_OVERFLOW");


	public static final long UNKNOWN_SIZE = -1;
	public static final long UNKNOWN_MODIFIED_TIME = -1;


	public URI uri();

	public int type();

	public boolean exists();


	public String mimetype();

	public long size();

	public long lastTimeModified();

	public boolean isReadable();

	public boolean isWritable();

	/**
	 * Convenient method for the resource system. This method can be used to indicate that this resource is no longer
	 * used. The resource system then may clean up internal data structures. Otherwise the resource system is notified
	 * by the garbage collector when the object is no longer in use.
	 */
	public void discard();

}
