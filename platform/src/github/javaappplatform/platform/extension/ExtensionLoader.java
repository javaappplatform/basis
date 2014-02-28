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
import github.javaappplatform.commons.json.JSONReader;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.platform.utils.CmdLineTools;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonToken;

/**
 * TODO javadoc
 * @author funsheep
 */
public class ExtensionLoader
{

	private static final Logger LOGGER = Logger.getLogger();


	public static final void loadExtensionsFromArgs(String[] args) throws IOException
	{
		String exts = CmdLineTools.getValue("extensions", args);
		if (exts != null)
		{
			String[] extensions = exts.split(":");
			loadExtensions(extensions);
		}
	}

	public static final void loadExtensions(String[] extensions) throws IOException
	{
		for (String name : extensions)
		{
			LOGGER.info("Trying to load extension " + name);
			ExtensionRegistry.registerExtension(load(name));
		}
	}


	@SuppressWarnings("resource")
	private static final Extension[] load(String name) throws IOException
	{
		final String path = name.replace('.', '/') + ".ext";
		URL url = ExtensionLoader.class.getClassLoader().getResource(path);
		if (url == null)
			throw new IOException("Could not find extension: " + name);

		final SmallSet<Extension> exts = new SmallSet<Extension>(1);	//there can be several declarations in one file
		JSONReader reader = null;
		try
		{
			reader = new JSONReader(url);

			reader.nextToken(JsonToken.START_OBJECT);
			while (reader.nextToken() != JsonToken.END_OBJECT)
			{
				String extname = reader.getCurrentName();
				reader.nextToken(JsonToken.START_OBJECT);

				SmallMap<String, Object> properties = new SmallMap<String, Object>();
				while (reader.nextToken() != JsonToken.END_OBJECT)
				{
					String propname = reader.getCurrentName();
					JsonToken token = reader.nextToken();
					Object propvalue;
					switch (token)
					{
						case VALUE_FALSE:
							propvalue = Boolean.FALSE;
							break;
						case VALUE_TRUE:
							propvalue = Boolean.TRUE;
							break;
						case VALUE_NUMBER_FLOAT:
							propvalue = Float.valueOf(reader.getCurrentText());
							break;
						case VALUE_NUMBER_INT:
							propvalue = Integer.valueOf(reader.getCurrentText());
							break;
						case VALUE_STRING:
							propvalue = reader.getCurrentText();
							break;
						case START_ARRAY:
							final ArrayList<String> list = new ArrayList<String>();
							while (reader.nextToken() != JsonToken.END_ARRAY)
							{
								list.add(reader.getCurrentText());
							}
							propvalue = list.toArray(new String[list.size()]);
							break;
						case VALUE_NULL:
						default:
							propvalue = null;
							reader.skipChildren();
					}
					try
					{
						if ("class".equals(propname))
							propvalue = Class.forName((String) propvalue);
						if ("api".equals(propname))
							propvalue = Class.forName((String) propvalue);
					}
					catch (ClassNotFoundException e)
					{
						throw new JsonParseException("Could not find defined class " + propvalue + " for property " + propname + " in extension " + extname + " in " + name, reader.getCurrentLocation());
					}
					catch (ClassCastException e)
					{
						throw new JsonParseException("Property "+propname+" in extension definition "+ extname +" in "+ name +" does not define required class name.", reader.getCurrentLocation());
					}
					if (propname.equals("extname"))
						throw new JsonParseException("Extension " + extname + " in "+ name +" has a property called 'extname'. This is a reserved word.", reader.getCurrentLocation());
					properties.put(propname, propvalue);
				}
				if (!properties.containsKey("point"))
					throw new JsonParseException("The extension "+ extname +" in " + name + " is missing the field 'point'", reader.getCurrentLocation());
				exts.add(new Extension(extname, properties));
			}
		}
		catch (IOException ex)
		{
			throw new IOException("Could not parse extension " + name + " correctly.", ex);
		}
		finally
		{
			Close.close(reader);
		}
		return exts.toArray(new Extension[exts.size()]);
	}


	private ExtensionLoader()
	{
		//no instance
	}

}
