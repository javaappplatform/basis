/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
 */
package github.javaappplatform.network.msg;

import github.javaappplatform.commons.collection.SemiDynamicByteArray;

import java.nio.ByteOrder;

public class MessageBuilder
{
	private final int startSize;
	private SemiDynamicByteArray dynamicArray;

	private final byte[] FOUR = new byte[4];
	private final byte[] EIGHT = new byte[8];

	public MessageBuilder()
	{
		this(32);
	}

	public MessageBuilder(int startSize)
	{
		this.startSize = startSize;
		this.dynamicArray = new SemiDynamicByteArray(this.startSize);
	}

	public void reset()
	{
		this.dynamicArray = new SemiDynamicByteArray(this.startSize);
	}

	public SemiDynamicByteArray get()
	{
		return this.dynamicArray;
	}

	public void putInt(int value)
	{
		Converter.putIntBig(this.FOUR, 0, value);
		this.dynamicArray.putAll(this.FOUR);
	}

	public void putLong(long value)
	{
		Converter.putLongBig(this.EIGHT, 0, value);
		this.dynamicArray.putAll(this.EIGHT);
	}

	public void putArray(byte[] value)
	{
		this.dynamicArray.putAll(value);
	}

	public void putBoolean(boolean bool)
	{
		this.dynamicArray.put(bool ? (byte)1 : (byte)0);
	}

	public void putByte(byte value)
	{
		this.dynamicArray.put(value);
	}

	public void putData(byte[] value)
	{
		this.dynamicArray.putAll(value);
	}

	public void putDouble(double value)
	{
		Converter.putDoubleBig(this.EIGHT, 0, value);
		this.dynamicArray.putAll(this.EIGHT);
	}

	public void putFloat(float value)
	{
		Converter.putFloatBig(this.FOUR, 0, value);
		this.dynamicArray.putAll(this.FOUR);
	}

	public void putShort(short value)
	{
		throw new UnsupportedOperationException();
	}

	public void putString(String string)
	{
		if (string == null)
			string = "";
		
		//not the best solution, but efficient and correct. Maybe, in the future, use Converter.putString()
		byte[] temp = Converter.convertString(string);
		Converter.putInt(this.FOUR, 0, temp.length, ByteOrder.BIG_ENDIAN);
		this.dynamicArray.putAll(this.FOUR);
		this.dynamicArray.putAll(temp);
	}

}
