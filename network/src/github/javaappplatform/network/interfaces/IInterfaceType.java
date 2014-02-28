/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.interfaces;

/**
 * The base class of every network interface. Only defines the type.
 * @author funsheep
 */
public interface IInterfaceType
{

	/**
	 * Interfaces of this type are bound to a specific session. Each session has its own instance of such an interface.
	 */
	public static final int SESSION_INTERFACE = 2;
	/**
	 * These interfaces are instantiated only once per client and work for all sessions created for the client.
	 */
	public static final int CLIENT_INTERFACE = 1;


	/**
	 * Returns the type of the interface. See the constants.
	 * @return The type of the interface.
	 */
	public int type();

}
