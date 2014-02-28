/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.network.interfaces;

import github.javaappplatform.commons.util.GenericsToolkit;
import github.javaappplatform.network.IClientUnit;
import github.javaappplatform.network.ISession;
import github.javaappplatform.network.interfaces.IClientInterface;
import github.javaappplatform.network.interfaces.IInterfaceType;
import github.javaappplatform.network.interfaces.ISessionInterface;

/**
 * TODO javadoc
 * @author funsheep
 */
public class RegisterInterface<R extends RegisterInterface<R>>
{

	protected final IInterfaceType type;
	protected String id;
	protected int[] msgTypes;


	/**
	 *
	 */
	private RegisterInterface(IInterfaceType type)
	{
		this.type = type;
	}


	public static final SessionRegisterInterface instance(ISessionInterface face)
	{
		return new SessionRegisterInterface(face);
	}

	public static final ClientRegisterInterface instance(IClientInterface face)
	{
		return new ClientRegisterInterface(face);
	}

	public final R with(String _id, int... _msgTypes)
	{
		this.id = _id;
		this.msgTypes = _msgTypes;
		return GenericsToolkit.<R>convertUnchecked(this);
	}

	protected void checkParameters()
	{
		if (this.id == null || this.msgTypes.length == 0)
			throw new IllegalStateException("ID and msgType must be set.");
	}

	public static final class SessionRegisterInterface extends RegisterInterface<SessionRegisterInterface>
	{

		private SessionRegisterInterface(ISessionInterface face)
		{
			super(face);
		}


		public void at(ISession session)
		{
			this.checkParameters();
			GetInterface.register((ISessionInterface) this.type, session, this.id, this.msgTypes);
		}
	}

	public static final class ClientRegisterInterface extends RegisterInterface<ClientRegisterInterface>
	{

		private ClientRegisterInterface(IClientInterface face)
		{
			super(face);
		}


		public void at(IClientUnit unit)
		{
			this.checkParameters();
			GetInterface.register((IClientInterface) this.type, unit, this.id, this.msgTypes);
		}
	}

}
