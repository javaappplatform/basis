/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.internal;

import github.javaappplatform.commons.util.Arrays2;
import github.javaappplatform.commons.util.Close;
import github.javaappplatform.platform.Platform;
import github.javaappplatform.platform.resource.IDirectory;
import github.javaappplatform.platform.resource.IResource;
import gnu.trove.iterator.TObjectLongIterator;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;

import java.net.URI;
import java.util.HashMap;

/**
 * TODO javadoc
 * @author funsheep
 */
public abstract class AResourceSystem implements IResourceSystem
{

	protected static final int CLEANUP_COUNTER = 50;


	protected final String[] schemes;
	protected final HashMap<URI, IInternalResource> resourcesByURI = new HashMap<>();
	protected final TObjectLongMap<URI> accessTimestamps = new TObjectLongHashMap<>();
	protected int opencalls = 0;


	/**
	 *
	 */
	public AResourceSystem(String... schemes)
	{
		this.schemes = schemes;
	}


	protected void checkScheme(URI uri)
	{
		if (!Arrays2.contains(this.schemes, uri.getScheme()))
			throw new IllegalArgumentException("ResourceSystem does not support given URI scheme "+ uri.getScheme() +".");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IResource resolveAsResource(URI uri)
	{
		this.checkScheme(uri);
		return new DummyResource(uri, this);
	}


	protected abstract IInternalResource createResource(URI uri);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized IInternalResource open(IResource resource)
	{
		final URI clean = ResourceTools.cleanURIFromOptions(resource.uri());

		IInternalResource ires = this.resourcesByURI.get(clean);
		if (ires == null)
		{
			ires = this.createResource(clean);
			this.resourcesByURI.put(clean, ires);
		}
		this.accessTimestamps.put(clean, Platform.currentTime());
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
	public IDirectory resolveAsDirectory(URI uri)
	{
		throw new UnsupportedOperationException("Directories are not supported for this resource system. Given URI was: " + uri);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IInternalDirectory open(IDirectory dir)
	{
		throw new UnsupportedOperationException("Directories are not supported for this resource system. Given URI was: " + dir.uri());
	}

	protected synchronized void cleanup()
	{
		long current = Platform.currentTime();
		TObjectLongIterator<URI> iter = this.accessTimestamps.iterator();
		while (iter.hasNext())
		{
			iter.advance();
			if (current - iter.value() > 120 * 1000 && (!this.resourcesByURI.containsKey(iter.key()) || this.resourcesByURI.get(iter.key()) instanceof IManagedResource && !((IManagedResource) this.resourcesByURI.get(iter.key())).hasOpenStreams()))	//2 minutes
			{
				iter.remove();
				Close.close(this.resourcesByURI.remove(iter.key()));
			}
		}
	}

}
