/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.platform.resource.IResourceAPI.OpenOption;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * TODO javadoc
 * @author funsheep
 */
public class Append
{

	private static final Logger LOGGER = Logger.getLogger();


	private final StringBuilder sb = new StringBuilder(20);


	/**
	 *
	 */
	private Append(OpenOption o)
	{
		this.sb.append("options=");
		this.sb.append(o.name());
	}

	public static final Append option(OpenOption o)
	{
		return new Append(o);
	}

	public final Append and(OpenOption o)
	{
		this.sb.append('+');
		this.sb.append(o.name());
		return this;
	}

	public final URI to(String uri) throws URISyntaxException
	{
		return this.to(new URI(uri));
	}

	public final URI to(URI uri)
	{
		String query = uri.getQuery();
		if (query == null || query.length() == 0)
			query = this.sb.toString();
		else
			query += "&" + this.sb.toString();

		try
		{
			return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), query, uri.getFragment());
		} catch (URISyntaxException e)
		{
			LOGGER.warn("Could not correctly append given options to uri " + uri, e);
			return uri;
		}
	}

}
