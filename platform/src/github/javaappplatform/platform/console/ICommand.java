/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.console;

import java.io.PrintStream;

/**
 * TODO javadoc
 * @author funsheep
 */
public interface ICommand
{

	public static final String EXT_POINT = "github.javaappplatform.platform.console.Command";


	public void run(String[] args, PrintStream out) throws Exception;

}
