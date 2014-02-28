/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.internal;

import github.javaappplatform.commons.collection.SmallSet;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.resource.IResourceAPI.OpenOption;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * TODO javadoc
 * @author funsheep
 */
public class ResourceTools
{

	private static final Logger LOGGER = Logger.getLogger();


	public static final IResourceSystem getRS(String scheme)
	{
		return (IResourceSystem) ExtensionRegistry.getService(IResourceSystem.EXT_POINT, "scheme="+scheme);
	}

	public static final String mimetype(String uri)
	{
		Extension e = ExtensionRegistry.getExtension("github.javaappplatform.platform.resource.mimetype", "fileext="+_fileExt(uri));
		return e != null ? e.name : null;
	}

	public static final String mimetype(URI uri)
	{
		return mimetype(uri.getSchemeSpecificPart());
	}

	public static final String fileExt(String mimetype)
	{
		Extension e = ExtensionRegistry.getExtensionByName(mimetype);
		if (e != null)
		{
			Object o = e.getProperty("fileext");
			if (o instanceof String)
				return o.toString();
			else if (o instanceof String[] && ((String[]) o).length > 0)
				return ((String[]) o)[0];
		}
		return null;
	}

	public static final String getFileExtForURI(URI uri)
	{
		return _fileExt(uri.toString());
	}

	private static final String _fileExt(String uri)
	{
		Set<Extension> set = ExtensionRegistry.getExtensions("github.javaappplatform.platform.resource.mimetype", null, true);
		for (Extension e : set)
		{
			Object o = e.getProperty("fileext");
			if (o instanceof String && uri.endsWith(o.toString().toLowerCase()))
				return o.toString();
			else if (o instanceof String[])
				for (String s : (String[]) o)
					if (uri.endsWith(s.toLowerCase()))
						return s.toLowerCase();
		}
		return getRawFileExt(uri).toLowerCase();
	}

	private static int getExtDotPos(String filename)
	{
		final int i1 = filename.lastIndexOf('.');
		final int i2 = filename.lastIndexOf('/');
		final int i3 = filename.lastIndexOf(File.separatorChar);

		if (i1 < 0 || i1 < i2 || i1 < i3)
			return -1;

		return i1;
	}

	/**
	 * Returns the type (usually ."xxx" where x is a character), of a given filename. Detailed, it returns the substring after the last dot '.' in the given
	 * string.
	 *
	 * @param filename The filename to parse.
	 * @return The substring after the last dot '.' in the given string.
	 */
	public static String getRawFileExt(String filename)
	{
		final int i = getExtDotPos(filename);
		return i < 0 ? "" : filename.substring(i + 1);
	}

	public static String getRawFileExt(URL file) throws URISyntaxException
	{
		final URI u = new URI(file.toExternalForm());
		return getRawFileExt(u.getPath());
	}


	private static final Pattern OPTIONS_PATTERN = Pattern.compile("options=(([_A-Z]+)([+][_A-Z]+)*)");
	public static final OpenOption[] extractOptions(URI uri)
	{
		if (uri == null || uri.getQuery() == null)
			return new OpenOption[0];

		String suri = uri.getQuery().toString();
		Matcher m = OPTIONS_PATTERN.matcher(suri);

		if (!m.find() || m.groupCount() == 0)
			return new OpenOption[0];

		SmallSet<OpenOption> ret = new SmallSet<>();
		String options = m.group(1);
		for (String opt : options.split("[+]"))
		{
			OpenOption option = OpenOption.resolve(opt);
			if (option != null)
				ret.add(option);
			else
				LOGGER.info("Could not match option: " + opt);
		}
		return ret.toArray(new OpenOption[ret.size()]);
	}

	public static final URI cleanURIFromOptions(URI uri)
	{
		if (uri == null || uri.getQuery() == null)
			return uri;

		String suri = uri.getQuery().toString();
		Matcher m = OPTIONS_PATTERN.matcher(suri);

		if (!m.find() || m.groupCount() == 0)
			return uri;

		String query = m.replaceAll("");
		if (query.startsWith("&"))
			query = query.substring(1);
		if (query.contains("&&"))
			query = query.replace("&&", "&");
		if (query.trim().length() == 0)
			query = null;

		try
		{
			return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), query, uri.getFragment());
		} catch (URISyntaxException e)
		{
			LOGGER.fine("Could not clean URI "+uri+" from options parameters.", e);
			return uri;
		}
	}

	private ResourceTools()
	{
		//no instance
	}

}
