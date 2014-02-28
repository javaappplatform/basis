/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.network.interfaces;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.network.ISession;
import github.javaappplatform.network.interfaces.IClientInterface;
import github.javaappplatform.network.interfaces.IInterfaceType;
import github.javaappplatform.network.interfaces.ISessionInterface;
import github.javaappplatform.network.msg.IMessage;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO javadoc
 * @author funsheep
 */
class InterfaceManager<I extends IInterfaceType>
{

	private static final Logger LOGGER = Logger.getLogger();


	protected final Map<String, I> interfaces = new HashMap<String, I>();
	protected final TIntObjectMap<I> interfacesByMSGtypes = new TIntObjectHashMap<I>(30);


	public synchronized void register(I face, String id, String... msgTypes)
	{
		assert !this.interfaces.containsKey(id);
		this.interfaces.put(id, face);
		for (String msg : msgTypes)
		{
			try
			{
				this.interfacesByMSGtypes.put(Integer.parseInt(msg), face);
			}
			catch (NumberFormatException e)
			{
				LOGGER.severe("Could not register interface for messageType " + msg + ". This is not a valid integer number.");
			}
		}
	}

	public synchronized void register(I face, String id, int... msgTypes)
	{
		assert !this.interfaces.containsKey(id);
		this.interfaces.put(id, face);
		for (int msg : msgTypes)
		{
			this.interfacesByMSGtypes.put(msg, face);
		}
	}


	public synchronized I getInterface(String id)
	{
		return this.interfaces.get(id);
	}

	/**
	 * This method is called by the {@link #dispatch(IMessage)} method to retrieve an interface that can process the given message type.
	 * Subclasses should override this method with a loading mechanism for interfaces if the interface could not be found in the internal datastructure.
	 * @param msgType The type of the message that should be processed and for which no interface could be found.
	 * @return The loaded interface or <code>null</code> if no interface could be found for the given id.
	 */
	public synchronized I getInterface(int msgType)
	{
		return this.interfacesByMSGtypes.get(msgType);
	}


	public static class ClientInterfaceManager extends InterfaceManager<IClientInterface>
	{

		public void dispose(ISession session)
		{
			for (IClientInterface face : this.interfaces.values())
				face.dispose(session);
		}
	}

	public static class SessionInterfaceManager extends InterfaceManager<ISessionInterface>
	{

		public void dispose()
		{
			for (ISessionInterface face : this.interfaces.values())
				face.dispose();
		}
	}

}
