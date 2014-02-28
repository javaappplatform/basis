/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.server;

import github.javaappplatform.commons.json.JSONReader;
import github.javaappplatform.network.IClientUnit;
import github.javaappplatform.platform.resource.Directory;
import github.javaappplatform.platform.resource.IDirectory;
import github.javaappplatform.platform.resource.internal.IInternalFile;
import github.javaappplatform.platform.resource.internal.ResourceTools;
import github.javaappplatform.platform.utils.URIs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * TODO javadoc
 * @author funsheep
 */
public class InternalServerFile extends InternalServerResource implements IInternalFile
{


	private URI parent;


	/**
	 * @param uri
	 * @param props
	 * @param unit
	 */
	InternalServerFile(URI uri, String props, IClientUnit unit) throws IOException
	{
		super(uri, props, unit);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void parseProps(JSONReader props) throws IOException
	{
		super.parseProps(props);

		this.parent = null;
		String parentURI = props.nextTextField("parent");
		if (parentURI != null)
		{
			try
			{
				this.parent = new URI(parentURI);
			}
			catch (URISyntaxException ex)
			{
				LOGGER.warn("Could not resolve parent URI "+parentURI, ex);
			}
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
	public String fileExtension()
	{
		return ResourceTools.getRawFileExt(this.name());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDirectory getParent()
	{
		if (this.parent == null)
			return null;
		return Directory.at(this.parent);
	}

}
