/*
	This file is part of the d3fact common library.
	Copyright (C) 2005-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.utils;

import github.javaappplatform.commons.collection.SmallMap;
import github.javaappplatform.commons.log.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the paths. But at a more generic level. Are usable without implementing a file system, unlike {@link Path}.
 * @author funsheep
 */
public class URIs
{

	private static final Logger LOGGER = Logger.getLogger();


	/**
	 * This method converts strings to {@link URI}s without having to catch a {@link URISyntaxException}. Instead the exception is wrapped in an {@link RuntimeException}.
	 * Use this method if and only if you know that the string is a valid URI (or should be). E.g. if the string is a const uri. Don't use this method to convert unknown
	 * strings (e.g. from user input).
	 * @param str The string to convert.
	 * @return A {@link URI} object if conversion was successful.
	 * @throws RuntimeException Is thrown if the string is not a vaild {@link URI}.
	 */
	public static final URI toURI(String str)
	{
		if (str == null)
			return null;
		try
		{
			return new URI(str);
		} catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static final URI toURI(String scheme, String path, String query, String fragment)
	{
		try
		{
			return new URI(scheme, null, path, query, fragment);
		} catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}


	/**
	 * Splits the path into its segments. Slashes at the beginning and end of the path are ignored.
	 * If the path is empty, i.e. {@link URI#getPath() is <code>null</code> or empty string <code>""</code>, <code>null</code> is returned.
	 * If the path equals <code>"/"</code>, a string array of the length 0 is returned.
	 * <pre>Example:
	 * "abc/def" = [abc, def]
	 * "/abc/def" = [abc, def]
	 * "/abc/def/" = [abc, def]
	 * "" = null
	 * "/" = []
	 * </pre>
	 * @return The segments of the path. Slashes at the beginning and end are treated special.
	 */
	public static final String[] getPathSegments(URI uri)
	{
		if (uri.getPath() == null || uri.getPath().equals(""))
			return null;
		if(uri.getPath().equals("/"))
			return new String[0];

		String path = uri.getPath();
		if (path.startsWith("/"))
			path = path.substring(1);
		return path.split("/");
	}


	public static final String extractName(URI uri)
	{
		boolean dir = false;
		String path = ensureNotNull(uri.getPath());
		if (path.endsWith("/"))
		{
			path = path.substring(0, path.length()-1);
			dir = true;
		}

		int index = path.lastIndexOf('/');
		if (index != -1)
			path = path.substring(index+1);

		if (dir)
			path += '/';

		if (uri.getQuery() != null)
			path += '?' + uri.getQuery();

		if (uri.getFragment() != null)
			path += '#' + uri.getFragment();

		return path;
	}


	public static final boolean isDirectory(URI uri)
	{
		if (uri == null || uri.getPath() == null)
			return false;
		return uri.getPath().endsWith("/");
	}


	public static final URI resolveChild(URI anchor, String name)
	{
		try
		{
			anchor = URIs.ensureIsDirectory(anchor);
			return new URI(anchor.getScheme(), anchor.getRawUserInfo(), anchor.getHost(), anchor.getPort(), anchor.getPath()+name, anchor.getQuery(), anchor.getFragment());
		} catch (URISyntaxException e)
		{
			LOGGER.warn("Could not resolve child.", e);
		}
		return null;
	}

	public static final URI resolveSibling(URI anchor, String name)
	{
		String path = null;
		String query = null;
		String fragment = null;
		if (name.startsWith("?"))	//name is a query
		{
			path = anchor.getPath();
			query = name.substring(1);
		}
		else if (name.startsWith("#"))
		{
			path = anchor.getPath();
			query = anchor.getQuery();
			fragment = name.substring(1);
		}
		else
		{
			path = resolveParentPath(anchor.getPath()) + name;
		}
		try
		{
			return new URI(anchor.getScheme(), anchor.getRawUserInfo(), anchor.getHost(), anchor.getPort(), path, query, fragment);
		} catch (URISyntaxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static final URI resolveParent(URI uri)
	{
		try
		{
			return new URI(uri.getScheme(), uri.getRawAuthority(), resolveParentPath(uri.getPath()), null, null);
		} catch (URISyntaxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static final URI ensureIsDirectory(URI uri)
	{
		if (isDirectory(uri))
			return uri;
		try
		{
			return new URI(uri.getScheme(), uri.getRawAuthority(), ensureNotNull(uri.getPath()) + "/", null, null);
		} catch (URISyntaxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static final Pattern PATTERN = Pattern.compile("(_(\\d)+)?([.][^/.]+)");
	public static final URI dontOverwrite(final URI uri)
	{
		try
		{
			String suri = uri.toString();
			Matcher m = PATTERN.matcher(suri);
			if (m.find())
			{
				if (m.start(2) != -1)
				{
					int number = Integer.parseInt(m.group(2));
					final int start = m.start(2);
					final int end = m.end(2);
						return new URI(suri.substring(0, start)+String.valueOf(++number)+suri.substring(end));
				}
				return new URI(suri.substring(0, m.start(3))+"_1"+suri.substring(m.start(3)));
			}
		} catch (URISyntaxException e)
		{
			throw new IllegalStateException("Could not determine uri format.", e);
		}
		throw new IllegalStateException("Could not determine uri format.");
	}

	private static final String resolveParentPath(String path)
	{
		if (path == null)
			return "";
		final int index = path.lastIndexOf('/');
		if (index == -1)
			return "";
		return path.substring(0, index+1);
	}

	private static final String ensureNotNull(String s)
	{
		if (s != null)
			return s;
		return "";
	}

	public static final String getFragmentWithoutSubquery(URI uri)
	{
		final String frag = uri.getFragment();
		if (frag != null)
		{
			int index = frag.indexOf('?');
			if (index >= 0)
				return frag.substring(0, index);
		}
		return frag;
	}

	public static final String getQuery(URI uri)
	{
		if (uri.getFragment() == null || uri.getQuery() != null)
			return uri.getQuery();

		final String frag = uri.getFragment();
		if (frag != null)
		{
			int index = frag.indexOf('?');
			if (index >= 0)
				return frag.substring(index+1);
		}
		return null;
	}

	public static final Map<String, String> parseQuery(URI uri)
	{
		String query = getQuery(uri);
		if (query == null)
			return Collections.emptyMap();

		final String[] keyvalues = query.split("&");
		final SmallMap<String, String> parameters = new SmallMap<>(keyvalues.length);
		for (String kv : keyvalues)
		{
			int index = kv.indexOf('=');
			if (index <= 0)
				parameters.put(kv, "");
			else
				parameters.put(kv.substring(0, index), kv.substring(index+1));
		}
		return parameters;
	}

	private URIs()
	{
		//no instance
	}
}
