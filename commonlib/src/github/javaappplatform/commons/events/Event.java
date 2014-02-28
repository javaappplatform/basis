/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.events;

import github.javaappplatform.commons.util.GenericsToolkit;

/**
 * This is a generic event class. It holds information about the type of event,
 * the source and a generic data field for application use.
 *
 * <b> This class is not intended to be subclassed.</b>
 *
 * This class is serializable. When it is serialized it will save
 * (and restore) the source and data field if they are themselves serializable. Any
 * non-serializable source or data Object will be skipped during serialization.
 *
 * @author funsheep
 */
public class Event// implements Serializable
{

	int type;
	Object source;
	Object data;


	/**
	 * Convenient Constructor. Creates a new event with the given source, type information and data object.
	 *
	 * @param source The source of the event. Who created this object.
	 * @param type The type of the event.
	 * @param data The data to set
	 * @see {@link #setData(Object)}
	 */
	public Event(Object source, int type, Object... data)
	{
		this.source = source;
		this.type   = type;
		this.setData(data);
	}


	/**
	 * Returns the type of the event.
	 * @return The type.
	 */
	public int type()
	{
		return this.type;
	}

	/**
	 * Returns the source of this event. The method is generic, to remove the needed cast in the
	 * method call. Now its possible to do something like this:
	 * <code>
	 * MySource source = event.getSource();
	 * </code>
	 *
	 * instead of:
	 * <code>
	 * MySource source = (MySource) event.getSource();
	 * </code>
	 *
	 * If the type is not correct a ClassCastException is thrown (obviously).
	 *
	 * @param <T> The type of the source.
	 * @return The source of this event.
	 */
	public <T> T getSource()
	{
		return GenericsToolkit.<T>convertUnchecked(this.source);
	}

	/**
	 * Sets the data field for this event. This can be used by the application
	 * to store information on the event for later use.
	 * @param data The data to be set.
	 */
	public void setData(Object... data)
	{
		if (data == null || data.length == 0)
			this.data = null;
		else if (data.length == 1)
			this.data = data[0];
		else
			this.data = data;
	}

	/**
	 * Returns the data field of this event. The method is generic, to remove the needed cast in the
	 * method call. Now its possible to do something like this:
	 * <code>
	 * MyData data = event.getData();
	 * </code>
	 *
	 * instead of:
	 * <code>
	 * MyData data = (MyData) event.getData();
	 * </code>
	 *
	 * If the type is not correct a ClassCastException is thrown (obviously).
	 *
	 * @param <T> The type of the data.
	 * @return The data field of this event.
	 */
	public <T> T getData()
	{
		return GenericsToolkit.<T>convertUnchecked(this.data);
	}

	/**
	 * Use this, if data is an array.
	 * @param i Specifies which entry to get.
	 * @return The ith entry of the array.
	 */
	public <T> T getIthData(int i)
	{
		Object[] arr = (Object[]) this.data;
		return GenericsToolkit.<T>convertUnchecked(arr[i]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return "Event["+this.type+"][Source="+String.valueOf(this.source)+"][Data="+String.valueOf(this.data)+"]";
	}


//    /**
//     * @serialData int type, Object or null Source, Object or null data.
//     * <p>
//     * At serialization time we skip non-serializable fields.
//     */
//    private void writeObject(ObjectOutputStream s) throws IOException
//    {
//    	s.writeInt(this.type);
//    	if (this.source instanceof Serializable)
//    		s.writeObject(this.source);
//    	else
//    		s.writeObject(null);
//
//    	if (this.data instanceof Serializable)
//    		s.writeObject(this.data);
//    	else
//    		s.writeObject(null);
//    }
//
//
//    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException
//    {
//    	this.type = s.readInt();
//    	this.source = s.readObject();
//    	this.data = s.readObject();
//    }
//
//	/** sVUID */
//	private static final long serialVersionUID = 8305337465539381767L;

}
