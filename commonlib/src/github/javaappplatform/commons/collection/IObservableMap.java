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
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author renken
 *
 */
public interface IObservableMap<K, V> extends IObservableCollection
{
	public int size();
	public boolean isEmpty();
	public boolean containsKey(Object key);
	public boolean containsValue(Object value);
	public V get(Object key);
	public Set<K> keySet();
	public Collection<V> values();
	public Set<Entry<K, V>> entrySet();
}
