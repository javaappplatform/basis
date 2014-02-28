/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.server;

import github.javaappplatform.network.internal.InternalMessageAPI;
import github.javaappplatform.network.msg.Converter;

/**
 * TODO javadoc
 * @author funsheep
 */
public class ServerUtils
{

	private static final int INITACK_MSGBODY_LENGTH_V2 = 4;


	public static final byte[] initAckV2(int clientID)
	{
		byte[] msg = new byte[4+INITACK_MSGBODY_LENGTH_V2];
		Converter.putIntBig(msg, 0, INITACK_MSGBODY_LENGTH_V2);
		Converter.putIntBig(msg, 4, clientID);
		return msg;
	}

	public static final byte[] initAckV3(int clientID)
	{
		return initReturn(InternalMessageAPI.INITACK_MSG_TYPE, clientID);
	}

	public static final byte[] initError(int errorID)
	{
		return initReturn(InternalMessageAPI.INITERR_MSG_TYPE, errorID);
	}

	private static final byte[] initReturn(byte type, int field2)
	{
		byte[] msg = new byte[4+InternalMessageAPI.INITRET_MSGBODY_LENGTH];
		Converter.putIntBig(msg, 0, InternalMessageAPI.INITRET_MSGBODY_LENGTH);
		msg[4] = type;
		Converter.putIntBig(msg, 5, field2);
		return msg;
	}


	private ServerUtils()
	{
		//no instance
	}

}
