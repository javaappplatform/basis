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
 * TODO javadoc
 * @author funsheep
 */
public interface IInnerTalker extends ITalker
{

	/**
	 * Creates a new event from the given information. The source is set to the
	 * in the constructor given source object. The event is then posted to all
	 * listeners, listening to the event type. The data field of the event is set
	 * to <code>null</code>.
	 * @param type The type of the event.
	 */
	public void postEvent(int type);

	/**
	 * Creates a new event from the given information. The source is set to the
	 * in the constructor given source object. The event is then posted to all
	 * listeners, listening to the event type.
	 * @param type The type of the event.
	 * @param data The data field.
	 */
	public void postEvent(int type, Object... data);

	/**
	 * Clears the stub. All listeners are removed.
	 */
	public void clear();

}
