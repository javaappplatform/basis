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
public class ListenerSet
{

//	private static final Logger LOGGER = Logger.getLogger();

	private static final int GROW_SIZE = 4;

	protected int size = 0;
	protected int _last_valid_index = -1;
	private int[] types;
	private IListener[] listeners;


	/**
	 * Returns the size of this set.
	 * @return The size of this set.
	 */
	public int size()
	{
		return this.size;
	}

	/**
	 * This method executes the given "function" for every <type, listener> pair currently in this set.
	 * @param func The function to execute.
	 */
	public void foreachEntry(TIntObjectProcedure<IListener> func)
	{
		this.cleanup();

		int i = 0;
		while (i < this.size && func.execute(this.types[i], this.listeners[i]))
		{
			i++;
		}
	}

	/**
	 * Returns <code>true</code> when there are listeners for this type of event. <code>false</code>
	 * otherwise.
	 * @param type The event type to test.
	 * @return Returns whether there are listener to the given event type or not.
	 */
	public boolean hasHooks(int type)
	{
		this.cleanup();
		for (int i = 0; i < this.size; i++)
			if (this.types[i] == type)
				return true;
		return false;
	}

	/**
	 * Hooks a listener up for the given event type. It does not matter if the listener already
	 * listens to other event types or even to the same event type.
	 * @param type The type to listen to.
	 * @param listener The listener.
	 */
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
			this.listeners[i] = listener;
			this.size++;
			return;
		}

		if (this.types == null)
		{
			this.types = new int[GROW_SIZE];
			this.listeners = new IListener[GROW_SIZE];
		} else if (this.size == this.types.length)
		{
			final int[] newtypes = new int[this.size + GROW_SIZE];
			final IListener[] newlisteners = new IListener[this.size + GROW_SIZE];

			System.arraycopy(this.types, 0, newtypes, 0, this.size);
			System.arraycopy(this.listeners, 0, newlisteners, 0, this.size);

			this.types = newtypes;
			this.listeners = newlisteners;

		}
		this.types[this.size] = type;
		this.listeners[this.size] = listener;
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
	 * Unhooks the listener from the given event type.
	 * @param type The event type.
	 * @param listener The listener.
	 */
	public void unhook(int type, IListener listener)
	{
		for (int i = 0; i <= this._last_valid_index; i++)
		{
			if (this.types[i] == type && this.listeners[i] == listener)
				this.remove(i);
		}
	}

	/**
	 * Unhooks the listener from all event types he is hooked up to.
	 * @param listener The listener to unhook.
	 */
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
			if (this.listeners[i] == null)
			{
				while (k > i && this.listeners[k] == null)
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
			final IListener[] newlisteners = new IListener[this.size+2];

			System.arraycopy(this.types, 0, newtypes, 0, this.size);
			System.arraycopy(this.listeners, 0, newlisteners, 0, this.size);

			this.types = newtypes;
			this.listeners = newlisteners;
			this._last_valid_index = this.size-1;
		}
	}


	public void removeAllListener()
	{
		this.size = 0;
		this._last_valid_index = -1;
		this.listeners = null;
		this.types = null;
	}


}
