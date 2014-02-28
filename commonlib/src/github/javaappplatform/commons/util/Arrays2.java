/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.util;

import java.util.Arrays;



/**
 * Following the {@link Arrays} implementation, this class contains various methods for manipulating arrays.
 * @author funsheep
 */
public final class Arrays2
{

	public static final byte[]   EMPTY_BYTE   = {};
	public static final short[]  EMPTY_SHORT  = {};
	public static final int[]    EMPTY_INT    = {};
	public static final long[]   EMPTY_LONG   = {};
	public static final float[]  EMPTY_FLOAT  = {};
	public static final double[] EMPTY_DOUBLE = {};
	public static final String[] EMPTY_STRING = {};
	public static final Object[] EMPTY_OBJECT = {};


	public static final boolean contains(int[] arr, int a)
	{
		return contains(a, arr, 0, arr.length);
	}

	public static final boolean contains(int a, int[] arr, int off, int len)
	{
		for (int i = off; i < off+len; i++)
			if (a == arr[i])
				return true;
		return false;
	}


	public static boolean contains(Object[] os, Object o)
	{
		return indexOf(os, o) != -1;
	}

	public static int indexOf(Object[] os, Object o)
	{
		for (int i = 0; i < os.length; i++)
			if (o.equals(os[i]))
				return i;
		return -1;
	}


	private Arrays2()
	{
		//no instance
	}

}
