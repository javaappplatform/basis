/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.server;

import github.javaappplatform.commons.log.Logger;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.network.IClientUnit;
import github.javaappplatform.network.ISession;
import github.javaappplatform.platform.Platform;
import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.extension.ServiceInstantiationException;
import github.javaappplatform.platform.network.IPlatformNetworkAPI;
import github.javaappplatform.platform.network.interfaces.DispatchMessages;
import github.javaappplatform.platform.network.interfaces.RegisterInterface;
import github.javaappplatform.platform.resource.IDirectory;
import github.javaappplatform.platform.resource.IResource;
import github.javaappplatform.platform.resource.IResourceAPI;
import github.javaappplatform.platform.resource.IResourceAPI.OpenOption;
import github.javaappplatform.platform.resource.internal.DummyDirectory;
import github.javaappplatform.platform.resource.internal.DummyFile;
import github.javaappplatform.platform.resource.internal.DummyResource;
import github.javaappplatform.platform.resource.internal.IInternalDirectory;
import github.javaappplatform.platform.resource.internal.IInternalFile;
import github.javaappplatform.platform.resource.internal.IResourceSystem;
import github.javaappplatform.platform.resource.internal.ResourceTools;
import gnu.trove.iterator.TObjectLongIterator;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

/**
 * TODO javadoc
 * @author funsheep
 */
public final class ServerFileSystem implements IResourceSystem
{


	protected static final int CLEANUP_COUNTER = 50;


	private static final Logger LOGGER = Logger.getLogger();


	private final TObjectLongMap<URI> accessTimestamps = new TObjectLongHashMap<>();
	private final HashMap<URI, InternalServerResource> resourcesByURI = new HashMap<>();
	private final HashMap<URI, IInternalDirectory> dirsByURI = new HashMap<>();
	private final SystemInterface network = new SystemInterface();
	private final IClientUnit unit;
	private int opencalls = 0;


	public ServerFileSystem() throws IOException
	{
		try
		{
			this.unit = ExtensionRegistry.getExtension(IPlatformNetworkAPI.EXT_POINT).<IClientUnit>getService();
			ISession session = this.unit.startSession();
			RegisterInterface.instance(this.network).with(SystemInterface.INTERFACE_ID, SystemInterface.MESSAGE_TYPES).at(session);
			DispatchMessages.automaticallyFor(session).inNetworkThread();
		} catch (ServiceInstantiationException e)
		{
			throw new IOException("Could not load network.", e);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized IResource resolveAsResource(URI uri)
	{
		switch (uri.getScheme())
		{
			case "file":
			case "ws:":
			case "dav":
			case "davs":
				return new DummyFile(uri, this);
			case "http":
			case "https":
				return new DummyResource(uri, this);
		}

		try
		{
			InternalServerResource ires = this.openResource(ResourceTools.cleanURIFromOptions(uri));
			if (ires instanceof IInternalFile)
				return new DummyFile(uri, this);
			return new DummyResource(uri, this);
		}
		catch (IOException e)
		{
			LOGGER.severe("Network is down. Could not connect to server.", e);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InternalServerResource open(IResource resource) throws IOException
	{
		return this.openResource(ResourceTools.cleanURIFromOptions(resource.uri()));
	}

	private synchronized InternalServerResource openResource(URI uri) throws IOException
	{
		InternalServerResource ires = this.resourcesByURI.get(uri);
		if (ires == null)
		{
			Object[] returnValue = this.network.resolveURIAsResource(uri);
			if (((Byte)returnValue[0]).byteValue() == IResourceAPI.TYPE_FILE)
				ires = new InternalServerFile(uri, returnValue[1].toString(), this.unit);
			else
				ires = new InternalServerResource(uri, returnValue[1].toString(), this.unit);
			this.resourcesByURI.put(uri, ires);
		}
		this.accessTimestamps.put(uri, Platform.currentTime());
		if (this.opencalls++ > CLEANUP_COUNTER)
		{
			this.opencalls = 0;
			this.cleanup();
		}
		return ires;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized IDirectory resolveAsDirectory(URI uri)
	{
		switch (uri.getScheme())
		{
			case "file":
			case "ws:":
			case "dav":
			case "davs":
				return new DummyDirectory(uri, IResourceAPI.TYPE_FILE, this);
			case "http":
			case "https":
				throw new UnsupportedOperationException("Directories are not supported for uri scheme " + uri.getScheme());
		}

		try
		{
			IInternalDirectory idir = this.openDirectory(ResourceTools.cleanURIFromOptions(uri));
			return new DummyDirectory(uri, idir.resourceType(), this);
		}
		catch (IOException e)
		{
			LOGGER.severe("Network is down. Could not connect to server.", e);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IInternalDirectory open(IDirectory dir) throws IOException
	{
		return this.openDirectory(ResourceTools.cleanURIFromOptions(dir.uri()));
	}

	private synchronized IInternalDirectory openDirectory(URI uri) throws IOException
	{
		IInternalDirectory idir = this.dirsByURI.get(uri);
		if (idir == null)
		{
			Object[] returnValue = this.network.resolveURIAsDirectory(uri);
			idir = new InternalServerFileDirectory(uri, returnValue[1].toString(), this.unit);
			this.dirsByURI.put(uri, idir);
		}
		this.accessTimestamps.put(uri, Platform.currentTime());
		if (this.opencalls++ > CLEANUP_COUNTER)
		{
			this.opencalls = 0;
			this.cleanup();
		}
		return idir;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void copy(IResource _from, IResource _to, int options) throws IOException
	{
		InternalServerResource from = this.open(_from);
		InternalServerResource to   = this.open(_to);
		if (!from.exists())
			throw new IOException("Could not find resource " + from.uri());
		if (!to.isWritable())
			throw new IOException("Resource is not writable. " + to.uri());
		if (to.exists() && !OpenOption.CREATE_IN_ANY_CASE.isSetIn(options))
			throw new IOException("Could not override resource " + to.uri());

		this.network.copy(from.uri(), to.uri(), options);
	}

	private synchronized void cleanup()
	{
		long current = Platform.currentTime();
		TObjectLongIterator<URI> iter = this.accessTimestamps.iterator();
		while (iter.hasNext())
		{
			iter.advance();
			if (current - iter.value() > 120 * 1000 && (!this.resourcesByURI.containsKey(iter.key()) || !this.resourcesByURI.get(iter.key()).hasOpenStreams()))	//2 minutes
			{
				iter.remove();
				Close.close(this.resourcesByURI.remove(iter.key()));
				Close.close(this.dirsByURI.remove(iter.key()));
			}
		}
	}

}
