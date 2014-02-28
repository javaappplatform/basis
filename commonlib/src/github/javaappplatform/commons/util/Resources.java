/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.util;

import github.javaappplatform.commons.log.Logger;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.logging.Level;


/**
 * Use this class to load Resourcen from the classpath.
 * @author Sven
 */
public class Resources
{
	private final static Logger LOGGER = Logger.getLogger();
	private static final ClassLoader CLASSLOADER = Resources.class.getClassLoader();
	private static final HashMap<String, URL> PACKAGES = new HashMap<String, URL>();


	private static boolean debugThisThing(String l)
	{
		LOGGER.log(Level.INFO, CLASSLOADER.toString());
		if (CLASSLOADER instanceof URLClassLoader)
		{
			final URLClassLoader ucl = (URLClassLoader)CLASSLOADER;
			final URL[] u = ucl.getURLs();
			for (final URL element : u)
			{
				LOGGER.log(Level.FINEST, "URL in ClassPath: {0}", element);
				// TODO does this work with JARs?
				InputStream in = null;
				try
				{
					final URL u2 = new URL(element, l);
					LOGGER.log(Level.FINEST, "URL to check: {0}", u2);
					in = u2.openStream();
				} catch (final Exception e)
				{
					LOGGER.log(Level.WARNING, "Unable to open stream.", e);
				} finally
				{
					Close.close(in);
				}
			}
		}
		return true;
	}

	/**
	 * Adds a resource-package to the resource-manager.
	 *
	 * @param packagename the name of the package
	 * @param location the root-folder of the packge (i.e. de/upb/mypackage)
	 */
	public static void addPackage(String packagename, String location)
	{
		location += "/indicator.txt";

		URL u1 = CLASSLOADER.getResource(location);
		if (u1 == null)
		{
			assert debugThisThing(location);
			throw new RuntimeException("could not find '" + location + "'");
		}

		try
		{
			u1 = new URL(u1, ".");
		} catch (final MalformedURLException e)
		{
			throw new RuntimeException("internal error", e);
		}

		PACKAGES.put(packagename, u1);

		LOGGER.log(Level.CONFIG, "added package {0} with URL {1}", new Object[] { packagename, u1 });
	}

	/**
	 * TODO javadoc
	 *
	 * @param parent
	 * @param packagename
	 * @param relpath
	 */
	public static void addPackage(String parent, String packagename, String relpath)
	{
		final URL u1 = getBaseURL(parent);
		relpath += "/";

		try
		{
			final URI u2 = new URI(null, null, relpath, null);
			final URL u3 = new URL(u1, u2.getRawPath());

			PACKAGES.put(packagename, u3);

			LOGGER.log(Level.CONFIG, "added package {0} with URL {1}", new Object[] { packagename, u3 });
		} catch (final URISyntaxException e)
		{
			throw new RuntimeException("malformed path '" + relpath + "'", e);
		} catch (final MalformedURLException e)
		{
			throw new RuntimeException("internal error", e);
		}
	}

	/**
	 * Returns if a package has already been added.
	 *
	 * @param packagename
	 * @return true, if package exists
	 */
	public static boolean packageExists(String packagename)
	{
		return PACKAGES.containsKey(packagename);
	}

	/**
	 * Returns the URL to the root-folder of the resource-package.
	 *
	 * @param packagename the name of the package.
	 * @return the root-URL.
	 */
	public static URL getBaseURL(String packagename)
	{
		final URL u1 = PACKAGES.get(packagename);

		if (u1 == null)
			throw new RuntimeException("unknown package: " + packagename);

		return u1;
	}

	/**
	 * Returns the URL of a resourcen given by a path relative to the root-URL of the
	 * resource-package.
	 *
	 * @param packagename the name of the package.
	 * @param relpath relative path
	 * @return the URL
	 */
	public static URL getResource(String packagename, String relpath)
	{
		final URL u1 = getBaseURL(packagename);

		relpath = relpath.replace(File.separatorChar, '/');

		try
		{
			final URI u2 = new URI(null, null, relpath, null);
			return new URL(u1, u2.getRawPath());
		} catch (final MalformedURLException e)
		{
			throw new RuntimeException("internal error", e);
		} catch (final URISyntaxException e)
		{
			throw new RuntimeException("malformed path '" + relpath + "'", e);
		}
	}


	private Resources()
	{
		// this class cannot be instanced
	}

}
