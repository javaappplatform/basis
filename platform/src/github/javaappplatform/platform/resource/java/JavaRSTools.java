/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.java;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.utils.URIs;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Set;

/**
 * TODO javadoc
 * @author funsheep
 */
class JavaRSTools
{


	private static final Logger LOGGER = Logger.getLogger();


	public static final Object[] detectJavaCall(URI uri)
	{
		try
		{

			ClassLoader loader = uri.getClass().getClassLoader();
			if (loader == null)
				loader = ClassLoader.getSystemClassLoader();

			String method = URIs.getFragmentWithoutSubquery(uri);
			if (method == null)
				method = "";
			if (method.endsWith("()"))
				method = method.substring(0, method.length()-2);

			String classname = uri.getSchemeSpecificPart();
			if (classname.endsWith("()"))
				classname = classname.substring(0, classname.length()-2);

			final Class<?> clazz = loader.loadClass(classname);

			Object[] ret = new Object[2];
			Set<Extension> exts = ExtensionRegistry.getExtensions("github.javaappplatform.platform.resource.mimetype");
			for (Extension ext : exts)
			{
				String[] parameters = ext.getProperty("parameters");
				if (parameters == null)
					parameters = new String[0];
				String returntype = ext.getProperty("returntype");

				try
				{
					Class<?>[] paramClasses = new Class<?>[parameters.length];
					for (int i = 0; i < parameters.length; i++)
						paramClasses[i] = Class.forName(parameters[i]);//loader.loadClass(parameters[i]);

					if (method.length() == 0 && (returntype == null || returnTypeMatching(returntype, clazz)))
					{
						ret[0] = clazz.getConstructor(paramClasses);
						ret[1] = ext.name;
						return ret;
					}
					Method m = clazz.getMethod(method, paramClasses);
					if (returntype == null || returnTypeMatching(returntype, m))
					{
						ret[0] = m;
						ret[1] = ext.name;
						return ret;
					}
				}
				catch (Exception e)
				{
					//do nothing just carry on
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.finest("Could not detect appropriate java call.", e);
			return null;
		}
		return null;
	}

	private static final boolean returnTypeMatching(String returntype, Method m) throws ClassNotFoundException
	{
		if (m.getReturnType() == null) //void
			return false;
		Class<?> rtc = Class.forName(returntype);
		return rtc.isAssignableFrom(m.getReturnType());
	}

	private static final boolean returnTypeMatching(String returntype, Class<?> c) throws ClassNotFoundException
	{
		Class<?> rtc = Class.forName(returntype);
		return rtc.isAssignableFrom(c);
	}

	private JavaRSTools()
	{
		//do nothing
	}

}
