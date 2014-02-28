/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.server;

import github.javaappplatform.network.IClientUnit;

import java.net.InetSocketAddress;

/**
 * TODO javadoc
 * @author funsheep
 */
public interface IRemoteClientUnit extends IClientUnit
{

	/**
	 * Returns the associated socket address of the remote computer.
	 * @return The associated socket address of the remote computer.
	 */
	public InetSocketAddress address(int protocol);

}
