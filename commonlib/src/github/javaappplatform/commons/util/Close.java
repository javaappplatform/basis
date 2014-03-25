/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
 */
package github.javaappplatform.commons.util;

import github.javaappplatform.commons.log.Logger;

import java.io.Closeable;
import java.io.OutputStream;
import java.nio.channels.FileLock;

import javax.imageio.ImageReader;

public class Close
{

	private final static Logger LOGGER = Logger.getLogger();

	public static void close(ImageReader s)
	{
		try
		{
			if (s != null)
				s.dispose();
			assert LOGGER.trace("Close(ImageReader)--> Close");
		}
		catch (final Exception e)
		{
			assert LOGGER.trace("unimportant", e);
		}
	}

	public static void close(OutputStream s)
	{
		try
		{
			if (s != null)
			{
				try
				{
					s.flush();
				}
				catch (final Exception e)
				{
					assert LOGGER.trace("unimportant", e);
				}
				s.close();
			}
			assert LOGGER.trace("Close(OutputStream)--> Close");
		}
		catch (final Exception e)
		{
			assert LOGGER.trace("unimportant", e);
		}
	}

	public static void close(FileLock s)
	{
		try
		{
			if (s != null)
			{
				s.close();
				assert LOGGER.trace("Close(FileLock)--> Close");
			}
		}
		catch (final Exception e)
		{
			assert LOGGER.trace("unimportant", e);
		}
	}

	public static void close(Closeable s)
	{
		try
		{
			if (s != null)
			{
				s.close();
				assert LOGGER.trace("Close(Closeable)--> Close");
			}
		}
		catch (final Exception e)
		{
			assert LOGGER.trace("unimportant", e);
		}
	}

}
