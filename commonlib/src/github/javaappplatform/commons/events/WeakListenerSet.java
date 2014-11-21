/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.events;

import java.lang.ref.WeakReference;

import gnu.trove.procedure.TIntObjectProcedure;

/**
 * This is a utility class that manages a set of listeners and their types of events
 * for which they listen. The internal fields are only created when needed.
 * Also the internal datastructure grows and shrinks when appropriate.
 *
 * This class is serializable. When it is serialized it will save
 * (and restore) any listeners that are themselves serializable.  Any
 * non-serializable listeners will be skipped during serialization.
 *
 * @author funsheep
 */
public class WeakListenerSet implements IListenerSet
{

//	private static final Logger LOGGER = Logger.getLogger();

	private static final int GROW_SIZE = 4;

	protected int size = 0;
	protected int _last_valid_index = -1;
	private int[] types;
	private WeakReference<IListener>[] listeners;


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size()
	{
		return this.size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void foreachEntry(TIntObjectProcedure<IListener> func)
	{
		this.cleanup();

		int i = 0;
		while (i < this._last_valid_index)
		{
			IListener lis = this.listeners[i].get();
			if (lis != null && !func.execute(this.types[i], lis))
				break;
			i++;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasHooks(int type)
	{
		this.cleanup();
		for (int i = 0; i < this.size; i++)
			if (this.types[i] == type)
				return true;
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void hookUp(int type, IListener listener)
	{
		if (this.isAlreadyHooked(type, listener))
			return;

		//if internal_size > size -> search for place to put
		if (this._last_valid_index > this.size-1)
		{
			int i = 0;
			while (this.listeners[i] != null)
				i++;

			this.types[i] = type;
			this.listeners[i] = new WeakReference<>(listener);
			this.size++;
			return;
		}

		if (this.types == null)
		{
			this.types = new int[GROW_SIZE];
			this.listeners = new WeakReference[GROW_SIZE];
		} else if (this.size == this.types.length)
		{
			final int[] newtypes = new int[this.size + GROW_SIZE];
			final WeakReference<IListener>[] newlisteners = new WeakReference[this.size + GROW_SIZE];

			System.arraycopy(this.types, 0, newtypes, 0, this.size);
			System.arraycopy(this.listeners, 0, newlisteners, 0, this.size);

			this.types = newtypes;
			this.listeners = newlisteners;

		}
		this.types[this.size] = type;
		this.listeners[this.size] = new WeakReference<>(listener);
		this._last_valid_index = this.size;
		this.size++;
	}

	private boolean isAlreadyHooked(int type, IListener listener)
	{
		if (this.types == null)
			return false;
		for (int i = 0; i < this._last_valid_index; i++)
			if (this.types[i] == type && this.listeners[i] == listener)
				return true;
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unhook(int type, IListener listener)
	{
		for (int i = 0; i <= this._last_valid_index; i++)
		{
			if (this.types[i] == type && this.listeners[i] == listener)
				this.remove(i);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unhook(IListener listener)
	{
		for (int i = 0; i <= this._last_valid_index; i++)
			if (this.listeners[i] == listener)
				this.remove(i);
	}

	protected void remove(int index)
	{
		this.listeners[index] = null;
		this.size--;
	}

	protected void cleanup()
	{
		if (this._last_valid_index == this.size-1)
			return;

		int i = 0;
		int k = this._last_valid_index;
		while (i < k)
		{
			if (this.listeners[i] == null || this.listeners[i].get() == null)
			{
				while (k > i && this.listeners[k] == null || this.listeners[k].get() == null)
				{
					k--;
				}
				this._last_valid_index = k-1;
				if (k > i)
				{
					this.types[i] = this.types[k];
					this.listeners[i] = this.listeners[k];
					this.listeners[k] = null;
				}
			}
			i++;
		}

		if (this.size > 0 && this.size+2 < this.types.length / 2)
		{
			final int[] newtypes = new int[this.size+2];
			@SuppressWarnings("unchecked")
			final WeakReference<IListener>[] newlisteners = new WeakReference[this.size+2];

			System.arraycopy(this.types, 0, newtypes, 0, this.size);
			System.arraycopy(this.listeners, 0, newlisteners, 0, this.size);

			this.types = newtypes;
			this.listeners = newlisteners;
			this._last_valid_index = this.size-1;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeAllListener()
	{
		this.size = 0;
		this._last_valid_index = -1;
		this.listeners = null;
		this.types = null;
	}


}
