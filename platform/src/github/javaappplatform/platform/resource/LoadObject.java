/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource;

import github.javaappplatform.commons.util.GenericsToolkit;
import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.resource.internal.ILoader;
import github.javaappplatform.platform.utils.URIs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO javadoc
 * @author funsheep
 */
public class LoadObject
{


	private final IResource resource;
	private final Map<String, Object> _properties = new HashMap<>();


	private LoadObject(IResource resource)
	{
		this.resource = resource;
		this._properties.putAll(URIs.parseQuery(resource.uri()));
	}


	public static final LoadObject from(IResource resource)
	{
		return new LoadObject(resource);
	}

	public LoadObject with(Map<String, Object> properties)
	{
		this._properties.putAll(properties);
		return this;
	}

	public LoadObject withParameter(String key, Object value)
	{
		this._properties.put(key, value);
		return this;
	}

	public <O extends Object> O as(String returntype) throws IOException
	{
		final String searchPattern = "mimetype=" + this.resource.mimetype() + (returntype != null ? ",returntype=" + returntype : ",default=true");

		ILoader loader = (ILoader) ExtensionRegistry.getService(ILoader.EXTENSION_POINT, searchPattern);
		if (loader == null)
			throw new IOException("Could not find appropriate loader for requested resource: " + this.resource.uri() + " Searchpattern was: " + searchPattern);

		return GenericsToolkit.<O>convertUnchecked(loader.load(this.resource, returntype, this._properties));
	}

	public <O extends Object> O as(Class<O> returntype) throws IOException
	{
		return this.as(returntype.getName());
	}

	public Object asUnknown() throws IOException
	{
		return this.as((String) null);
	}

}
