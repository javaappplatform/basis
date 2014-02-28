/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package de.d3fact.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropLoadSaveDemo
{

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) throws IOException
	{
		Properties p = new Properties();
		p.setProperty("key0", "value0");
		p.setProperty("key1", "String with \n next line delimit");
		p.setProperty("key2", "String with <tag \\>");

		File f = new File("prop.demo.txt");
		f.createNewFile();
		FileOutputStream st = new FileOutputStream(f);

		p.store(st, "comment comment");
		st.close();
	}

}
