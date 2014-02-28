/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.interfaces;

import github.javaappplatform.network.IClientUnit;
import github.javaappplatform.network.ISession;
import github.javaappplatform.network.msg.IMessage;

/**
 * This interface is instantiated once per client. It should be hooked up with each session the client creates. The methods to process messages and for disposal of resources
 * are designed to get an instance of {@link ISessionHandler} so the implementing code can differentiate between the different sessions.
 * @author funsheep
 */
public interface IClientInterface extends IInterfaceType
{

	/**
	 * This method is called by a session handler to process a message received through the session the session handler manages.
	 * @param ses The session handler who got the message.
	 * @param msg The message to process.
	 */
	public void execute(ISession ses, IMessage msg);

	/**
	 * This method is called when a session is closed and therefore also the corresponding session handler. In this method all resources associated with the closed session and
	 * session handler can be disposed.
	 * @param ses The session handler that is closed.
	 */
	public void dispose(ISession ses);

	/**
	 * Initializes this interface with the ClientUnit it works for.
	 * @param cunit The CientUnit.
	 */
	public void init(IClientUnit cunit);
}
