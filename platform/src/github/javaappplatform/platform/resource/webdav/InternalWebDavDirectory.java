/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.webdav;

import github.javaappplatform.commons.collection.SmallSet;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.resource.IResourceAPI;
import github.javaappplatform.platform.resource.internal.IInternalDirectory;
import github.javaappplatform.platform.utils.URIs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO javadoc
 * @author MeisterYeti
 */
public class InternalWebDavDirectory implements IInternalDirectory
{
	private static final Logger LOGGER = Logger.getLogger();
	private static final String HREF_PATTERN = "<a .*?href=\"(.*?)\"";

	private final URI uri;
	private URLConnection connection;


	/**
	 *
	 */
	public InternalWebDavDirectory(URI uri)
	{
		this.uri = uri;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI uri()
	{
		return this.uri;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int resourceType()
	{
		return IResourceAPI.TYPE_FILE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists()
	{
		if(this.connection != null)
			return true;
		try
		{
			this.connection = WebDavTools.connect(this.uri);
			return this.connection != null;
		} catch (IOException e)
		{
			LOGGER.warn("Could not connect to '" + this.uri + "'.", e);
			this.connection = null;
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String name()
	{
		return URIs.extractName(this.uri);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWritable()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI[] getChildren() throws IOException
	{
		if (!this.exists())
			return null;

		if(!this.connection.getContentType().startsWith("text/html"))
			return null;

		BufferedReader in = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
		String line = null;
		Pattern pattern = Pattern.compile(HREF_PATTERN);

		SmallSet<URI> uris = new SmallSet<URI>();

		while((line = in.readLine()) != null)
		{
			Matcher matcher = pattern.matcher(line);
			while(matcher.find())
			{
				String match = matcher.group(1);
				// TODO: Find better method to filter out non-relative urls
				if(!match.startsWith("/") && !match.contains("://") && !match.contains("?"))
				{
					uris.add(this.uri.resolve(match));
				}
			}
		}
		return uris.toArray(new URI[uris.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void discard()
	{
		//do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean create() throws IOException
	{
		throw new UnsupportedOperationException("Creating directories is not permitted.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean ensureExistence() throws IOException
	{
		if (this.exists())
			return false;
		return this.create();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean delete() throws IOException
	{
		throw new UnsupportedOperationException("Deleting directories is not permitted.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close()
	{
		this.connection = null;
	}

}
