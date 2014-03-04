/*
	This file is part of the javaappplatform platform library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)
	
	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the 
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.job;

import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.commons.events.TalkerStub;

/**
 * TODO javadoc
 * @author renken
 */
public class JobbedTalkerStub extends TalkerStub
{
	
	private final String thread;


	protected JobbedTalkerStub(String thread)
	{
		this.thread = thread;
	}

	protected JobbedTalkerStub(Object source, String thread)
	{
		super(source);
		this.thread = thread;
	}


	private void _addListener(int type, IListener listener, int priority)
	{
		super.addListener(type, listener, priority);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addListener(final int type, final IListener listener, final int priority)
	{
		JobPlatform.runJob(new Runnable()
		{
			
			@Override
			public void run()
			{
				JobbedTalkerStub.this._addListener(type, listener, priority);
			}
		}, this.thread);
	}

	
	private boolean _hasListener(int type)
	{
		return super.hasListener(type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasListener(final int type)
	{
		AComputeDoJob job = new AComputeDoJob("Call haslistener", this.thread)
		{
			
			@Override
			public void doJob()
			{
				this.finished(Boolean.valueOf(JobbedTalkerStub.this._hasListener(type)));
			}
		};
		try
		{
			return job.<Boolean>get().booleanValue();
		}
		catch (Exception e)
		{
			return false;
		}
	}

	private void _removeListener(int type, IListener listener)
	{
		super.removeListener(type, listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeListener(final int type, final IListener listener)
	{
		JobPlatform.runJob(new Runnable()
		{
			
			@Override
			public void run()
			{
				JobbedTalkerStub.this._removeListener(type, listener);
			}
		}, this.thread);

	}

	private void _removeListener(IListener listener)
	{
		super.removeListener(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeListener(final IListener listener)
	{
		JobPlatform.runJob(new Runnable()
		{
			
			@Override
			public void run()
			{
				JobbedTalkerStub.this._removeListener(listener);
			}
		}, this.thread);
	}

	private void _postEvent(int type, Object... data)
	{
		super.postEvent(type, data);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void postEvent(final int type, final Object... data)
	{
		JobPlatform.runJob(new Runnable()
		{
			
			@Override
			public void run()
			{
				JobbedTalkerStub.this._postEvent(type, data);
			}
		}, this.thread);
	}

	private void _clear()
	{
		super.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear()
	{
		JobPlatform.runJob(new Runnable()
		{
			
			@Override
			public void run()
			{
				JobbedTalkerStub.this._clear();
			}
		}, this.thread);
	}

}
