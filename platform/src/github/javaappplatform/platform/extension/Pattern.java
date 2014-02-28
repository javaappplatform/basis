/*
	This file is part of the javaappplatform library.
	Copyright (C) 2011-2013 Hendrik Renken

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.platform.extension;

import github.javaappplatform.commons.collection.SmallMap;

import java.util.Map;

/**
 * TODO javadoc
 * @author funsheep
 */
public final class Pattern
{

	protected final Map<String, String> search;
	private final String searchString;


	public Pattern(String searchPattern)
	{
		this.searchString = searchPattern;
		this.search = new SmallMap<String, String>(1);

		String[] searches = searchPattern.split(",");
		for (String s : searches)
		{
			int indexOf = s.indexOf('=');
			this.search.put(s.substring(0, indexOf), s.substring(indexOf+1));
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		return this.searchString.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o)
	{
		return o instanceof Pattern && ((Pattern) o).searchString.equals(this.searchString);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return this.searchString;
	}

}
