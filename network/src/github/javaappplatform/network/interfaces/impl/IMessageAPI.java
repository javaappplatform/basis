/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.interfaces.impl;

import github.javaappplatform.commons.util.StringID;

/**
 * TODO javadoc
 * @author funsheep
 */
public interface IMessageAPI
{
	public static final int MSGTYPE_PING = 	1;

	public static final int MSGTYPE_PONG = 	2;



	/** Content: ID(int), Length(int), Content(*) */
	public static final int MSGTYPE_STREAM_HEADER = 		200;

	/** Content: ID(int), Content(*) */
	public static final int MSGTYPE_STREAM_INTERMEDIATE = 	201;

	/** Content: ID(int), Content(*) */
	public static final int MSGTYPE_STREAM_TAIL = 			202;

	/** Content: ID(int) */
	public static final int MSGTYPE_STREAM_ERROR = 			203;


	public static final int MSGTYPE_RESOURCES_GET				= 2010;
	public static final int MSGTYPE_RESOURCES_GET_ERROR			= 2013;
	public static final int MSGTYPE_RESOURCES_GET_INFO			= 2020;
	public static final int MSGTYPE_RESOURCES_GET_INFO_RESPONSE	= 2021;

	public static final int MSGTYPE_RESOURCE_RESOLVE_URI		    = 2050;
	public static final int MSGTYPE_RESOURCE_URI_RESOLVED		    = 2051;
	public static final int MSGTYPE_RESOURCE_RESOLVE_URI_ERROR	    = 2052;
	public static final int MSGTYPE_RESOURCE_OPEN_STREAM 		    = 2060;
	public static final int MSGTYPE_RESOURCE_STREAM_OPENED	 	    = 2061;
	public static final int MSGTYPE_RESOURCE_OPEN_STREAM_ERROR	    = 2062;
	public static final int MSGTYPE_RESOURCE_EVENT_OBJECT_CREATED 	= 2065;
	public static final int MSGTYPE_RESOURCE_EVENT_OBJECT_DELETED 	= 2066;
	public static final int MSGTYPE_RESOURCE_DELETE_URI 		    = 2070;
	public static final int MSGTYPE_RESOURCE_URI_DELETED		    = 2071;
	public static final int MSGTYPE_RESOURCE_DELETE_URI_ERROR 		= 2072;
	public static final int MSGTYPE_RESOURCE_CREATE_DIRECTORY 		= 2080;
	public static final int MSGTYPE_RESOURCE_DIRECTORY_CREATED 		= 2081;
	public static final int MSGTYPE_RESOURCE_CREATE_DIRECTORY_ERROR	= 2082;
	public static final int MSGTYPE_RESOURCE_COPY_RESOURCES			= 2090;
	public static final int MSGTYPE_RESOURCE_RESOURCES_COPIED		= 2091;
	public static final int MSGTYPE_RESOURCE_COPY_RESOURCE_ERROR	= 2092;

	public static final byte RESOURCE_RESOLVE_TYPE_DIRECTORY		= -1;
	public static final byte RESOURCE_RESOLVE_TYPE_RESOURCE			= 0;

	public static final int MSGTYPE_POOL_GET_CONTENT = 				2110;
	public static final int MSGTYPE_POOL_GET_CONTENT_RESPONSE = 	2111;
	public static final int MSGTYPE_POOL_GET_CONTENT_ERROR = 		2113;
	public static final int MSGTYPE_POOL_REGISTER_FOR_UPDATES = 	2120;
	public static final int MSGTYPE_POOL_UNREGISTER_FROM_UPDATES = 	2122;
	public static final int MSGTYPE_POOL_NEW_OBJECTS = 				2125;
	public static final int MSGTYPE_POOL_REMOVED_OBJECTS = 			2127;


	public static final int EVENT_STREAM_SEND = StringID.id("Stream send by streaming interface");

}
