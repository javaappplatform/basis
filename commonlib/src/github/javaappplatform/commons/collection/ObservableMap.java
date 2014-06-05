package github.javaappplatform.commons.collection;

import github.javaappplatform.commons.events.TalkerStub;
import github.javaappplatform.commons.util.GenericsToolkit;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Wrapper Class for Key-Value-Maps to post events for every put and remove. Update: Several
 * bugfixes and map interface implementations missing in the original implementation.
 * 
 * @author cgrote
 * @author renken
 */
public class ObservableMap<K, V> extends TalkerStub implements Map<K, V>
{

	private final Map<K, V> container;

	public ObservableMap()
	{
		this.container = new HashMap<K, V>();
	}

	public ObservableMap(int initalCapacity)
	{
		this.container = new HashMap<K, V>(initalCapacity);
	}

	public ObservableMap(int initalCapacity, float loadFactor)
	{
		this.container = new HashMap<K, V>(initalCapacity, loadFactor);
	}

	public ObservableMap(Map<K, V> map, boolean wrap)
	{
		if (map instanceof SortedMap)
			this.container = wrap ? map : new TreeMap<>(map);
		else
			this.container = wrap ? map : new HashMap<>(map);
	}

	@Override
	public V put(K name, V data)
	{
		V old = this.container.put(name, data);
		if (old == null)
			this.postEvent(IObservableCollection.E_NEW_ELEMENT, name, data);
		else if (!(data.equals(old)))
			this.postEvent(IObservableCollection.E_UPDATED_ELEMENT, name, old, data);
		return old;
	}

	@Override
	public V remove(Object name)
	{
		V removed = this.container.remove(name);
		if (removed != null)
			this.postEvent(IObservableCollection.E_REMOVED_ELEMENT, name, removed);
		return removed;
	}

	@Override
	public boolean containsKey(Object key)
	{
		return this.container.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return this.container.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		AbstractSet<java.util.Map.Entry<K, V>> set = new AbstractSet<Map.Entry<K, V>>()
			{

				class ObservableEntry implements Map.Entry<K, V>
				{

					private Map.Entry<K, V> internal;

					public ObservableEntry(Map.Entry<K, V> internal)
					{
						this.internal = internal;
					}

					/**
					 * {@inheritDoc}
					 */
					@Override
					public K getKey()
					{
						return this.internal.getKey();
					}

					/**
					 * {@inheritDoc}
					 */
					@Override
					public V getValue()
					{
						return this.internal.getValue();
					}

					/**
					 * {@inheritDoc}
					 */
					@Override
					public V setValue(V value)
					{
						V old = this.internal.setValue(value);
						ObservableMap.this.postEvent(IObservableCollection.E_UPDATED_ELEMENT, this.internal.getKey(), old, this.internal.getValue());
						return old;
					}

				}

				private Set<java.util.Map.Entry<K, V>> set = ObservableMap.this.container.entrySet();

				@Override
				public boolean remove(Object o)
				{
					if (this.set.remove(o))
					{
						Map.Entry<K, V> e = GenericsToolkit.<Map.Entry<K, V>>convertUnchecked(o);
						ObservableMap.this.postEvent(IObservableCollection.E_REMOVED_ELEMENT, e.getKey(), e.getValue());
						return true;
					}
					return false;
				}

				@Override
				public Iterator<java.util.Map.Entry<K, V>> iterator()
				{
					return new Iterator<Map.Entry<K, V>>()
						{

							private final Iterator<Map.Entry<K, V>> internal = set.iterator();
							private java.util.Map.Entry<K, V> last = null;

							@Override
							public boolean hasNext()
							{
								return this.internal.hasNext();
							}

							@Override
							public java.util.Map.Entry<K, V> next()
							{
								this.last = this.internal.next();
								return new ObservableEntry(this.last);
							}

							@Override
							public void remove()
							{
								this.internal.remove();
								java.util.Map.Entry<K, V> e = this.last;
								this.last = null;
								ObservableMap.this.postEvent(IObservableCollection.E_REMOVED_ELEMENT, e.getKey(), e.getValue());
							}
						};
				}

				@Override
				public int size()
				{
					return ObservableMap.this.size();
				}

			};
		return set;
	}

	@Override
	public V get(Object key)
	{
		return this.container.get(key);
	}

	@Override
	public boolean isEmpty()
	{
		return this.container.isEmpty();
	}

	@Override
	public Set<K> keySet()
	{
		return new AbstractSet<K>()
		{
			private Set<K> internal = ObservableMap.this.container.keySet();
			
			@Override
	        public Iterator<K> iterator()
	        {
	            return new Iterator<K>()
				{
	            	
	            	private Iterator<K> iter = internal.iterator();
	            	private K lastKey;
	            	private V lastValue;

					@Override
					public boolean hasNext()
					{
						return this.iter.hasNext();
					}

					@Override
					public K next()
					{
						this.lastKey = this.iter.next();
						this.lastValue = ObservableMap.this.get(this.lastKey);
						return this.lastKey;
					}

					@Override
					public void remove()
					{
						this.iter.remove();
						K lk = this.lastKey;
						V lv = this.lastValue;
						this.lastKey = null;
						this.lastValue = null;
						ObservableMap.this.postEvent(IObservableCollection.E_REMOVED_ELEMENT, lk, lv);
					}
				};
	        }
			@Override
	        public int size()
	        {
	            return ObservableMap.this.size();
	        }
			@Override
	        public boolean contains(Object o)
	        {
	            return ObservableMap.this.containsKey(o);
	        }
			@Override
	        public boolean remove(Object o)
	        {
	        	return ObservableMap.this.remove(o) != null;
	        }
			@Override
	        public void clear()
	        {
	            ObservableMap.this.clear();
	        }
	    };
	}

	@Override
	public void putAll(Map< ? extends K, ? extends V> m)
	{
		ArrayList<Object> addedKeys = new ArrayList<>(m.size());
		ArrayList<Object> addedValues = new ArrayList<>(m.size());
		ArrayList<Object> updatedKeys = new ArrayList<>(m.size());
		ArrayList<Object> updatedValues = new ArrayList<>(m.size());
		for (Map.Entry< ? extends K, ? extends V> e : m.entrySet())
		{
			Object old = this.container.put(e.getKey(), e.getValue());
			if (old == null)
			{
				addedKeys.add(e.getKey());
				addedValues.add(e.getValue());
			}
			else if (!old.equals(e.getValue()))
			{
				updatedKeys.add(e.getKey());
				updatedValues.add(old);
				addedValues.add(e.getValue());
			}
		}
		if (addedKeys.size() > 0)
			this.postEvent(IObservableCollection.E_NEW_ELEMENTS, addedKeys, addedValues);
		if (updatedKeys.size() > 0)
			this.postEvent(IObservableCollection.E_UPDATED_ELEMENTS, updatedKeys, updatedValues, addedValues);
	}

	@Override
	public int size()
	{
		return this.container.size();
	}

	@Override
	public Collection<V> values()
	{
		return new AbstractCollection<V>()
		{
			private Set<Map.Entry<K, V>> internal = ObservableMap.this.entrySet();
			
			@Override
	        public Iterator<V> iterator()
	        {
	            return new Iterator<V>()
				{
	            	
	            	private Iterator<Map.Entry<K, V>> iter = internal.iterator();
	            	private Map.Entry<K, V> last;

	            	
					@Override
					public boolean hasNext()
					{
						return this.iter.hasNext();
					}

					@Override
					public V next()
					{
						this.last = this.iter.next();
						return this.last.getValue();
					}

					@Override
					public void remove()
					{
						this.iter.remove();
						Map.Entry<K, V> e = this.last;
						this.last = e;
						ObservableMap.this.postEvent(IObservableCollection.E_REMOVED_ELEMENT, e.getKey(), e.getValue());
					}
				};
	        }
			@Override
	        public int size()
	        {
	            return ObservableMap.this.size();
	        }
			@Override
	        public boolean contains(Object o)
	        {
	            return ObservableMap.this.containsKey(o);
	        }
			@Override
	        public boolean remove(Object o)
	        {
	        	return ObservableMap.this.remove(o) != null;
	        }
			@Override
	        public void clear()
	        {
	            ObservableMap.this.clear();
	        }
	    };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear()
	{
		if (this.container.size() > 0)
		{
			this.container.clear();
			this.postEvent(IObservableCollection.E_COLLECTION_CLEARED);
		}
	}

}
