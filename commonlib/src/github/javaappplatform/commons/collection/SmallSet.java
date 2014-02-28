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
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * This the implementation of a small set. It works like the hashset but uses much less space and
 * datastructure-objects to maintain its data. It works with the cognition that lists/arrays have
 * much less overhead as basedatastructures for sets. This implementation is NOT intended to be used
 * for large datasets!
 *
 * @author funsheep
 */
public class SmallSet<E> implements Set<E>, Serializable
{

	private float grow_factor;
	private E[] arr;
	private int size = 0;

	/**
	 * Creates a new set with the size 4 and a growfactor of 1.75.
	 */
	public SmallSet()
	{
		this(4);
	}

	/**
	 * Creates a new set with the given capacity and a default growfactor of 1.75.
	 *
	 * @param init_capacity The initial size of this set.
	 */
	public SmallSet(int init_capacity)
	{
		this(init_capacity, 1.75f);
	}

	/**
	 * Creates a new set with the given capacity and the given growfactor which must be higher than
	 * 1. A growfactor of two means, that every time this set gets too small, the size doubles.
	 *
	 * @param init_size The initial size of this set.
	 * @param growfactor The growfactor for this set. Must be higher than 1.
	 */
	public SmallSet(int init_capacity, float growfactor)
	{
		if (growfactor <= 1f)
			throw new IllegalArgumentException("The growfactor for this set is less than one. This set would shrink instead of growing");
		this.arr = GenericsToolkit.<E>convertUnchecked(new Object[init_capacity]);
		this.grow_factor = growfactor;
	}

	/**
	 * Creates a new set with the given capacity and the given growfactor which must be higher than
	 * 1. A growfactor of two means, that every time this set gets too small, the size doubles.
	 *
	 * @param init_size The initial size of this set.
	 * @param growfactor The growfactor for this set. Must be higher than 1.
	 */
	public SmallSet(E[] arr)
	{
		this(arr.length);
		for (E e : arr)
			this.add(e);
	}

	/**
	 * Creates a new set with the given capacity and the given growfactor which must be higher than
	 * 1. A growfactor of two means, that every time this set gets too small, the size doubles.
	 *
	 * @param init_size The initial size of this set.
	 * @param growfactor The growfactor for this set. Must be higher than 1.
	 */
	public SmallSet(Collection<E> arr)
	{
		this(arr.size());
		for (E e : arr)
			this.add(e);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(E e)
	{
		if (this.find(e) == -1)
		{
			if (this.size == this.arr.length)
			{
				E[] tmp = GenericsToolkit.<E>convertUnchecked(new Object[Math.max((int)(this.size * this.grow_factor), this.size + 1)]);
				System.arraycopy(this.arr, 0, tmp, 0, this.size);
				this.arr = tmp;
			}
			this.arr[this.size] = e;
			this.size++;
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(Collection< ? extends E> c)
	{
		boolean changed = false;
		for (E e : c)
		{
			changed |= this.add(e);
		}
		return changed;
	}

	/**
	 * Adds all objects provided in the array.
	 * @param a The array to add.
	 * @return <code>true</code> if the method changed the content of the set, <code>false</code> otherwise.
	 */
	public boolean addAll(E[] a)
	{
		boolean changed = false;
		for (E e : a)
		{
			changed |= this.add(e);
		}
		return changed;
	}

	/**
	 * Adds all objects provided in the array.
	 * @param a The array to add.
	 * @return <code>true</code> if the method changed the content of the set, <code>false</code> otherwise.
	 */
	public boolean removeAll(E[] a)
	{
		boolean changed = false;
		for (E e : a)
		{
			changed |= this.remove(e);
		}
		return changed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear()
	{
		this.arr = GenericsToolkit.<E>convertUnchecked(new Object[4]);
		this.size = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(Object o)
	{
		return this.find(o) != -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsAll(Collection< ? > c)
	{
		for (Object o : c)
			if (this.find(o) == -1)
				return false;
		return true;
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
	public Iterator<E> iterator()
	{
		return new Iterator<E>()
			{

				private int index = -1;

				@Override
				public boolean hasNext()
				{
					return this.index + 1 < SmallSet.this.size;
				}

				@Override
				public E next()
				{
					this.index++;
					if (this.index == SmallSet.this.size)
						throw new NoSuchElementException("No more elements in set");
					return SmallSet.this.arr[this.index];
				}

				@Override
				public void remove()
				{
					SmallSet.this.remove(this.index);
					this.index--;
				}

			};
	}

	/**
	 * Returns one element of this set.
	 * <b>Note: This method may not return the elements uniformly distributed.</b>
	 * @return One element of this set.
	 */
	public E random()
	{
		if (this.size == 0)
			throw new NoSuchElementException("No elements in set");
		return this.arr[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(Object o)
	{
		final int index = this.find(o);
		if (index != -1)
		{
			this.remove(index);
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeAll(Collection< ? > c)
	{
		boolean changed = false;
		for (Object o : c)
		{
			changed |= this.remove(o);
		}
		return changed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean retainAll(Collection< ? > c)
	{
		for (int i = 0; i < this.size;)
		{
			if (!c.contains(this.arr[i]))
			{
				this.remove(i);
			} else
				i++;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Set<?>))
			return false;
		final Set<?> os = (Set<?>) o;
		return os.size() == this.size() && os.containsAll(this);
	}

	private int find(Object o)
	{
		for (int i = 0; i < this.size; i++)
			if (this.arr[i].equals(o))
				return i;
		return -1;
	}

	private void remove(int index)
	{
		if (index < this.size - 1)
			this.arr[index] = this.arr[this.size - 1];
		this.size--;
		this.arr[this.size] = null;
		if (this.arr.length / (float)this.size > this.grow_factor)
		{
			final E[] tmp = GenericsToolkit.<E>convertUnchecked(new Object[Math.max((int)(this.arr.length / this.grow_factor), this.size + 1)]);
			System.arraycopy(this.arr, 0, tmp, 0, this.size);
			this.arr = tmp;
		}
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
	public Object[] toArray()
	{
		final Object[] tmp = new Object[this.size];
		System.arraycopy(this.arr, 0, tmp, 0, this.size);
		return tmp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T[] toArray(T[] a)
	{
		final T[] tmp = a.length >= this.size ? a :
			GenericsToolkit.<T[]>convertUnchecked(java.lang.reflect.Array
			.newInstance(a.getClass().getComponentType(), this.size));
		System.arraycopy(this.arr, 0, tmp, 0, this.size);
		if (a.length > this.size)
			a[this.size] = null;
		return tmp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(30);
		sb.append('[');
		for (int i = 0; i < this.size()-1; i++)
		{
			sb.append(this.arr[i]);
			sb.append(", ");
		}
		if (this.size() > 0)
			sb.append(this.arr[this.size()-1]);
		sb.append(']');
		return sb.toString();
	}

	/**
	 * Save the state of the {@code SmallSet} instance to a stream (that is, serialize it).
	 */
	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException
	{
		// Write out any hidden stuff
		s.defaultWriteObject();

		s.writeFloat(this.grow_factor);

		// Write out size
		s.writeInt(this.size);

		s.writeInt(this.arr.length);

		// Write out all elements in the proper order.
		for (int i = 0; i < this.size; i++)
			s.writeObject(this.arr[i]);
	}

	/**
	 * Reconstruct the {@code SmallSet} instance from a stream (that is, deserialize it).
	 */
	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException
	{
		// Read in any hidden stuff
		s.defaultReadObject();

		this.grow_factor = s.readFloat();

		// Read in size
		this.size = s.readInt();

		this.arr = GenericsToolkit.<E>convertUnchecked(new Object[s.readInt()]);

		for (int i = 0; i < this.size; i++)
			this.arr[i] = GenericsToolkit.<E>convertUnchecked(s.readObject());
	}

	private static final long serialVersionUID = -5644652340472260104L;

}
