/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.internal;

import github.javaappplatform.platform.resource.IDirectory;
import github.javaappplatform.platform.utils.URIs;

import java.io.IOException;
import java.net.URI;

/**
 * TODO javadoc
 * @author funsheep
 */
public class DummyDirectory implements IDirectory
{

	private final URI uri;
	private final int resType;
	private final IResourceSystem sys;


	public DummyDirectory(URI uri, int resType, IResourceSystem sys)
	{
		this.uri =  URIs.ensureIsDirectory(uri);
		this.resType = resType;
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
		return this.resType;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI[] getChildren() throws IOException
	{
		return this.sys.open(this).getChildren();
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


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void discard()
	{
//		if (this.sys instanceof ITalker)
//			((ITalker) this.sys).removeListener(this.rsEventListener);
	}

//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	protected void finalize()
//	{
//		this.discard();
//	}

//	private boolean isRegistered = false;
//	private final IListener rsEventListener = new IListener()
//	{
//
//		@Override
//		public void handleEvent(Event e)
//		{
//			if (e.getData() != null && e.getData() instanceof URI)
//				if (DummyDirectory.this.uri().equals(e.getData()) || DummyDirectory.this.uri().equals(URIs.resolveParent((URI) e.getData())))
//					DummyDirectory.this.postEvent(e.type(), this);
//		}
//	};

}
