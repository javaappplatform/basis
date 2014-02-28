/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * This class provides methods which makes living with the incredible failing java generics more easy.
 * @author Sven
 * @author funsheep
 */
public class GenericsToolkit
{
	@SuppressWarnings("unchecked")
	public static <T>T[] createArray(Class<T> c, int size)
	{
		return (T[])Array.newInstance(c, size);
	}

	@SuppressWarnings("unchecked")
	public static <T>T convertUnchecked(Object object)
	{
		return (T) object;
	}

	@SuppressWarnings("unchecked")
	public static <T>T[] convertUnchecked(Object[] array)
	{
		return (T[])array;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> convert(Properties p)
	{
		@SuppressWarnings("rawtypes")
		final Map m = p;
		return m;
	}

	@SuppressWarnings("unchecked")
	public static <T>Iterator<T> iterator(Iterator< ? extends T> i)
	{
		return (Iterator<T>)i;
	}

	public static <T>Iterator<T> iterator(Collection< ? extends T> c)
	{
		return GenericsToolkit.<T> iterator(c.iterator());
	}

	public static <T>T[] toArray(Class<T> c1, Collection< ? extends T> c2)
	{
		return c2.toArray(GenericsToolkit.<T> createArray(c1, c2.size()));
	}
}
