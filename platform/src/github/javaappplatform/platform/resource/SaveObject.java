/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource;

import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.resource.internal.ISaver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO javadoc
 * @author funsheep
 */
public class SaveObject
{

	private final IResource resource;
	private final Map<String, Object> _properties = new HashMap<>();


	private SaveObject(IResource resource)
	{
		this.resource = resource;
	}


	public static final SaveObject to(IResource resource)
	{
		return new SaveObject(resource);
	}


	public SaveObject with(Map<String, Object> properties)
	{
		this._properties.putAll(properties);
		return this;
	}

	public SaveObject withParameter(String key, Object value)
	{
		this._properties.put(key, value);
		return this;
	}


	public void save(Object o) throws IOException
	{
		final String searchPattern = "mimetype=" + this.resource.mimetype();

		ISaver saver = (ISaver) ExtensionRegistry.getService(ISaver.EXTENSION_POINT, searchPattern);
		if (saver == null)
			throw new IOException("Could not find appropriate loader for requested resource: " + this.resource.uri() + " Searchpattern was: " + searchPattern);

		saver.save(o, this.resource, this._properties);
	}

}
