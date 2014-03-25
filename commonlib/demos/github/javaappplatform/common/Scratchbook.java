/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author funsheep
 *
 */
public class Scratchbook
{
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd Hmmss");

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		System.out.println(DATE_FORMAT.format(LocalDateTime.now()));
	}

}
