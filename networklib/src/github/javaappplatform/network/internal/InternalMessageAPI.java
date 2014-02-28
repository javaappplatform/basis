/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.internal;

public interface InternalMessageAPI
{

	public static final int OFFSET_LENGTH = 0;
	public static final int OFFSET_TCP_SESSIONID = OFFSET_LENGTH + 4;
	public static final int OFFSET_TCP_ORDER = OFFSET_TCP_SESSIONID + 4;
	public static final int OFFSET_TCP_MSGTYPE = OFFSET_TCP_ORDER + 8;
	public static final int OFFSET_TCP_BODY = OFFSET_TCP_MSGTYPE + 4;


	public static final int OFFSET_UDP_CLIENTID = OFFSET_LENGTH + 4;
	public static final int OFFSET_UDP_SESSIONID = OFFSET_TCP_SESSIONID + 4;
	public static final int OFFSET_UDP_ORDER = OFFSET_TCP_ORDER + 4;
	public static final int OFFSET_UDP_MSGTYPE = OFFSET_TCP_MSGTYPE + 4;
	public static final int OFFSET_UDP_BODY = OFFSET_TCP_BODY + 4;

	public static final int LENGTH_TCP_HEADER = OFFSET_TCP_BODY-4;	//-length
	public static final int LENGTH_UDP_HEADER = OFFSET_UDP_BODY-4;	//-length

	public static final int INIT_OFFSET_PROTOCOL_VERSION = 0;
	public static final int INIT_OFFSET_CLIENT_ID = 1;
	public static final int INIT_OFFSET_UDPPORT = 5;
	public static final int INIT_OFFSET_TYPE = 9;
	public static final int INIT_MSGBODY_LENGTH = 10;

	public static final int INITRET_MSGBODY_LENGTH = 5;
	public static final byte INITACK_MSG_TYPE = 1;
	public static final byte INITERR_MSG_TYPE = 2;
	public static final int INITRET_OFFSET_TYPE = 0;
	public static final int INITRET_OFFSET_FIELD2 = 1;

	public static final int MSGTYPE_START_NEW_SESSION = -10;
	public static final int MSGTYPE_ACK_NEW_SESSION = -11;
	public static final int MSGTYPE_ERROR_NEW_SESSION = -12;

	public static final int MSGTYPE_CLOSE_SESSION = -20;
	public static final int MSGTYPE_ACK_CLOSE_SESSION = -21;

}
