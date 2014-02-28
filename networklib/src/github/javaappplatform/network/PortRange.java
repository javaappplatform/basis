/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TODO javadoc
 * @author funsheep
 */
public class PortRange
{

	private final int min;
	private final int max;


	/**
	 *
	 */
	public PortRange(int min, int max)
	{
		if (min <= 0 || max <= 0)
			throw new IllegalArgumentException("Illegal min,max values below or equal to zero!");
		if (min < max)
		{
			this.min = min; this.max = max;
		}
		else
		{
			this.min = max; this.max = min;
		}
	}


	public static final void bind(Socket socket, PortRange range) throws IOException
	{
		if (range == null)
		{
			if(!socket.isBound())
				socket.bind(null);
			return;
		}
		for (int i = range.min; i < range.max; i++)
			try
			{
				socket.bind(new InetSocketAddress("localhost",i));
				return;
			}
			catch (IOException e)
			{
				// try the next port
			}
		throw new IOException("Could not bind socket within this port range " + range);
	}

	public static final void bind(ServerSocket socket, PortRange range) throws IOException
	{
		if (range == null)
		{
			if(!socket.isBound())
				socket.bind(null);
			return;
		}
		for (int i = range.min; i < range.max; i++)
			try
			{
				socket.bind(new InetSocketAddress("localhost",i));
				return;
			}
			catch (IOException e)
			{
				// try the next port
			}
		throw new IOException("Could not bind socket within this port range " + range);
	}

	public static final void bind(DatagramSocket socket, PortRange range) throws IOException
	{
		if (range == null)
		{
			if(!socket.isBound())
				socket.bind(null);
			return;
		}
		for (int i = range.min; i < range.max; i++)
			try
			{
				socket.bind(new InetSocketAddress("localhost",i));
				return;
			}
			catch (IOException e)
			{
				// try the next port
			}
		throw new IOException("Could not bind socket within this port range " + range);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return "[" + this.min + "," + this.max + "]";
	}

}
