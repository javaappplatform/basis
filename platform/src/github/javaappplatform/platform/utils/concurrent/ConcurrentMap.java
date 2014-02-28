/*
	This file is part of the d3fact common library.
	Copyright (C) 2005-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.utils.concurrent;

import github.javaappplatform.commons.collection.SmallMap;

import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * TODO javadoc
 * @author funsheep
 */
public class ConcurrentMap
{

	private final Map<Object, Concurrent> map = new SmallMap<>();


	public synchronized void pushResult(Object key, Object result)
	{
		Concurrent c = this.map.remove(key);
		if (c == null)
			throw new IllegalArgumentException("Key "+key+" not found.");
		c.pushResult(result);
	}

	public synchronized void tryPushResult(Object key, Object result)
	{
		Concurrent c = this.map.remove(key);
		if (c != null)
			c.pushResult(result);
	}


	public synchronized Concurrent openConcurrent(Object key)
	{
		Concurrent c = new Concurrent();
		this.map.put(key, c);
		return c;
	}

	public synchronized void releaseConcurrents()
	{
		for (Concurrent c : this.map.values())
			c.pushResult(new TimeoutException("Release concurrents."));
		this.map.clear();
	}

}
