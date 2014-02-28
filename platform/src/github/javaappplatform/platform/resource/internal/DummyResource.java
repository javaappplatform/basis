/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.internal;

import github.javaappplatform.platform.resource.IResource;
import github.javaappplatform.platform.resource.IResourceAPI;

import java.io.IOException;
import java.net.URI;

/**
 * TODO javadoc
 * @author funsheep
 */
public class DummyResource implements IResource
{

	protected final URI uri;
	protected final IResourceSystem sys;


	/**
	 *
	 */
	public DummyResource(URI uri, IResourceSystem sys)
	{
		this.uri = uri;
		this.sys = sys;
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
	public int type()
	{
		return IResourceAPI.TYPE_UNKNOWN_RESOURCE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists()
	{
		try
		{
			return this.sys.open(this).exists();
		}
		catch (IOException e)
		{
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String mimetype()
	{
		try
		{
			return this.sys.open(this).mimetype();
		}
		catch (IOException e)
		{
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long size()
	{
		try
		{
			return this.sys.open(this).size();
		}
		catch (IOException e)
		{
			return IResource.UNKNOWN_SIZE;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long lastTimeModified()
	{
		try
		{
			return this.sys.open(this).lastTimeModified();
		}
		catch (IOException e)
		{
			return IResource.UNKNOWN_MODIFIED_TIME;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadable()
	{
		try
		{
			return this.sys.open(this).isReadable();
		}
		catch (IOException e)
		{
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWritable()
	{
		try
		{
			return this.sys.open(this).isWritable();
		}
		catch (IOException e)
		{
			return false;
		}
	}

//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public void addListener(int type, IListener listener, int priority)
//	{
//		super.addListener(type, listener, priority);
//		if (!this.isRegistered)
//		{
//			this.sys.addListener(this, this.rsEventListener);
//			this.isRegistered = true;
//		}
//	}
//
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void discard()
	{
//		if (this.sys instanceof ITalker)
//			((ITalker) this.sys).removeListener(this.rsEventListener);
	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	protected void finalize()
//	{
//		this.discard();
//	}
//
//	private boolean isRegistered = false;
//	private final IListener rsEventListener = new IListener()
//	{
//
//		@Override
//		public void handleEvent(Event e)
//		{
//			if (DummyResource.this.uri().equals(e.getData()))
//				DummyResource.this.postEvent(e.type(), this);
//		}
//	};
//
}
