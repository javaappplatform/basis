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
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileLock;
import java.util.logging.Level;

import javax.imageio.ImageReader;
import javax.naming.Context;

public class Close
{


	private final static Logger LOGGER = Logger.getLogger();


	public static void close(Socket s)
	{
		try
		{
			if (s != null)
				s.close();
		} catch (final Exception e)
		{
			assert LOGGER.log(Level.INFO, "unimportant", e);
		}
	}

	public static void close(DatagramSocket s)
	{
		try
		{
			if (s != null)
				s.close();
		} catch (final Exception e)
		{
			assert LOGGER.log(Level.INFO, "unimportant", e);
		}
	}

	public static void close(ImageReader s)
	{
		try
		{
			if (s != null)
				s.dispose();
		} catch (final Exception e)
		{
			assert LOGGER.log(Level.INFO, "unimportant", e);
		}
	}

	public static void close(ServerSocket s)
	{
		try
		{
			if (s != null)
				s.close();
		} catch (final Exception e)
		{
			assert LOGGER.log(Level.INFO, "unimportant", e);
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
				} catch (final Exception e)
				{
					assert LOGGER.log(Level.INFO, "unimportant", e);
				}
				s.close();
			}
			assert LOGGER.fine("Close(stream)--> Close");

		} catch (final Exception e)
		{
			assert LOGGER.log(Level.INFO, "unimportant", e);
		}
	}

	public static void close(Closeable s)
	{
		try
		{
			if (s != null)
				s.close();
			assert LOGGER.fine("Close(closeable)--> Close");

		} catch (final Exception e)
		{
			assert LOGGER.log(Level.INFO, "unimportant", e);
		}
	}

	public static void close(FileLock s)
	{
		try
		{
			if (s != null)
				s.close();
			assert LOGGER.fine("Close(filelock)--> Close");

		} catch (final Exception e)
		{
			assert LOGGER.log(Level.INFO, "unimportant", e);
		}
	}

	public static void close(Context s)
	{
		try
		{
			if (s != null)
				s.close();
		} catch (final Exception e)
		{
			assert LOGGER.log(Level.INFO, "unimportant", e);
		}
	}

}
