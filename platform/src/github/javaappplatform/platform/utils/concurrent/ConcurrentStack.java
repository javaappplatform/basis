/*
	This file is part of the d3fact common library.
	Copyright (C) 2005-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.utils.concurrent;

import java.util.ArrayDeque;
import java.util.concurrent.TimeoutException;

/**
 * TODO javadoc
 * @author funsheep
 */
public class ConcurrentStack
{

	private final ArrayDeque<Concurrent> stack = new ArrayDeque<>(2);


	public synchronized void pushResult(Object _result)
	{
		Concurrent c = this.stack.removeFirst();
		c.pushResult(_result);
	}

	public synchronized Concurrent openConcurrent()
	{
		Concurrent c = new Concurrent();
		this.stack.add(c);
		return c;
	}

	public synchronized void releaseConcurrents()
	{
		for (Concurrent c : this.stack)
			c.pushResult(new TimeoutException("Release concurrents."));
		this.stack.clear();
	}

}
