/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.internal.events;

import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.commons.events.TalkerStub;


/**
 * TODO javadoc
 * @author funsheep
 */
public class SyncedTalkerStub extends TalkerStub
{

	protected SyncedTalkerStub()
	{
		super();
	}

	public SyncedTalkerStub(Object source)
	{
		super(source);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void addListener(int type, IListener listener, int priority)
	{
		super.addListener(type, listener, priority);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean hasListener(int type)
	{
		return super.hasListener(type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void removeListener(IListener listener)
	{
		super.removeListener(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void removeListener(int type, IListener listener)
	{
		super.removeListener(type, listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void postEvent(int type)
	{
		super.postEvent(type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void postEvent(int type, Object... data)
	{
		super.postEvent(type, data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void clear()
	{
		super.clear();
	}

}
