package github.javaappplatform.commons.collection;

import github.javaappplatform.commons.events.TalkerStub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Wrapper class for elements.
 * @author renken
 */
public class ObservableSet<E> extends TalkerStub implements Set<E>, IObservableCollection<E>
{

	private final Set<E> container;

	public ObservableSet()
	{
		this.container = new HashSet<>();
	}

	public ObservableSet(int initalCapacity)
	{
		this.container = new HashSet<>(initalCapacity);
	}

	public ObservableSet(int initalCapacity, float loadFactor)
	{
		this.container = new HashSet<>(initalCapacity, loadFactor);
	}

	public ObservableSet(Set<E> set, boolean wrap)
	{
		this.container = wrap ? set : new HashSet<>(set);
	}

	@Override
	public boolean add(E data)
	{
		boolean added = this.container.add(data);
		if (added)
			this.postEvent(IObservableCollection.E_NEW_ELEMENT, data);
		return added;
	}

	@Override
	public boolean remove(Object data)
	{
		boolean removed = this.container.remove(data);
		if (removed)
			this.postEvent(IObservableCollection.E_REMOVED_ELEMENT, data);
		return removed;
	}

	@Override
	public boolean contains(Object data)
	{
		return this.container.contains(data);
	}

	@Override
	public boolean isEmpty()
	{
		return this.container.isEmpty();
	}

	@Override
	public boolean addAll(Collection< ? extends E> c)
	{
		ArrayList<E> added = new ArrayList<>(c.size());
		for (E e : c)
			if (this.container.add(e))
				added.add(e);
		if (added.size() > 0)
		{
			this.postEvent(IObservableCollection.E_NEW_ELEMENTS, added);
			return true;
		}
		return false;
	}

	@Override
	public int size()
	{
		return this.container.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> iterator()
	{
		return new Iterator<E>()
		{
			private Iterator<E> intern = ObservableSet.this.container.iterator();
			private E last = null;

			@Override
			public boolean hasNext()
			{
				return this.intern.hasNext();
			}

			@Override
			public E next()
			{
				this.last = this.intern.next();
				return this.last;
			}

			@Override
			public void remove()
			{
				this.intern.remove();
				E removed = this.last;
				this.last = null;
				ObservableSet.this.postEvent(IObservableCollection.E_REMOVED_ELEMENT, removed);
				
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] toArray()
	{
		return this.container.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T>T[] toArray(T[] a)
	{
		return this.container.toArray(a);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsAll(Collection< ? > c)
	{
		return this.container.containsAll(c);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean retainAll(Collection< ? > c)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeAll(Collection<?> c)
	{
		ArrayList<Object> removed = new ArrayList<>();
		for (Object e : c)
			if (this.container.remove(e))
				removed.add(e);
		if (removed.size() > 0)
		{
			this.postEvent(IObservableCollection.E_REMOVED_ELEMENTS, removed);
			return true;
		}
		return false;
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
