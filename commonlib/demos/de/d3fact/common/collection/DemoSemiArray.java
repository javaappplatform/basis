/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package de.d3fact.common.collection;

import github.javaappplatform.commons.collection.SemiDynamicByteArray;

import java.util.Arrays;

/**
 * TODO javadoc
 * @author funsheep
 */
public class DemoSemiArray
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		byte[] dummy = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 11 };
//		byte[] dummy = { 1, 2, 3, 4, 5 };
		SemiDynamicByteArray arr = new SemiDynamicByteArray(400);
		arr.putAll(dummy);
		arr.putAll(dummy);
		arr.putAll(dummy);
		arr.put((byte) 1);
		arr.putAll(new byte[] {2});
		System.out.println(arr.size());
		for (byte[] a : arr.getRAWData())
			System.out.println(Arrays.toString(a));
		System.out.println(Arrays.toString(arr.getData()));

		byte[] dest = new byte[10];
		arr.cursor(3);
		arr.getData(dest);
		System.out.println(Arrays.toString(dest));

		long start = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++)
		{
			arr.putAll(dummy);
		}
		System.out.println(System.currentTimeMillis() - start);
	}

}
