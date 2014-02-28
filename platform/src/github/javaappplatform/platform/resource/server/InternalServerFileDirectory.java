/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.server;

import github.javaappplatform.commons.json.JSONReader;
import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.network.IClientUnit;
import github.javaappplatform.network.ISession;
import github.javaappplatform.platform.network.interfaces.DispatchMessages;
import github.javaappplatform.platform.network.interfaces.RegisterInterface;
import github.javaappplatform.platform.resource.internal.IInternalDirectory;
import github.javaappplatform.platform.utils.URIs;

import java.io.IOException;
import java.net.URI;

import com.fasterxml.jackson.core.JsonToken;

/**
 * TODO javadoc
 * @author funsheep
 */
class InternalServerFileDirectory implements IInternalDirectory
{

	private static final Logger LOGGER = Logger.getLogger();


	private final URI uri;
	private final ISession session;


	private int type;
	private boolean exists;
	private boolean isWritable;
	private URI[] children;
	private final DirectoryInterface netdir = new DirectoryInterface();
	private final SystemInterface netinfo = new SystemInterface();


	InternalServerFileDirectory(URI uri, String props, IClientUnit unit) throws IOException
	{
		this.uri = uri;
		this.session = unit.startSession();
		RegisterInterface.instance(this.netdir).with(DirectoryInterface.INTERFACE_ID, DirectoryInterface.MESSAGE_TYPES).at(this.session);
		RegisterInterface.instance(this.netinfo).with(SystemInterface.INTERFACE_ID, SystemInterface.MESSAGE_TYPES).at(this.session);
		DispatchMessages.automaticallyFor(this.session).inNetworkThread();
		this.update(props);
	}

	protected final void update(String props) throws IOException
	{
		@SuppressWarnings("resource")
		final JSONReader read = new JSONReader(props);
		read.nextToken(JsonToken.START_OBJECT);
		this.type = read.nextIntField("resourcetype");
		this.exists = read.nextBooleanField("exists");
		this.isWritable = read.nextBooleanField("isWritable");

		read.nextFieldName("children");
		String[] childNames = read.nextStringArray();
		this.children = new URI[childNames.length];
		for (int i = 0; i < childNames.length; i++)
		{
			this.children[i] = URIs.resolveChild(this.uri, childNames[i]);
			if (this.children[i] == null)
				throw new IOException("Could not resolve child " + childNames[i]);
		}
		read.nextToken(JsonToken.END_OBJECT);
	}

	private synchronized final void updateData() throws IOException
	{
		String props = this.netinfo.resolveURIAsDirectory(this.uri)[1].toString();
		this.update(props);
	}


	private synchronized final void setExists(boolean exists)
	{
		this.exists = exists;
		this.isWritable = true;
		this.children = new URI[0];
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
	public String name()
	{
		return URIs.extractName(this.uri);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int resourceType()
	{
		return this.type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists()
	{
		try
		{
			this.updateData();
		}
		catch (IOException e)
		{
			LOGGER.warn("Could not retrieve information.", e);
			return false;
		}
		return this.exists;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWritable()
	{
		try
		{
			this.updateData();
		}
		catch (IOException e)
		{
			LOGGER.warn("Could not retrieve information.", e);
			return false;
		}
		return this.isWritable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI[] getChildren() throws IOException
	{
		try
		{
			this.updateData();
		}
		catch (IOException e)
		{
			LOGGER.warn("Could not retrieve information.", e);
			return null;
		}
		if (!this.exists())
			return null;
		return this.children;
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
	public void close()
	{
		Close.close(this.session);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean create() throws IOException
	{
		final boolean created = this.netdir.createDirectory(this.uri, false);
		if (created)
			this.setExists(true);
		return created;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean delete() throws IOException
	{
		final boolean deleted = this.netdir.delete(this.uri);
		if (deleted)
			this.setExists(false);
		return deleted;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean ensureExistence() throws IOException
	{
		final boolean created = this.netdir.createDirectory(this.uri, true);
		if (created)
			this.setExists(true);
		return created;
	}

}
