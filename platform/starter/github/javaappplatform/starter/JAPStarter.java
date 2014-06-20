/*
	This file is part of the javaappplatform platform library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)
	
	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the 
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
 */
package github.javaappplatform.starter;

import github.javaappplatform.platform.Platform;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * TODO javadoc
 * 
 * @author renken
 */
public final class JAPStarter
{

	private static final String[] DIRs = { "platform", "plugins", "third" };

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException
	{
		for (String dir : DIRs)
		{
			Path dp = Paths.get(dir);
			if (!Files.exists(dp))
				continue;
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dp, "*.jar"))
			{
				stream.forEach((p) -> {
					try
					{
						addURL(p.toUri().toURL());
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				});
			}
		}
		if (args.length > 0)
			Platform.main(args);
		else
		{
			ArrayList<Path> configs = new ArrayList<>();
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(""), "*.config"))
			{
				stream.forEach((c) -> configs.add(c));
			}
			if (configs.size() == 0)
			{
				System.out.println("Could not find any *.config files. Aborting.");
				return;
			}
			System.out.println("Found " + configs.size() + " config files. Using " + configs.get(0));
			Platform.main(new String[] { "-config", configs.get(0).toString() });
		}
	}

	private static final void addURL(URL... urls) throws Exception
	{
		URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class< ? > clazz = URLClassLoader.class;

		// Use reflection
		Method method = clazz.getDeclaredMethod("addURL", new Class[] { URL.class });
		method.setAccessible(true);
		for (URL url : urls)
			method.invoke(classLoader, url);
	}

	private JAPStarter()
	{
		// no instance
	}
}
