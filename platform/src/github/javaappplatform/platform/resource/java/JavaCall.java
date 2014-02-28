/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.resource.java;

import github.javaappplatform.platform.resource.IResource;
import github.javaappplatform.platform.resource.IResourceAPI;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URI;

/**
 * TODO javadoc
 * @author funsheep
 */
public class JavaCall implements IResource
{

	private final URI uri;
	private final String mimetype;
	private final Member member;


	public JavaCall(URI uri)
	{
		this(uri, JavaRSTools.detectJavaCall(uri));
	}

	/**
	 *
	 */
	JavaCall(URI uri, Object[] call)
	{
		this.uri = uri;
		this.member   = (Member) (call != null ? call[0] : null);
		this.mimetype = (String) (call != null ? call[1] : null);
	}


	/**
	 * Supports calls to static methods or constructors.
	 * @param parameters Parameters to give in call to member.
	 * @return The result of the method call (if any) or the created object (when member is a constructor). <code>null</code> if member is a method with returntype 'void'.
	 */
	public Object invoke(Object... args) throws IOException
	{
		try
		{
			if (this.member instanceof Method)
				return ((Method) this.member).invoke(null, args);
			return ((Constructor<?>) this.member).newInstance(args);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			throw new IOException("Could not invoke associated member.", e);
		}
	}

	public Member getMember()
	{
		return this.member;
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
		return IResourceAPI.TYPE_JAVA_CALL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists()
	{
		return this.member != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String mimetype()
	{
		return this.mimetype;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long size()
	{
		return IResource.UNKNOWN_SIZE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long lastTimeModified()
	{
		return IResource.UNKNOWN_MODIFIED_TIME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadable()
	{
		return this.exists();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWritable()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void discard()
	{
		//do nothing
	}

}
