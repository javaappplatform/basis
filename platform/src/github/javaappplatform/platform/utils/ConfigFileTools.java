/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.utils;

import github.javaappplatform.commons.json.JSONReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonToken;

/**
 * TODO javadoc
 * @author funsheep
 */
public class ConfigFileTools
{

	public static final String[] read(String path) throws IOException
	{
		JSONReader reader = null;
		final ArrayList<String> args = new ArrayList<String>();
		try
		{
			reader = new JSONReader(new File(path));
			reader.nextToken(JsonToken.START_OBJECT);

			while(reader.nextToken() != JsonToken.END_OBJECT)
			{
				args.add("-"+reader.getCurrentName());
				JsonToken token = reader.nextToken();
				if (token == JsonToken.START_ARRAY)
				{
					ArrayList<String> list = new ArrayList<String>(16);
					while ((token = reader.nextToken()) != JsonToken.END_ARRAY)
					{
						list.add(reader.getCurrentText());
					}
					StringBuilder sb = new StringBuilder(list.size() * 10);
					for (int i = 0; i < list.size()-1; i++)
					{
						sb.append(list.get(i));
						sb.append(':');
					}
					if (list.size() > 0)
						sb.append(list.get(list.size()-1));
					args.add(sb.toString());
				}
				else if (token == JsonToken.START_OBJECT)
				{
					throw new JsonParseException("JSON Objects are not supported in config files.", reader.getCurrentLocation());
				}
				else if (token != JsonToken.VALUE_NULL)
					args.add(reader.getCurrentText());
			}
		}
		finally
		{
			if (reader != null)
				reader.close();
		}
		return args.toArray(new String[args.size()]);
	}

}
