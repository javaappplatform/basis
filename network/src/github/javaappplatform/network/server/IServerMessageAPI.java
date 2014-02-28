/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.server;

/**
 * TODO javadoc
 * @author funsheep
 */
public interface IServerMessageAPI
{

	public static final int _NO_ERROR = 0;

	public static final int ERROR_UNSUPPORTED_PROTOCOL_VERSION = 1;

	public static final int ERROR_UNKNOWN = -1;

	public static final int ERROR_WRONG_INIT_FORMAT  = 3;

	public static final int ERROR_SERVER_SHUTTING_DOWN  = 4;

	public static final int ERROR_CLIENT_ID_NOLONGER_RESERVED = 5;

}
