/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.internal;

import github.javaappplatform.commons.util.Close;
import github.javaappplatform.platform.Platform;
import github.javaappplatform.platform.resource.IDirectory;
import github.javaappplatform.platform.resource.IFile;
import github.javaappplatform.platform.resource.IResourceAPI;
import gnu.trove.iterator.TObjectLongIterator;

import java.net.URI;
import java.util.HashMap;

/**
 * TODO javadoc
 * @author funsheep
 */
public abstract class AFileSystem extends AResourceSystem
{


	private final HashMap<URI, IInternalDirectory> dirsByURI = new HashMap<>();



	public AFileSystem(String... schemes)
	{
		super(schemes);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected abstract IInternalFile createResource(URI uri);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IFile resolveAsResource(URI uri)
	{
		checkScheme(uri);
		return new DummyFile(uri, this);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDirectory resolveAsDirectory(URI uri)
	{
		checkScheme(uri);
		return new DummyDirectory(uri, IResourceAPI.TYPE_FILE, this);
	}

	protected abstract IInternalDirectory createDirectory(URI uri);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized IInternalDirectory open(IDirectory dir)
	{
		final URI clean = ResourceTools.cleanURIFromOptions(dir.uri());

		IInternalDirectory idir = this.dirsByURI.get(clean);
		if (idir == null)
		{
			idir = this.createDirectory(clean);
			this.dirsByURI.put(clean, idir);
		}
		this.accessTimestamps.put(clean, Platform.currentTime());
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
				Close.close(this.dirsByURI.remove(iter.key()));
			}
		}
	}

}
