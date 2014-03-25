/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)
	
	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the 
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.util;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;

import eu.infomas.annotation.AnnotationDetector;
import eu.infomas.annotation.AnnotationDetector.TypeReporter;
import github.javaappplatform.commons.log.Logger;

/**
 * Utility class to handle annotations. E.g. search for them at a specific type or within a package.
 * @author funsheep
 */
public class Annotations
{
	
	private static final Logger LOGGER = Logger.getLogger();


	/**
	 * Searches for an annotation in the class hierarchy of the given class and its interfaces.
	 * @return The annotation (with its values) if found. Otherwise <code>null</code>.
	 */
	public static <T extends Annotation> T searchAt(Class<?> clazz, Class<T> annotationType)
	{
		while (clazz != Object.class)
		{
			T ann = clazz.getAnnotation(annotationType);
			if (ann != null)
				return ann;
			
			clazz = clazz.getSuperclass();
		}

		for (Class<?> face : clazz.getInterfaces())
		{
			T ann = face.getAnnotation(annotationType);
			if (ann != null)
				return ann;
		}

		return null;
	}
	
	public static <T extends Annotation> Class<?>[] searchForTypesWith(final Class<T> annotationType)
	{
		final ArrayList<Class<?>> found = new ArrayList<>();
		final TypeReporter reporter = new TypeReporter()
		{
			
			private final Class<T>[] toSearchFor = GenericsToolkit.convertUnchecked(new Class[1]);
			
			{
				this.toSearchFor[0] = annotationType;
			}

		
			@Override
			public Class<T>[] annotations()
			{
				return this.toSearchFor;
			}
			
			@Override
			public void reportTypeAnnotation(Class< ? extends Annotation> annotation, String className)
			{
				try
				{
					found.add(Class.forName(className));
				}
				catch (ClassNotFoundException e)
				{
					LOGGER.debug("Could not find class {} on classpath. This should never happen.", className);
				}
			}
		};
		AnnotationDetector dc = new AnnotationDetector(reporter);
		try
		{
			dc.detect("com.lodige.loomi.engine");
		}
		catch (IOException e)
		{
			LOGGER.debug("Should not happen.", e);
			e.printStackTrace();
		}
		return found.toArray(new Class<?>[found.size()]);
	}
	

	private Annotations()
	{
		// no instance
	}

}
