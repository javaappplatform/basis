/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.collection;

import github.javaappplatform.commons.util.GenericsToolkit;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A map based on arrays. This map is useful when having a small number (< 20) of objects. Then the linear search is faster than the hash approach. Also,
 * since this implementation uses arrays, its relative memory efficient. It does not need to construct a whole datastructure with lots of objects.
 * Adding an object needs constant time, removing and searching in the worst case O(n). Where the constant factor is minimal.
 * @author funsheep
 */
public class SmallMap<K, V> implements Map<K, V>, Serializable
{


	private class SmallMapEntry implements java.util.Map.Entry<K, V>
	{
		private final int index;

		public SmallMapEntry(int index)
		{
			this.index = index;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public K getKey()
		{
			return SmallMap.this.keyarray[this.index];
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public V getValue()
		{
			return SmallMap.this.valarray[this.index];
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public V setValue(V value)
		{
			final V old = SmallMap.this.valarray[this.index];
			SmallMap.this.valarray[this.index] = value;
			return old;
		}

	}

	private class SmallMapKeyset implements Set<K>
	{

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean add(K e)
		{
			throw new UnsupportedOperationException("Not supported. Defined by Map interface");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean addAll(Collection< ? extends K> c)
		{
			throw new UnsupportedOperationException("Not supported. Defined by Map interface");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void clear()
		{
			SmallMap.this.clear();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean contains(Object o)
		{
			return SmallMap.this.containsKey(o);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean containsAll(Collection< ? > c)
		{
			for (Object o : c)
				if (!SmallMap.this.containsKey(o))
					return false;
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isEmpty()
		{
			return SmallMap.this.isEmpty();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Iterator<K> iterator()
		{
			return new Iterator<K>(){

				private int index = -1;

				@Override
				public boolean hasNext()
				{
					return this.index+1 < SmallMap.this.size;
				}

				@Override
				public K next() throws NoSuchElementException
				{
					this.index++;
					if (this.index == SmallMap.this.size)
						throw new NoSuchElementException("No more elements in keyset");
					return SmallMap.this.keyarray[this.index];
				}

				@Override
				public void remove()
				{
					SmallMap.this.remove(this.index);
				}

			};
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean remove(Object o)
		{
			final int sizeold = SmallMap.this.size;
			SmallMap.this.remove(o);
			return SmallMap.this.size != sizeold;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean removeAll(Collection< ? > c)
		{
			final int sizeold = SmallMap.this.size;
			for (Object o : c)
				SmallMap.this.remove(o);
			return SmallMap.this.size != sizeold;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean retainAll(Collection< ? > c)
		{
			final int sizeold = SmallMap.this.size;
			for (int i = 0; i < SmallMap.this.size; )
			{
				if (!c.contains(SmallMap.this.keyarray[i]))
					SmallMap.this.remove(i);
				else
					i++;
			}
			return SmallMap.this.size != sizeold;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int size()
		{
			return SmallMap.this.size;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object[] toArray()
		{
			Object[] tmp = new Object[SmallMap.this.size];
			System.arraycopy(SmallMap.this.keyarray, 0, tmp, 0, SmallMap.this.size);
			return tmp;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <T> T[] toArray(T[] a)
		{
			final T[] tmp = a.length >= SmallMap.this.size ? a :
				GenericsToolkit.<T[]>convertUnchecked(java.lang.reflect.Array
				.newInstance(a.getClass().getComponentType(), SmallMap.this.size));
			System.arraycopy(SmallMap.this.keyarray, 0, tmp, 0, SmallMap.this.size);
			if (a.length > SmallMap.this.size)
				a[SmallMap.this.size] = null;
			return tmp;
		}
	}

	private class SmallMapValueCollection implements Collection<V>
	{

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean add(V e)
		{
			throw new UnsupportedOperationException("Not supported. Defined by Map interface");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean addAll(Collection< ? extends V> c)
		{
			throw new UnsupportedOperationException("Not supported. Defined by Map interface");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void clear()
		{
			SmallMap.this.clear();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean contains(Object o)
		{
			return SmallMap.this.containsValue(o);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean containsAll(Collection< ? > c)
		{
			for (Object o : c)
				if (!SmallMap.this.containsValue(o))
					return false;
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isEmpty()
		{
			return SmallMap.this.isEmpty();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Iterator<V> iterator()
		{
			return new Iterator<V>(){

				private int index = -1;

				@Override
				public boolean hasNext()
				{
					return this.index+1 < SmallMap.this.size;
				}

				@Override
				public V next() throws NoSuchElementException
				{
					this.index++;
					if (this.index == SmallMap.this.size)
						throw new NoSuchElementException("No more elements in keyset");
					return SmallMap.this.valarray[this.index];
				}

				@Override
				public void remove()
				{
					SmallMap.this.remove(this.index);
				}

			};
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean remove(Object o)
		{
			return SmallMap.this.removeValue(o);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean removeAll(Collection< ? > c)
		{
			final int sizeold = SmallMap.this.size;
			for (Object o : c)
				this.remove(o);
			return SmallMap.this.size != sizeold;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean retainAll(Collection< ? > c)
		{
			final int sizeold = SmallMap.this.size;
			for (int i = 0; i < SmallMap.this.size; )
			{
				if (!c.contains(SmallMap.this.valarray[i]))
					SmallMap.this.remove(i);
				else
					i++;
			}
			return SmallMap.this.size != sizeold;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int size()
		{
			return SmallMap.this.size;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object[] toArray()
		{
			Object[] tmp = new Object[SmallMap.this.size];
			System.arraycopy(SmallMap.this.valarray, 0, tmp, 0, SmallMap.this.size);
			return tmp;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <T> T[] toArray(T[] a)
		{
			final T[] tmp = a.length >= SmallMap.this.size ? a :
				GenericsToolkit.<T[]>convertUnchecked(java.lang.reflect.Array
				.newInstance(a.getClass().getComponentType(), SmallMap.this.size));
			System.arraycopy(SmallMap.this.valarray, 0, tmp, 0, SmallMap.this.size);
			if (a.length > SmallMap.this.size)
				a[SmallMap.this.size] = null;
			return tmp;
		}

	}


	private K[] keyarray;
	private V[] valarray;
	private int size = 0;
	private float grow_factor;


	/**
	 *
	 */
	public SmallMap()
	{
		this(4);
	}

	public SmallMap(int init_capacity)
	{
		this(init_capacity, 1.75f);
	}

	public SmallMap(int init_capacity, float growfactor)
	{
		if (growfactor <= 1f)
			throw new IllegalArgumentException("The growfactor for this set is less than one. This set would shrink instead of growing");
		this.keyarray = GenericsToolkit.<K>convertUnchecked(new Object[init_capacity]);
		this.valarray = GenericsToolkit.<V>convertUnchecked(new Object[init_capacity]);
		this.grow_factor = growfactor;
	}

	public SmallMap(Map<? extends K, ? extends V> tocopy)
	{
		this(tocopy.size());
		for (Entry<? extends K, ? extends V> e : tocopy.entrySet())
		{
			this.put(e.getKey(), e.getValue());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsKey(Object key)
	{
		return this.findKey(key) != -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsValue(Object value)
	{
		return this.findValue(value) != -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		final SmallSet<Entry<K, V>> set = new SmallSet<Entry<K,V>>(this.size);
		for (int i = 0; i < this.size; i++)
			set.add(new SmallMapEntry(i));
		return set;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V get(Object key)
	{
		final int index = this.findKey(key);
		if (index != -1)
			return this.valarray[index];
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<K> keySet()
	{
		return new SmallMapKeyset();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V put(K key, V value)
	{
		final int index = this.findKey(key);
		if (index == -1)
		{
			if (this.size == this.keyarray.length)
			{
				final K[] keytmp = GenericsToolkit.<K>convertUnchecked(new Object[Math.max((int) (this.size * this.grow_factor), this.size+1)]);
				System.arraycopy(this.keyarray, 0, keytmp, 0, this.size);
				this.keyarray = keytmp;
				final V[] valtmp = GenericsToolkit.<V>convertUnchecked(new Object[keytmp.length]);
				System.arraycopy(this.valarray, 0, valtmp, 0, this.size);
				this.valarray = valtmp;
			}
			this.keyarray[this.size] = key;
			this.valarray[this.size] = value;
			this.size++;
			return null;
		}
		final V old = this.valarray[index];
		this.valarray[index] = value;
		return old;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putAll(Map< ? extends K, ? extends V> m)
	{
		for (Entry<? extends K, ? extends V> e : m.entrySet())
			this.put(e.getKey(), e.getValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V remove(Object key)
	{
		final int index = this.findKey(key);
		if (index != -1)
		{
			return this.remove(index);
		}
		return null;
	}

	public boolean removeValue(Object value)
	{
		int index = this.findValue(value);
		if (index == -1)
			return false;
		while (index != -1)
		{
			this.remove(index);
			index = this.findValue(value);
		}
		return true;
	}

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
	public Collection<V> values()
	{
		return new SmallMapValueCollection();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear()
	{
		this.keyarray = GenericsToolkit.<K>convertUnchecked(new Object[4]);
		this.valarray = GenericsToolkit.<V>convertUnchecked(new Object[4]);
		this.size = 0;
	}

	private int findKey(Object o)
	{
		for (int i = 0; i < this.size; i++)
			if (this.keyarray[i].equals(o))
				return i;
		return -1;
	}

	private int findValue(Object o)
	{
		for (int i = 0; i < this.size; i++)
			if (this.valarray[i].equals(o))
				return i;
		return -1;
	}

	private V remove(int index)
	{
		final V old = this.valarray[index];
		if (index < this.size-1)
		{
			this.keyarray[index] = this.keyarray[this.size-1];
			this.valarray[index] = this.valarray[this.size-1];
		}
		this.size--;
		this.keyarray[this.size] = null;
		this.valarray[this.size] = null;
		if (this.keyarray.length / (float)this.size > this.grow_factor)
		{
			final K[] keytmp = GenericsToolkit.<K>convertUnchecked(new Object[Math.max((int) (this.keyarray.length / this.grow_factor), this.size+1)]);
			System.arraycopy(this.keyarray, 0, keytmp, 0, this.size);
			this.keyarray = keytmp;
			final V[] valtmp = GenericsToolkit.<V>convertUnchecked(new Object[keytmp.length]);
			System.arraycopy(this.valarray, 0, valtmp, 0, this.size);
			this.valarray = valtmp;
		}
		return old;
	}

	/**
	 * {@inheritDoc}
	 * Node: Copied from {@link AbstractMap} implementation.
	 */
	@Override
	public String toString()
	{
		Iterator<Entry<K, V>> i = entrySet().iterator();
		if (!i.hasNext())
			return "{}";

		StringBuilder sb = new StringBuilder();
		sb.append('{');
		for (;;)
		{
			Entry<K, V> e = i.next();
			K key = e.getKey();
			V value = e.getValue();
			sb.append(key == this ? "(this Map)" : key);
			sb.append('=');
			sb.append(value == this ? "(this Map)" : value);
			if (!i.hasNext())
				return sb.append('}').toString();
			sb.append(',').append(' ');
		}
	}

	/**
	 * Save the state of the {@code SmallMap} instance to a stream (that is, serialize it).
	 *
	 * @serialData Emits the comparator used to order this set, or {@code null} if it obeys its
	 *             elements' natural ordering (Object), followed by the size of the set (the number
	 *             of elements it contains) (int), followed by all of its elements (each an Object)
	 *             in order (as determined by the set's Comparator, or by the elements' natural
	 *             ordering if the set has no Comparator).
	 */
	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException
	{
		// Write out any hidden stuff
		s.defaultWriteObject();

		s.writeFloat(this.grow_factor);

		// Write out size
		s.writeInt(this.size);

		s.writeInt(this.keyarray.length);

		// Write out all elements in the proper order.
		for (int i = 0; i < this.size; i++)
		{
			s.writeObject(this.keyarray[i]);
			s.writeObject(this.valarray[i]);
		}
	}

	/**
	 * Reconstitute the {@code SmallMap} instance from a stream (that is, deserialize it).
	 */
	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException
	{
		// Read in any hidden stuff
		s.defaultReadObject();

		this.grow_factor = s.readFloat();

		// Read in size
		this.size = s.readInt();

		final int arrSize = s.readInt();
		this.keyarray = GenericsToolkit.<K>convertUnchecked(new Object[arrSize]);
		this.valarray = GenericsToolkit.<V>convertUnchecked(new Object[arrSize]);

		for (int i = 0; i < this.size; i++)
		{
			this.keyarray[i] = GenericsToolkit.<K>convertUnchecked(s.readObject());
			this.valarray[i] = GenericsToolkit.<V>convertUnchecked(s.readObject());
		}
	}

	private static final long serialVersionUID = -2381643473599262470L;

}
