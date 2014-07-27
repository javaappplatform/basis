/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.events;

import gnu.trove.procedure.TIntObjectProcedure;



/**
 * This abstract class implements simple methods to work with a ListenerHook. Clients may use this class to clear their classes from these methods.
 * This implementation utilizes a lazy loading pattern.
 *
 * @author funsheep
 */
public class TalkerStub implements ITalker, ITalker.Inner
{


	private static class PostEventFunc implements TIntObjectProcedure<IListener>
	{

		public final Event e;

		public PostEventFunc(Event e)
		{
			this.e = e;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean execute(int a, IListener b)
		{
			if (this.e.type == a)
				b.handleEvent(this.e);
			return true;
		}

	}


	private final Class<IListenerSet> setImpl;
	protected final Object source;
	protected IListenerSet[] sets;


	protected TalkerStub()
	{
		this((Class<IListenerSet>) null);
	}

	protected TalkerStub(Class<IListenerSet> setImpl)
	{
		this.source = this;
		this.setImpl = setImpl;
	}

	public TalkerStub(Object source)
	{
		this(source, null);
	}

	public TalkerStub(Object source, Class<IListenerSet> setImpl)
	{
		this.source = source;
		this.setImpl = setImpl;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addListener(int type, IListener listener, int priority)
	{
		if (this.sets == null)
			this.sets = new IListenerSet[3];
		for (IListenerSet set : this.sets)
			if (set != null && set != this.sets[priority])
				set.unhook(type, listener);
		if (this.sets[priority] == null)
			this.sets[priority] = this.newSet();
		this.sets[priority].hookUp(type, listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasListener(int type)
	{
		if (this.sets != null)
		{
			for (IListenerSet set : this.sets)
				if (set != null && set.hasHooks(type))
					return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeListener(int type, IListener listener)
	{
		if (this.sets == null)
			return;
		for (IListenerSet set : this.sets)
			if (set != null)
				set.unhook(type, listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeListener(IListener listener)
	{
		if (this.sets == null)
			return;
		for (IListenerSet set : this.sets)
			if (set != null)
				set.unhook(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void postEvent(int type)
	{
		this.postEvent(type, (Object[]) null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void postEvent(int type, Object... data)
	{
		if (!this.hasListener(type))
			return;
		Event event = new Event(this.source, type);
		if (data != null && data.length == 1)
			event.setData(data[0]);
		else if (data != null && data.length > 0)
			event.setData(data);

		for (IListenerSet set : this.sets)
			if (set != null && set.size() > 0)
				set.foreachEntry(new PostEventFunc(event));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear()
	{
		this.sets = null;
	}

	private IListenerSet newSet()
	{
		if (this.setImpl == null)
			return new ListenerSet();
		try
		{
			return this.setImpl.newInstance();
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
}
