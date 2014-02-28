/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.console;

import github.javaappplatform.platform.job.ADoJob;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * TODO javadoc
 * @author funsheep
 */
public class Console extends ADoJob
{

	private BufferedReader console_reader = new BufferedReader(new InputStreamReader(System.in));


	public Console()
	{
		super("Console");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doJob()
	{
		while (!Console.this.shutdown && !Thread.currentThread().isInterrupted())
		{
			try
			{
				System.out.println();
				System.out.print("> ");
				String strInput = this.console_reader.readLine();
				if(strInput == null)
					continue;
				if (strInput.trim().length() > 0)
				{
					RunCommand.from(strInput).on(System.out);
				}
			}
			catch (IOException e)
			{
				this.console_reader = new BufferedReader(new InputStreamReader(System.in));	//try again!
			}
		}
	}

}
