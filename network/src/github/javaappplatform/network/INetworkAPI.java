/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network;

/**
 * TODO javadoc
 * @author funsheep
 */
public interface INetworkAPI
{

	public static final int PROTOCOL_VERSION = 4;

	public static final int STATE_NOT_STARTED = 1;
	public static final int STATE_STARTING = 2;
	public static final int STATE_RUNNING = 3;
	public static final int STATE_NOT_CONNECTED = 4;
	public static final int STATE_CONNECTION_PENDING = 7;
	public static final int STATE_CONNECTED = 8;
	public static final int STATE_SHUTDOWN = 19;
	public static final int STATE_CLOSING = 20;

	public static final int EVENT_CLIENT_CONNECTED = 1;
	public static final int EVENT_MSG_RECEIVED = 2;
	public static final int EVENT_MSG_SEND = 3;
	public static final int EVENT_STATE_CHANGED = 12;
	public static final int EVENT_SESSION_STARTED = 17;
	public static final int ERROR_SOCKET_NOT_ACCEPTED = 14;
	public static final int ERROR_UNKNOWN = 16;

	public static final int MAX_UDP_PAKET_SIZE = 4096;
	public static final int MAX_SERVER_CONNECTION_QUEUE = 10;
	public static final int CONNECTION_TIMEOUT = 1000 * 15;

	public static final int LITTLE_ENDIAN = 2;
	public static final int BIG_ENDIAN = 1;

	public static final int RELIABLE_PROTOCOL = 0;
	public static final int FAST_UNRELIABLE_PROTOCOL = 1;

	public static final int MAX_SEND_MESSAGE_COUNTER = 50;


}
