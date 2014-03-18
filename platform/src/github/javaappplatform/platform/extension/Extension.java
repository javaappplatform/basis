/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.extension;

import github.javaappplatform.commons.collection.SmallMap;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Arrays2;
import github.javaappplatform.commons.util.GenericsToolkit;

import java.util.Map;
import java.util.Set;

/**
 * TODO javadoc
 * @author funsheep
 */
public class Extension
{

	private static final Logger LOGGER = Logger.getLogger();


	public final String name;
	public final String[] points;
	private Class<?> clazz;
	private Object service;

	protected final SmallMap<String, Object> properties;


	protected Extension(String name, Map<String, Object> properties)
	{
		this.name = name;
		final Object point = properties.get("point");
		if (point instanceof String)
		{
			this.points = new String[1];
			this.points[0] = (String) point;
		}
		else
			this.points = (String[]) point;

		this.properties = new SmallMap<String, Object>(properties);
		this.properties.remove("point");
	}

	protected Extension(String name, Object service, Map<String, Object> properties)
	{
		this(name, properties);
		this.service = service;
	}


	public boolean extens(String point)
	{
		return Arrays2.contains(this.points, point);
	}

	public <O extends Object> O getProperty(String key)
	{
		return GenericsToolkit.<O>convertUnchecked(this.properties.get(key));
	}

	public <O extends Object> O getProperty(String key, O deFault)
	{
		if (!this.hasProperty(key))
			return deFault;
		return GenericsToolkit.<O>convertUnchecked(this.properties.get(key));
	}

	public boolean getProperty(String key, boolean deFault)
	{
		if (!this.hasProperty(key))
			return deFault;
		Object value = this.properties.get(key);
		if (value instanceof Boolean)
			return ((Boolean) value).booleanValue();
		try
		{
			return Boolean.parseBoolean(String.valueOf(this.<Object>getProperty(key)));
		}
		catch (Exception ex)
		{
			LOGGER.debug("Could not convert key " + key + " with value " +  this.getProperty(key) + " properly into a boolean value.", ex);
		}
		return deFault;
	}

	public int getProperty(String key, int deFault)
	{
		if (!this.hasProperty(key))
			return deFault;
		Object value = this.properties.get(key);
		if (value instanceof Integer)
			return ((Integer) value).intValue();
		try
		{
			return Integer.parseInt(String.valueOf(this.<Object>getProperty(key)));
		}
		catch (Exception ex)
		{
			LOGGER.debug("Could not convert key " + key + " with value " +  this.getProperty(key) + " properly into an int.", ex);
		}
		return deFault;
	}

	public double getProperty(String key, double deFault)
	{
		if (!this.hasProperty(key))
			return deFault;
		Object value = this.properties.get(key);
		if (value instanceof Integer)
			return ((Integer) value).doubleValue();
		else if (value instanceof Double)
			return ((Double) value).doubleValue();
		try
		{
			return Double.parseDouble(String.valueOf(this.<Object>getProperty(key)));
		}
		catch (Exception ex)
		{
			LOGGER.debug("Could not convert key " + key + " with value " +  this.getProperty(key) + " properly into a double.", ex);
		}
		return deFault;
	}

	public boolean hasProperty(String key)
	{
		return this.properties.containsKey(key);
	}

	public Set<Map.Entry<String, Object>> getProperties()
	{
		return this.properties.entrySet();
	}

	public <O extends Object> O getService() throws ServiceInstantiationException
	{
		if (this.clazz == null)
		{
			final String clazzprop = this.getProperty("class");
			if (clazzprop == null)
				throw new ServiceInstantiationException("This extension "+this.name+" does not provide a service object. The 'class' property is missing.");
			try
			{
				this.clazz = Class.forName(clazzprop);
			}
			catch (ClassNotFoundException e)
			{
				throw new ServiceInstantiationException("The class specified by this extension in the 'class' property cannot be found.", e);
			}
		}
		final Boolean singleton = this.getProperty("singleton");
		if (singleton != null && singleton.booleanValue())
		{
			if (this.service == null)
				try
				{
					this.service = this.clazz.newInstance();
					if (this.service instanceof IService)
						((IService) this.service).init(this);
				} catch (InstantiationException e)
				{
					throw new ServiceInstantiationException("Could not instanciate requested service.", e);
				} catch (IllegalAccessException e)
				{
					throw new ServiceInstantiationException("Could not instanciate requested service.", e);
				}
			return GenericsToolkit.<O>convertUnchecked(this.service);
		}
		try
		{
			final Object o = this.clazz.newInstance();
			if (o instanceof IService)
				((IService) o).init(this);
			return GenericsToolkit.<O>convertUnchecked(o);
		} catch (InstantiationException e)
		{
			throw new ServiceInstantiationException("Could not instanciate requested service.", e);
		} catch (IllegalAccessException e)
		{
			throw new ServiceInstantiationException("Could not instanciate requested service.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return this.name;
	}

}
