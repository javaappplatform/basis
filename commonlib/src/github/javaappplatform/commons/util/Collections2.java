/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)
	
	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the 
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author funsheep
 *
 */
public class Collections2
{

	public static final <E> Collection<E> wrapArray(Object[] array)
	{
		return new ArrayWrapper<>(array, array.length, true);
	}


	private static class ArrayWrapper<E> implements Collection<E>, Serializable
	{

		private static final float GROW_FACTOR = 1.75f;

		private final boolean immutable;
		private boolean original = true;
		private Object[] arr;
		private int size;


		public ArrayWrapper(Object[] sharedArray, int size, boolean immutable)
		{
			this.arr = sharedArray;
			this.size = size;
			this.immutable = immutable;
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean add(E e)
		{
			if (this.immutable)
				throw new UnsupportedOperationException("Immutable Collection.");
			this.replaceOriginal();
			if (this.size == this.arr.length)
			{
				E[] tmp = GenericsToolkit.<E>convertUnchecked(new Object[Math.max((int)(this.size * GROW_FACTOR), this.size + 1)]);
				System.arraycopy(this.arr, 0, tmp, 0, this.size);
				this.arr = tmp;
			}
			this.arr[this.size] = e;
			this.size++;
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean addAll(Collection< ? extends E> c)
		{
			if (this.immutable)
				throw new UnsupportedOperationException("Immutable Collection.");
			for (E e : c)
				this.add(e);
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void clear()
		{
			if (this.immutable)
				throw new UnsupportedOperationException("Immutable Collection.");
			this.size = 0;
			this.arr = new Object[10];
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
						return this.index + 1 < ArrayWrapper.this.size;
					}

					@Override
					public E next()
					{
						this.index++;
						if (this.index == ArrayWrapper.this.size)
							throw new NoSuchElementException("No more elements in set");
						return GenericsToolkit.<E>convertUnchecked(ArrayWrapper.this.arr[this.index]);
					}

					@Override
					public void remove()
					{
						if (ArrayWrapper.this.immutable)
							throw new UnsupportedOperationException("Immutable Collection.");
						ArrayWrapper.this.replaceOriginal();
						ArrayWrapper.this.remove(this.index);
					}

				};
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean remove(Object o)
		{
			if (this.immutable)
				throw new UnsupportedOperationException("Immutable Collection.");
			this.replaceOriginal();
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
			if (this.immutable)
				throw new UnsupportedOperationException("Immutable Collection.");
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
			if (this.immutable)
				throw new UnsupportedOperationException("Immutable Collection.");
			this.replaceOriginal();
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

		private int find(Object o)
		{
			for (int i = 0; i < this.size; i++)
				if (this.arr[i].equals(o))
					return i;
			return -1;
		}

		private void remove(int index)
		{
			assert !this.immutable;
			assert !this.original;
			if (index < this.size - 1)
				System.arraycopy(this.arr, index+1, this.arr, index, this.size()-index-1);
			this.size--;
			this.arr[this.size] = null;
			if (this.arr.length / (float)this.size > GROW_FACTOR)
			{
				final E[] tmp = GenericsToolkit.<E>convertUnchecked(new Object[Math.max((int)(this.arr.length / GROW_FACTOR), this.size + 1)]);
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

		private void replaceOriginal()
		{
			if (this.original)
			{
				Object[] newArr = new Object[Math.max(2, this.size()*2)];
				System.arraycopy(this.arr, 0, newArr, 0, this.size());
				this.arr = newArr;
				this.original = false;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			Iterator<E> it = iterator();
			if (! it.hasNext())
				return "[]";

			StringBuilder sb = new StringBuilder();
			sb.append('[');
			for (;;) {
				E e = it.next();
				sb.append(e == this ? "(this Collection)" : e);
				if (! it.hasNext())
					return sb.append(']').toString();
				sb.append(',').append(' ');
			}
		}

		/**
		 * Save the state of the {@code SmallSet} instance to a stream (that is, serialize it).
		 */
		private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException
		{
			// Write out any hidden stuff
			s.defaultWriteObject();

			// Write out size
			s.writeInt(this.size);

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

			// Read in size
			this.size = s.readInt();

			this.arr = GenericsToolkit.<E>convertUnchecked(new Object[this.size]);

			for (int i = 0; i < this.size; i++)
				this.arr[i] = GenericsToolkit.<E>convertUnchecked(s.readObject());
		}

		private static final long serialVersionUID = -5644652340472260104L;

	}


	private Collections2()
	{
		//no instance
	}

}
