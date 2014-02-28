/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.util;

import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.nio.charset.Charset;
import java.util.zip.CRC32;


/**
 * This "library" hashes Strings into ints! Is used to provide some performance when dealing with {@link java.lang.String}s as IDs. Use this, e.g. with
 * the {@link TIntObjectHashMap}. The type uses the simple CRC32 hash method because of performance reasons. Collisions are unlikely to happen since int
 * provides a space of nearly four billion unique ids and we will hash some hundred strings.
 * @author funsheep
 */
public class StringID
{

	private static final Charset CHARSET = Charset.forName("UTF-8");

	private static final TIntObjectMap<String> _STRINGS_BY_CRC = TCollections.synchronizedMap(new TIntObjectHashMap<String>());

	/**
	 * Generates an id from the given string. The pair <id, string> is then checked for collision. A collision means, that to the very same id another,
	 * different string (s.equals(string) returns false) is saved.
	 * @param s The string to hash.
	 * @return The hash to the given string.
	 */
	public static final int id(String s)
	{
		assert (!checkForCollision(s));
		if (checkForCollision(s))
			throw new IllegalArgumentException("Collision found between " + _STRINGS_BY_CRC.get(_crc(s)) + " and " + s + ". Database Size: " + _STRINGS_BY_CRC.size());

		final int crc = _crc(s);
		_STRINGS_BY_CRC.putIfAbsent(crc, s);
		return crc;
	}

	/**
	 * Returns the saved string to the given id. If no string was previously hashed to the given id, <code>null</code> is returned.
	 * @param stringID The id to look up.
	 * @return The string associated with the given id, or <code>null</code>.
	 */
	public static final String id(int stringID)
	{
		return _STRINGS_BY_CRC.get(stringID);
	}

	/**
	 * Can be used to check for collision, this should be used for testing of user defined strings.
	 * @param s The string to check.
	 * @return Whether the given string collides with another one (having the same hash) or not.
	 */
	public static final boolean checkForCollision(String s)
	{
		final String db = _STRINGS_BY_CRC.get(_crc(s));
		return db != null && !db.equals(s);
	}

	/**
	 * Available for internal purposes. Returns the hash to the string without associating the string with the id.
	 * @param s The string to hash.
	 * @return  The CRC32 hash.
	 */
	private static final int _crc(String s)
	{
		CRC32 crc = new CRC32();
		crc.update(s.getBytes(CHARSET));
		return (int) crc.getValue();
	}


	private StringID()
	{
		//no instance
	}

}
