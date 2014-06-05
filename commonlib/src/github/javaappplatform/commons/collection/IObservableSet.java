/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)
	
	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the 
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.collection;

import java.util.Collection;

/**
 * @author renken
 *
 */
public interface IObservableSet<E> extends IObservableCollection, Iterable<E>
{
	public int size();
	public boolean isEmpty();
	public boolean contains(Object object);
	public Object[] toArray();
	public <T> T[] toArray(T[] array);
	public boolean containsAll(Collection<?> c);

}
