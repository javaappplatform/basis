/*
	This file is part of the d3fact common library.
	Copyright (C) 2005-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.utils;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * A buffered InputStream for the http(s) protocol with fast skip support.
 *
 * @author cgrote
 */
public class HTTPInputStream extends FilterInputStream
{
	
	public static final int DEFAULT_SKIPLIMIT = 512 * 1024; // 512KB
	
	private final URL url;
	private final int skipLimit;
	private long pos = 0;
	private long size = -2;
	private boolean rangeSupported = true;

	/**
	 * @param url The http url
	 * @throws IOException
	 */
	public HTTPInputStream(URL url) throws IOException
	{
		this(url, DEFAULT_SKIPLIMIT);
	}

	/**
	 * @param url The http url
	 * @throws IOException
	 */
	public HTTPInputStream(URLConnection con) throws IOException
	{
		this(con, DEFAULT_SKIPLIMIT);
	}

	/**
	 * @param url The http url
	 * @param skipLimit Skip creates a new connection if more than skipLimit bytes are requested to
	 *            skip.
	 * @throws IOException
	 */
	public HTTPInputStream(URL url, int skipLimit) throws IOException
	{
		this(Connect.hardTo(url), skipLimit);
	}

	public HTTPInputStream(URLConnection con, int skipLimit) throws IOException
	{
		super(new BufferedInputStream(con.getInputStream()));
		this.url = con.getURL();
		this.skipLimit = skipLimit;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException
	{
		this.pos++;
		return super.read();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		int i = super.read(b, off, len);
		this.pos += i;
		return i;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		this.rangeSupported = false;
		super.close();

	}

	private long normalSkip(long n) throws IOException
	{
		long s = super.skip(n);
		if (s >= 0)
			this.pos += s;
		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long skip(long n) throws IOException
	{
		if (!this.rangeSupported || n < this.skipLimit)
			return this.normalSkip(n);

		URLConnection con = Connect.hardTo(this.url);

		if (!con.getHeaderField("Accept-Ranges").equals("bytes"))
		{
			this.rangeSupported = false;
			return this.normalSkip(n);
		}

		StringBuilder sb = new StringBuilder("bytes=");
		sb.append(this.pos + n);
		sb.append('-');
		sb.append(getTotalSize());

		con.setRequestProperty("Range", sb.toString());
		con.connect();

		InputStream stream = null;
		try
		{
			stream = con.getInputStream();
		} catch (IOException e)
		{
			this.rangeSupported = false;
			return this.normalSkip(n);
		}

		super.in.close();
		super.in = new BufferedInputStream(stream);

		return n;
	}

	private long getTotalSize() throws IOException
	{
		if (this.size >= -1)
			return this.size;

		final URLConnection con = Connect.lightlyTo(this.url);
		this.size = (con != null) ? con.getContentLengthLong() : -1;
		return this.size;
	}

}
