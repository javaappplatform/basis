/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.server;

import github.javaappplatform.platform.utils.concurrent.Concurrent;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * TODO javadoc
 * @author funsheep
 */
public class ServerTools
{

	public static final <O extends Object> O retrieveNormalizedResult(Concurrent c) throws IOException
	{
		return retrieveNormalizedResult(c, Concurrent.DEFAULT_TIMEOUT);
	}

	public static final <O extends Object> O retrieveNormalizedResult(Concurrent c, long timeout) throws IOException
	{
		try
		{
			return c.retrieveResult(timeout);
		} catch (TimeoutException e)
		{
			throw new IOException("Connection timed out.", e);
		} catch (ExecutionException e)
		{
			if (e.getCause() instanceof IOException)
				throw (IOException) e.getCause();
			throw new IOException("Didn't retrieve correct result.", e.getCause());
		}

	}

	private ServerTools()
	{
		//no instance
	}

}
