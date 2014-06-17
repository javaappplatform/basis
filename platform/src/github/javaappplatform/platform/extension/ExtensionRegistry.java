/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.extension;

import github.javaappplatform.commons.collection.SmallMap;
import github.javaappplatform.commons.collection.SmallSet;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.commons.events.TalkerStub;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.StringID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO javadoc
 * FIXME move servicegroup and extensiongroup mechanism into helper classes when needed
 * @author funsheep
 */
public class ExtensionRegistry
{

//	private static final class ExtGroup implements IListener
//	{
//		public final String point;
//		public final Pattern search;
//		public final SmallSet<Extension> set;
//
//		public ExtGroup(String point, Pattern searchPattern, Set<Extension> set)
//		{
//			this.point = point;
//			this.search = searchPattern;
//			this.set = new SmallSet<Extension>(set);
//			ExtensionRegistry.addListener(this);
//		}
//
//		/**
//		 * {@inheritDoc}
//		 */
//		@Override
//		public void handleEvent(Event e)
//		{
//			Extension ext = e.getData();
//			if (!ext.extens(this.point) || !matches(ext, this.search))
//				return;
//			if (e.type() == EVENT_EXTENSION_REGISTERED)
//				this.set.add(ext);
//			else
//				this.set.remove(ext);
//		}
//	}

//	private static final class ServiceGroup implements IListener
//	{
//		public final String point;
//		public final Pattern search;
//		public final SmallSet<Object> set;
//
//		public ServiceGroup(String point, Pattern searchPattern, Set<Object> set)
//		{
//			this.point = point;
//			this.search = searchPattern;
//			this.set = new SmallSet<Object>(set);
//			ExtensionRegistry.addListener(this);
//		}
//
//
//		/**
//		 * {@inheritDoc}
//		 */
//		@Override
//		public void handleEvent(Event e)
//		{
//			Extension ext = e.getData();
//			if (!ext.extens(this.point) || !matches(ext, this.search))
//				return;
//
//			try
//			{
//				if (e.type() == EVENT_EXTENSION_REGISTERED)
//					this.set.add(ext.getService());
//				else
//					this.set.remove(ext.getService());
//			} catch (ServiceInstantiationException e1)
//			{
//				LOGGER.warn("Could not get service from extension " + ext.name, e1);
//			}
//		}
//	}


	private static final Logger LOGGER = Logger.getLogger();

	public static final int EVENT_EXTENSION_REGISTERED = StringID.id("EVENT_EXTENSION_REGISTERED");

	public static final int EVENT_EXTENSION_UNREGISTERED = StringID.id("EVENT_EXTENSION_UNREGISTERED");


	private static final TalkerStub TALKER = new TalkerStub("ExtensionRegistry");

	private static final ReentrantLock EXTREG_LOCK = new ReentrantLock();

	private static final HashMap<String, Extension> EXTS_BY_NAME = new HashMap<String, Extension>();
	private static final HashMap<String, SmallSet<Extension>> EXTS_BY_POINT = new HashMap<String, SmallSet<Extension>>();
//	private static final SmallSet<ExtGroup> EXTSGROUPS = new SmallSet<ExtensionRegistry.ExtGroup>(1);
//	private static final SmallSet<ServiceGroup> SERVICESGROUP = new SmallSet<ServiceGroup>(1);


	public static final Extension registerExtension(String name, String point, Map<String, Object> properties)
	{
		if (properties == null)
			properties = new SmallMap<String, Object>();
		properties.put("point", point);
		final Extension e = new Extension(name, properties);
		registerExtension(e);
		return e;
	}

	public static final Extension registerSingleton(String name, String point, Object service, Map<String, Object> properties)
	{
		if (properties == null)
			properties = new SmallMap<String, Object>();
		properties.put("point", point);
		properties.put("singleton", Boolean.TRUE);
		if (!properties.containsKey("class"))
			properties.put("class", service.getClass().getName());
		final Extension e = new Extension(name, service, properties);
		registerExtension(e);
		return e;
	}

	static final void registerExtension(Extension...exts)
	{
		EXTREG_LOCK.lock();
		try
		{
			for (Extension e : exts)
			{
				if (getExtensionByName(e.name) != null)
					throw new IllegalStateException("An extension with the name " + e.name + " is already registered.");
				EXTS_BY_NAME.put(e.name, e);

				for (String point : e.points)
				{
					SmallSet<Extension> set = EXTS_BY_POINT.get(point);
					if (set == null)
					{
						set = new SmallSet<Extension>();
						EXTS_BY_POINT.put(point, set);
					}
					set.add(e);
				}
				TALKER.postEvent(EVENT_EXTENSION_REGISTERED, e);
				assert LOGGER.trace("Found extension: {}", e);
			}
		}
		finally
		{
			EXTREG_LOCK.unlock();
		}
	}

	public static final <O> O getService(String point)
	{
		return ExtensionRegistry.<O>getService(point, (Pattern) null);
	}

	public static final <O> O getService(String point, String searchPattern)
	{
		return getService(point, new Pattern(searchPattern));
	}

	public static final <O> O getService(String point, Pattern searchPattern)
	{
		Extension e = getExtension(point, searchPattern);
		if (e != null)
			try
			{
				return e.getService();
			} catch (ServiceInstantiationException e1)
			{
				LOGGER.warn("Could not get service from extension " + e.name, e1);
			}
		return null;
	}

	public static final <E> Set<E> getServices(String point)
	{
		return getServices(point, null, false);
	}

	public static final <E> Set<E> getServices(String point, String searchPattern)
	{
		return getServices(point, new Pattern(searchPattern), false);
	}

	private static final <E> Set<E> getServices(String point, Pattern searchPattern, boolean persistent)
	{
//		ServiceGroup g = findServices(point, searchPattern);
//		if (g != null)
//			return Collections.<Object>unmodifiableSet(g.set);

		final Set<Extension> set = getExtensions(point, searchPattern, false);
		if (set.isEmpty())
			return Collections.<E>emptySet();
		final SmallSet<E> ret = new SmallSet<>(set.size());
		for (Extension e : set)
			try
			{
				ret.add(e.<E>getService());
			} catch (ServiceInstantiationException e1)
			{
				LOGGER.warn("Could not get service from extension " + e.name, e1);
			}

//		if (persistent && ret.size() > 0)
//		{
//			EXTREG_LOCK.lock();
//			try
//			{
//				g = new ServiceGroup(point, searchPattern, ret);
//				SERVICESGROUP.add(g);
//			}
//			finally
//			{
//				EXTREG_LOCK.unlock();
//			}
//		}

		return ret;
	}

	public static final Extension getExtensionByName(String name)
	{
		EXTREG_LOCK.lock();
		try
		{
			return EXTS_BY_NAME.get(name);
		}
		finally
		{
			EXTREG_LOCK.unlock();
		}
	}

	public static final Extension getExtension(String point)
	{
		return getExtension(point, (Pattern) null);
	}

	public static final Extension getExtension(String point, String searchPattern)
	{
		return getExtension(point, new Pattern(searchPattern));
	}

	public static final Extension getExtension(String point, Pattern searchPattern)
	{
		EXTREG_LOCK.lock();
		try
		{
			final Set<Extension> set = EXTS_BY_POINT.get(point);
			if (set != null)
				for (Extension e : set)
				{
					if (matches(e, searchPattern))
						return e;
				}
			return null;
		}
		finally
		{
			EXTREG_LOCK.unlock();
		}
	}

	public static final Set<Extension> getAllExtensions()
	{
		EXTREG_LOCK.lock();
		try
		{
			SmallSet<Extension> exts = new SmallSet<Extension>(EXTS_BY_POINT.size());
			for (Set<Extension> set : EXTS_BY_POINT.values())
				exts.addAll(set);
			return exts;
		}
		finally
		{
			EXTREG_LOCK.unlock();
		}
	}

	public static final Set<Extension> getExtensions(String point)
	{
		return getExtensions(point, null, false);
	}
	public static final Set<Extension> getExtensions(String point, String searchPattern)
	{
		return getExtensions(point, new Pattern(searchPattern), false);
	}

	private static final Set<Extension> getExtensions(String point, Pattern searchPattern, boolean persistent)
	{
		EXTREG_LOCK.lock();
		try
		{
//			ExtGroup g = findExtensions(point, searchPattern);
//			if (g != null)
//				return Collections.<Extension>unmodifiableSet(g.set);

			final Set<Extension> set = EXTS_BY_POINT.get(point);
			if (set != null)
			{
				SmallSet<Extension> found = new SmallSet<Extension>(1);
				for (Extension e : set)
				{
					if (matches(e, searchPattern))
						found.add(e);
				}
//				if (persistent && found.size() > 0)
//				{
//					g = new ExtGroup(point, searchPattern, found);
//					EXTSGROUPS.add(g);
//				}

				return found;
			}
		}
		finally
		{
			EXTREG_LOCK.unlock();
		}

		return Collections.<Extension>emptySet();
	}

//	public static final void removeExtension(Extension e)
//	{
//		EXTREG_LOCK.lock();
//		try
//		{
//			for (String point : e.points)
//			{
//				EXTS_BY_POINT.get(point).remove(e);
//			}
//			TALKER.postEvent(EVENT_EXTENSION_UNREGISTERED, e);
//		}
//		finally
//		{
//			EXTREG_LOCK.unlock();
//		}
//	}

	private static final boolean matches(Extension e, Pattern searchPattern)
	{
		if (searchPattern != null)
		{
			for (Map.Entry<String, String> me : searchPattern.search.entrySet())
			{
				if ("extname".equals(me.getKey()))
				{
					if (!e.name.equals(me.getValue()))
						return false;
					continue;
				}
				Object value = e.properties.get(me.getKey());
				if (value == null)
					return false;
				if (value instanceof Object[])
				{
					if (!contains((Object[]) value, me.getValue()))
						return false;
				}
				else if (!me.getValue().equals(value.toString()))
					return false;
			}
		}
		return true;
	}

	private static final boolean contains(Object[] arr, String value)
	{
		for (Object a : arr)
			if (value.equals(a.toString()))
				return true;
		return false;
	}


//	private static final ServiceGroup findServices(String point, Pattern searchPattern)
//	{
//		EXTREG_LOCK.lock();
//		try
//		{
//			for (ServiceGroup g : SERVICESGROUP)
//				if (g.point.equals(point) && (g.search == searchPattern || g.search.equals(searchPattern)))
//					return g;
//			return null;
//		}
//		finally
//		{
//			EXTREG_LOCK.unlock();
//		}
//	}
//
//	private static final ExtGroup findExtensions(String point, Pattern searchPattern)
//	{
//		EXTREG_LOCK.lock();
//		try
//		{
//			for (ExtGroup g : EXTSGROUPS)
//				if (g.point.equals(point) && (g.search == searchPattern || g.search.equals(searchPattern)))
//					return g;
//			return null;
//		}
//		finally
//		{
//			EXTREG_LOCK.unlock();
//		}
//	}

	public static final void addListener(IListener listener)
	{
		TALKER.addListener(EVENT_EXTENSION_REGISTERED, listener);
		TALKER.addListener(EVENT_EXTENSION_UNREGISTERED, listener);
	}

	public static final void removeListener(IListener listener)
	{
		TALKER.removeListener(listener);
	}


	private ExtensionRegistry()
	{
		//no instance
	}

}
