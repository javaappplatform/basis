/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.network.interfaces;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.GenericsToolkit;
import github.javaappplatform.network.IClientUnit;
import github.javaappplatform.network.ISession;
import github.javaappplatform.network.interfaces.IClientInterface;
import github.javaappplatform.network.interfaces.IInterfaceType;
import github.javaappplatform.network.interfaces.ISessionInterface;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.extension.ServiceInstantiationException;
import github.javaappplatform.platform.network.interfaces.InterfaceManager.ClientInterfaceManager;
import github.javaappplatform.platform.network.interfaces.InterfaceManager.SessionInterfaceManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO javadoc
 * @author funsheep
 */
public class GetInterface
{

	public static final String EXT_POINT = "github.javaappplatform.platform.network.Interface";

	private static final Logger LOGGER = Logger.getLogger();

	private static final ClientInterfaceManager CLIENT_INTERFACES = new ClientInterfaceManager();
	private static final Map<ISession, SessionInterfaceManager> SESSION_INTERFACES = Collections.synchronizedMap(new HashMap<ISession, SessionInterfaceManager>());


	private final String iID;
	private final int msgType;


	private GetInterface(String iID)
	{
		this.iID = iID;
		this.msgType = Integer.MIN_VALUE;
	}

	private GetInterface(int msgType)
	{
		this.iID = null;
		this.msgType = msgType;
	}


	public static final GetInterface with(String id)
	{
		return new GetInterface(id);
	}

	public static final GetInterface ffor(int msgType)
	{
		return new GetInterface(msgType);
	}


	public final <I extends IInterfaceType> I ffor(ISession session)
	{
		IInterfaceType face = null;
		synchronized (SESSION_INTERFACES)
		{
			if (this.iID != null)
			{
				face = CLIENT_INTERFACES.getInterface(this.iID);
				if (face == null)
				{
					final InterfaceManager<?> mgr = SESSION_INTERFACES.get(session);
					face = mgr != null ? mgr.getInterface(this.iID) : null;
				}

				if (face == null)
				{
					try
					{
						Extension e = ExtensionRegistry.getExtension(EXT_POINT, "extname="+this.iID);
						face = register(e, session);
					}
					catch (ServiceInstantiationException e1)
					{
						LOGGER.warn("Could not instantiate requested interface " + this.iID, e1);
					}
				}
			}
			else
			{
				face = CLIENT_INTERFACES.getInterface(this.msgType);
				if (face == null)
				{
					final InterfaceManager<?> mgr = SESSION_INTERFACES.get(session);
					face = mgr != null ? mgr.getInterface(this.msgType) : null;
				}

				if (face == null)
				{
					try
					{
						Extension e = ExtensionRegistry.getExtension(EXT_POINT, "msgtypes="+this.msgType);
						face = register(e, session);
					}
					catch (ServiceInstantiationException e1)
					{
						LOGGER.warn("Could not instantiate requested interface " + this.msgType, e1);
					}
				}

			}
		}
		return GenericsToolkit.<I>convertUnchecked(face);
	}



	private static final IInterfaceType register(Extension e, ISession session) throws ServiceInstantiationException
	{
		if (e == null)
			return null;

		IInterfaceType type = (IInterfaceType) e.getService();
		if(type.type() == IInterfaceType.CLIENT_INTERFACE)
		{
			CLIENT_INTERFACES.register((IClientInterface) type, e.name, e.<String[]>getProperty("msgtypes"));
			((IClientInterface) type).init(session.client());
		}
		else if (type.type() == IInterfaceType.SESSION_INTERFACE)
		{
			ISessionInterface face = (ISessionInterface) type;
			SessionInterfaceManager mgr = SESSION_INTERFACES.get(session);
			if (mgr == null)
			{
				mgr = new SessionInterfaceManager();
				SESSION_INTERFACES.put(session, mgr);
			}
			mgr.register(face, e.name, e.<String[]>getProperty("msgtypes"));
			face.init(session);
		}
		return type;
	}

	static final void register(IClientInterface face, IClientUnit unit, String id, int... msgTypes)
	{
		if (CLIENT_INTERFACES.getInterface(id) != null)
			throw new IllegalArgumentException("Given ID " + id + " is already used.");
		CLIENT_INTERFACES.register(face, id, msgTypes);
		face.init(unit);
	}

	static final void register(ISessionInterface face, ISession session, String id, int... msgTypes)
	{
		SessionInterfaceManager mgr = SESSION_INTERFACES.get(session);
		if (mgr != null && mgr.getInterface(id) != null)
			throw new IllegalArgumentException("Given ID " + id + " is already used.");
		else if (mgr == null)
		{
			mgr = new SessionInterfaceManager();
			SESSION_INTERFACES.put(session, mgr);
		}
		mgr.register(face, id, msgTypes);
		face.init(session);
	}

	static final void dispose(ISession session)
	{
		synchronized (SESSION_INTERFACES)
		{
			CLIENT_INTERFACES.dispose(session);
			SessionInterfaceManager mgr = SESSION_INTERFACES.remove(session);
			if (mgr != null)
				mgr.dispose();
		}
	}

}
