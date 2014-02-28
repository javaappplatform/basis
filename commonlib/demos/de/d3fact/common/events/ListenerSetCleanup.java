/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package de.d3fact.common.events;

import java.util.Arrays;

/**
 * TODO javadoc
 * @author funsheep
 */
public class ListenerSetCleanup
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

		int[] types = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		Integer[] listeners = { Integer.valueOf(0), Integer.valueOf(1), null, null, Integer.valueOf(4), null, Integer.valueOf(6), Integer.valueOf(7), null, null };
//		Integer[] listeners = { null, null, null, null, null, null, null, null, null, null };
		int _last_valid_index = 9;

		int i = 0;
		int k = _last_valid_index;
		while (i < k)
		{
			if (listeners[i] == null)
			{
				while (k > i && listeners[k] == null)
				{
					k--;
				}
				_last_valid_index = k-1;
				if (k > i)
				{
					types[i] = types[k];
					listeners[i] = listeners[k];
					listeners[k] = null;
				}
			}
			i++;
		}

		System.out.println("Last valid index " + _last_valid_index);
		System.out.println(Arrays.toString(listeners));
	}

}
