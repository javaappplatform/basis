/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.interfaces;

import github.javaappplatform.network.ISession;
import github.javaappplatform.network.msg.IMessage;

/**
 * This network interface works in the scope of a session and its handler. Every session will have its own instance of this type.
 * @author funsheep
 */
public interface ISessionInterface extends IInterfaceType
{

	/**
	 * Initialises this interface with the session handler it works for.
	 * @param ses The session handler.
	 */
	public void init(ISession ses);

	/**
	 * This method is called by the managing session handler to process the received message.
	 * @param msg The message to process.
	 */
	public void execute(IMessage msg);

	/**
	 * This method is called when the session and the corresponding session handler are closed. Use this method to clear all resources within this interface.
	 */
	public void dispose();

}
