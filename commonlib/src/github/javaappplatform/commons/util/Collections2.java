/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)
	
	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the 
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.util;

import github.javaappplatform.commons.collection.IObservableCollection;
import github.javaappplatform.commons.collection.ObservableMap;
import github.javaappplatform.commons.collection.ObservableSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author funsheep
 *
 */
public class Collections2
{

	public static final <E> IObservableCollection<E> observableCollection(Collection<E> collection)
	{
		if (collection instanceof Set)
			return new ObservableSet<E>(GenericsToolkit.<Set<E>>convertUnchecked(collection), true);
		throw new UnsupportedOperationException("Given collection at the moment not supported: " + collection.getClass());
	}
	
	public static final <E> ObservableSet<E> observableSet(Set<E> set)
	{
		return new ObservableSet<E>(set, true);
	}
	
	public static final <K,V> ObservableMap<K, V> observableMap(Map<K, V> map)
	{
		return new ObservableMap<K,V>(map, true);
	}

	private Collections2()
	{
		//no instance
	}

}
