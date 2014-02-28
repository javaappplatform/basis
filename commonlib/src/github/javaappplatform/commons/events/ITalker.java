/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.events;


/**
 * An interface indicating that implementing classes fire events, to which listener can subscribe.
 * @author funsheep
 */
public interface ITalker
{

	/** Listeners of this priority are called first. */
	public static final int PRIORITY_HIGH = 0;
	/** Listeners of this priority are called after the high priority listeners. */
	public static final int PRIORITY_NORMAL = 1;
	/** Listeners of this priority are called after the normal priority listeners. */
	public static final int PRIORITY_LOW = 2;


	/**
	 * This method does the same as calling {@link #addListener(int, IListener, int)} with {@link #PRIORITY_NORMAL}.
	 * @param type The event type to listen to.
	 * @param listener The listener.
	 */
	public void addListener(int type, IListener listener);

	/**
	 * Hooks a listener (with the given priority) up for the given event type. It does not matter if the listener already
	 * listens to other event types or even to the same event type.
	 * @param type The type to listen to.
	 * @param listener The listener.
	 * @param priority The priority.
	 */
	public void addListener(int type, IListener listener, int priority);

	/**
	 * Returns, whether this talker has a listener that listens for events for the given type or not.
	 * @param type The event type to check.
	 * @return <code>true</code> if there is at least one listener that listens for events of the given type, <code>false</code> otherwise.
	 */
	public boolean hasListener(int type);

	/**
	 * Unhooks the listener from all event types he is hooked up to.
	 * @param listener The listener to unhook.
	 */
	public void removeListener(IListener listener);

	/**
	 * Removes the listener from listening to the given event type.
	 * @param type The event type.
	 * @param listener The listener.
	 */
	public void removeListener(int type, IListener listener);

}
