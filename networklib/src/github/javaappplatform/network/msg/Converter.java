/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.msg;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class Converter
{

	static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");


	/**
	 * Reads the addressed byte and returns a boolean value of this representation.
	 * @param b
	 * @param off
	 * @return
	 */
	public static boolean getBooleanBig(byte[] b, int off)
	{
		return b[off] > 0;
	}

	public static boolean getBooleanBig(byte b)
	{
		return b > 0;
	}

	/**
	 * @param b
	 * @param off
	 * @return
	 */
	public static float getFloatBig(byte[] b, int off)
	{
		return Float.intBitsToFloat(Converter.getIntBig(b, off));
	}

	/**
	 * @param b
	 * @param off
	 * @return
	 */
	public static int getIntBig(byte[] b, int off)
	{
		return ((b[off + 3] & 0xFF) << 0) + ((b[off + 2] & 0xFF) << 8) + ((b[off + 1] & 0xFF) << 16) + ((b[off + 0] & 0xFF) << 24);
	}

	/**
	 * @param b
	 * @param off
	 * @return
	 */
	public static long getLongBig(byte[] b, int off)
	{
		return ((b[off + 7] & 0xFFL) << 0) + ((b[off + 6] & 0xFFL) << 8) + ((b[off + 5] & 0xFFL) << 16) + ((b[off + 4] & 0xFFL) << 24)
			+ ((b[off + 3] & 0xFFL) << 32) + ((b[off + 2] & 0xFFL) << 40) + ((b[off + 1] & 0xFFL) << 48) + ((b[off + 0] & 0xFFL) << 56);
	}

	/**
	 * @param b
	 * @param off
	 * @return
	 */
	public static double getDoubleBig(byte[] b, int off)
	{
		return Double.longBitsToDouble(getLongBig(b,off));
	}

	/**
	 * @param b
	 * @param off
	 * @return
	 */
	public static void putBoolean(byte[] b, int off, boolean bool)
	{
		b[off] = bool ? (byte)1 : (byte)0;
	}

	/**
	 * @param b
	 * @param off
	 * @param val
	 */
	public static void putIntBig(byte[] b, int off, int val)
	{
		b[off + 3] = (byte)(val >>> 0);
		b[off + 2] = (byte)(val >>> 8);
		b[off + 1] = (byte)(val >>> 16);
		b[off + 0] = (byte)(val >>> 24);
	}

	/**
	 * @param b
	 * @param off
	 * @param val
	 */
	public static void putLongBig(byte[] b, int off, long val)
	{
		b[off + 7] = (byte)(val >>> 0);
		b[off + 6] = (byte)(val >>> 8);
		b[off + 5] = (byte)(val >>> 16);
		b[off + 4] = (byte)(val >>> 24);
		b[off + 3] = (byte)(val >>> 32);
		b[off + 2] = (byte)(val >>> 40);
		b[off + 1] = (byte)(val >>> 48);
		b[off + 0] = (byte)(val >>> 56);
	}

	/**
	 * @param b
	 * @param off
	 * @param val
	 */
	public static void putFloatBig(byte[] b, int off, float val)
	{
		final int j = Float.floatToIntBits(val);
		b[off + 3] = (byte)(j >>> 0);
		b[off + 2] = (byte)(j >>> 8);
		b[off + 1] = (byte)(j >>> 16);
		b[off + 0] = (byte)(j >>> 24);
	}

	/**
	 * @param b
	 * @param off
	 * @param val
	 */
	public static void putDoubleBig(byte[] b, int off, double val)
	{
		final long j = Double.doubleToLongBits(val);
		b[off + 7] = (byte)(j >>> 0);
		b[off + 6] = (byte)(j >>> 8);
		b[off + 5] = (byte)(j >>> 16);
		b[off + 4] = (byte)(j >>> 24);
		b[off + 3] = (byte)(j >>> 32);
		b[off + 2] = (byte)(j >>> 40);
		b[off + 1] = (byte)(j >>> 48);
		b[off + 0] = (byte)(j >>> 56);
	}

	/**
	 * @param b
	 * @param off
	 * @return
	 */
	public static int getIntLittle(byte[] b, int off)
	{
		return ((b[off + 0] & 0xFF) << 0) + ((b[off + 1] & 0xFF) << 8) + ((b[off + 2] & 0xFF) << 16) + ((b[off + 3] & 0xFF) << 24);
	}

	/**
	 * @param b
	 * @param off
	 * @return
	 */
	public static long getLongLittle(byte[] b, int off)
	{
		return ((b[off + 0] & 0xFFL) << 0) + ((b[off + 1] & 0xFFL) << 8) + ((b[off + 2] & 0xFFL) << 16) + ((b[off + 3] & 0xFFL) << 24)
			+ ((b[off + 4] & 0xFFL) << 32) + ((b[off + 5] & 0xFFL) << 40) + ((b[off + 6] & 0xFFL) << 48) + ((b[off + 7] & 0xFFL) << 56);
	}

	/**
	 * @param b
	 * @param off
	 * @param val
	 */
	public static void putIntLittle(byte[] b, int off, int val)
	{
		b[off + 0] = (byte)(val >>> 0);
		b[off + 1] = (byte)(val >>> 8);
		b[off + 2] = (byte)(val >>> 16);
		b[off + 3] = (byte)(val >>> 24);
	}

	/**
	 * @param b
	 * @param off
	 * @param val
	 */
	public static void putLongLittle(byte[] b, int off, long val)
	{
		b[off + 0] = (byte)(val >>> 0);
		b[off + 1] = (byte)(val >>> 8);
		b[off + 2] = (byte)(val >>> 16);
		b[off + 3] = (byte)(val >>> 24);
		b[off + 4] = (byte)(val >>> 32);
		b[off + 5] = (byte)(val >>> 40);
		b[off + 6] = (byte)(val >>> 48);
		b[off + 7] = (byte)(val >>> 56);
	}

	/**
	 * @param b
	 * @param off
	 * @param val
	 */
	public static void putFloatLittle(byte[] b, int off, float val)
	{
		final int j = Float.floatToIntBits(val);
		b[off + 0] = (byte)(j >>> 0);
		b[off + 1] = (byte)(j >>> 8);
		b[off + 2] = (byte)(j >>> 16);
		b[off + 3] = (byte)(j >>> 24);
	}

	/**
	 * @param b
	 * @param off
	 * @param val
	 */
	public static void putDoubleLittle(byte[] b, int off, double val)
	{
		final long j = Double.doubleToLongBits(val);
		b[off + 0] = (byte)(j >>> 0);
		b[off + 1] = (byte)(j >>> 8);
		b[off + 2] = (byte)(j >>> 16);
		b[off + 3] = (byte)(j >>> 24);
		b[off + 4] = (byte)(j >>> 32);
		b[off + 5] = (byte)(j >>> 40);
		b[off + 6] = (byte)(j >>> 48);
		b[off + 7] = (byte)(j >>> 56);
	}

	public static void putArray(byte[] b, int off, byte[] value)
	{
		System.arraycopy(value, 0, b, off, value.length);
	}

	/**
	 *
	 * @param b byte-array into which the String will be inserted.
	 * @param off offset in the array to be used for insertion of string.
	 * @param string String to be converted and inserted into given byte-array.
	 * @param order Used ByteOrder for String-length.
	 * @return Returns offset after the newly inserted String.
	 */
	public static int putString(byte[] b, int off, String string, ByteOrder order)
	{
		final byte[] arr = convertString(string);
		putInt(b, off, arr.length, order);
		putArray(b, off+4, arr);

		return off+4+arr.length;
	}

	public static byte[] convertString(String value)
	{
		return value.getBytes(DEFAULT_CHARSET);
	}

	public static String getString(byte[] b, int off)
	{
		int length = Converter.getIntBig(b, off);
		return new String(b, off+4, length, DEFAULT_CHARSET);
	}

	public static int getInt(byte[] b, int off, ByteOrder order)
	{
		return order == ByteOrder.LITTLE_ENDIAN ? getIntLittle(b, off) : getIntBig(b, off);
	}

	public static long getLong(byte[] b, int off, ByteOrder order)
	{
		return order == ByteOrder.LITTLE_ENDIAN ? getLongLittle(b, off) : getLongBig(b, off);
	}

	public static void putInt(byte[] b, int off, int val, ByteOrder order)
	{
		if (order == ByteOrder.LITTLE_ENDIAN)
			putIntLittle(b, off, val);
		else
			putIntBig(b, off, val);
	}

	public static void putLong(byte[] b, int off, long val, ByteOrder order)
	{
		if (order == ByteOrder.LITTLE_ENDIAN)
			putLongLittle(b, off, val);
		else
			putLongBig(b, off, val);
	}

	public static void putFloat(byte[] b, int off, float val, ByteOrder order)
	{
		if (order == ByteOrder.LITTLE_ENDIAN)
			putFloatLittle(b, off, val);
		else
			putFloatBig(b, off, val);
	}

	public static void putDouble(byte[] b, int off, double val, ByteOrder order)
	{
		if (order == ByteOrder.LITTLE_ENDIAN)
			putDoubleLittle(b, off, val);
		else
			putDoubleBig(b, off, val);
	}

}
