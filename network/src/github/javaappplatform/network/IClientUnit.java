/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network;

import github.javaappplatform.commons.events.ITalker;

import java.io.Closeable;
import java.util.Collection;

/**
 * TODO javadoc
 * @author funsheep
 */
public interface IClientUnit extends ITalker, Closeable
{

	public byte type();

	public int clientID();

	public ISession startSession();

	public ISession getSession(int sessionID);

	public Collection<ISession> getAllSessions();

	public int state();

	@Override
	public void close();

	public void shutdown();

}
