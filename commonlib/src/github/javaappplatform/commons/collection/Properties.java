/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)
	
	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the 
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.collection;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.GenericsToolkit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author renken
 *
 */
public class Properties
{

	private static final Logger LOGGER = Logger.getLogger();


	private final Map<String, Object> properties;


	public Properties(Map<String, Object> properties)
	{
		if (properties.size() <= 16)
			this.properties = new SmallMap<String, Object>(properties);
		else
			this.properties = new HashMap<String, Object>(properties);
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

	public <V extends Enum<V>> V getEnum(String key, Class<V> enumType)
	{
		if (!this.hasProperty(key))
			throw new IllegalStateException(key + " is unknown property.");
		return Enum.valueOf(enumType, this.<String>getProperty(key));
	}


	public boolean hasProperty(String key)
	{
		return this.properties.containsKey(key);
	}

	public Set<Map.Entry<String, Object>> entrySet()
	{
		return this.properties.entrySet();
	}

}
