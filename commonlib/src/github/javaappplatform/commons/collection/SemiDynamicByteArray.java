/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.collection;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO javadoc
 *
 * @author funsheep
 */
public class SemiDynamicByteArray
{

	public static final int BLOCK_SIZE = 400;

	private final ArrayList<byte[]> list = new ArrayList<byte[]>();
	private final int blockSize;

	private int cursor = 0;
	private int pointer = 0;
	private int current = 0;
	private int size = 0;
	private int capacity = 0;


	/**
	 * TODO javadoc
	 */
	public SemiDynamicByteArray()
	{
		this(BLOCK_SIZE);
	}

	/**
	 * TODO javadoc
	 * @param blocksize
	 */
	public SemiDynamicByteArray(int blocksize)
	{
		this.blockSize = blocksize;
	}


	public void put(byte value)
	{
		this.ensureCapacity(this.cursor+1);
		if (this.pointer == this.list.get(this.current).length)
		{
			this.current++;
			this.pointer = 0;
		}
		this.list.get(this.current)[this.pointer] = value;
		this.pointer++;
		this.cursor++;
		this.size = Math.max(this.size, this.cursor);
	}

	public void putAt(byte value, int position)
	{
		this.cursor(position);
		this.put(value);
	}

	public void putAt(byte[] value, int off, int len, int position)
	{
		this.cursor(position);
		this.put(value, off, len);
	}

	public void put(byte[] value, int off, int len)
	{
		this.ensureCapacity(this.cursor + len);

		int stillToAppend = len;
		while (stillToAppend > 0)
		{
			final int leftSpace = this.list.get(this.current).length - this.pointer;
			final int toCopy = Math.min(stillToAppend, leftSpace);
			if (toCopy > 0)
				System.arraycopy(value, off + len - stillToAppend, this.list.get(this.current), this.pointer, toCopy);
			this.pointer += toCopy;
			stillToAppend -= toCopy;
			if (stillToAppend > 0)
			{
				this.current++;
				this.pointer = 0;
			}
		}
		this.cursor += len;
		this.size = Math.max(this.size, this.cursor);
	}

	public void putAll(byte[] value)
	{
		this.put(value, 0, value.length);
	}

	public void putAll(ByteBuffer value)
	{
		this.ensureCapacity(this.cursor + value.remaining());

		this.cursor += value.remaining();
		while (value.remaining() > 0)
		{
			final int leftSpace = this.list.get(this.current).length - this.pointer;
			final int toCopy = Math.min(value.remaining(), leftSpace);
			for (int i = 0; i < toCopy; i++)
			{
				this.list.get(this.current)[this.pointer++] = value.get();
			}
			if (value.remaining() > 0)
			{
				this.current++;
				this.pointer = 0;
			}
		}
		this.size = Math.max(this.size, this.cursor);
	}

	public void ensureCapacity(int _capacity)
	{
		if (_capacity > this.capacity)
		{
			final byte[] _arr = new byte[Math.max(this.blockSize, _capacity - this.capacity)];
			this.list.add(_arr);
			this.capacity += _arr.length;
		}
	}

	public void incCursor()
	{
		this.ensureCapacity(this.cursor()+1);
		if (this.pointer == this.list.get(this.current).length)
		{
			this.current++;
			this.pointer = 0;
		}
		else
			this.pointer++;
		this.cursor++;
	}

	public void decCursor()
	{
		if (this.cursor-1 < 0)
			throw new IllegalArgumentException("Requested position [-1] < 0");
		if (this.pointer == 0)
		{
			this.current--;
			this.pointer = this.list.get(this.current).length;
		}
		this.cursor--;
		this.pointer--;
	}

	public void moveCursor(int delta)
	{
		if (delta > 0){
			this.ensureCapacity(this.cursor() + delta);
			this.cursor += delta;
			while(delta > 0 && delta > this.list.get(this.current).length - this.pointer){
				delta -= this.list.get(this.current).length - this.pointer;
				this.pointer = 0;
				this.current++;
			}
			this.pointer += delta;

		}
		else{
			if (this.cursor-delta < 0)
				throw new IllegalArgumentException("Requested position [" + (this.cursor-delta) + "] < 0");
			for (int i = delta; i < 0; i++)
				this.decCursor();
		}
	}

	public void cursor(final int _position)
	{
		if (_position > this.size)
			throw new IllegalArgumentException("Requested position [" + _position + "] > size of this array [" + this.size + "]");

		if (_position > this.cursor)
		{
			if (this.capacity - _position > _position - this.cursor)
			{
				int currentstart = this.cursor - this.pointer;
				while (_position > currentstart + this.list.get(this.current).length)
				{
					this.current++;
					currentstart += this.list.get(this.current).length;
				}
				this.pointer = _position - currentstart;
			}
			else
			{
				int position = this.capacity - _position - 1;
				this.current = this.list.size() - 1;
				while (this.current >= 0 && position >= this.list.get(this.current).length)
				{
					position -= this.list.get(this.current).length;
					this.current--;
				}
				this.pointer = this.list.get(this.current).length - position - 1;
			}
		}
		else if (_position < this.cursor)
		{
			if (_position > this.cursor - _position)
			{
				int currentstart = this.cursor - this.pointer;
				while (_position < currentstart)
				{
					this.current--;
					currentstart -= this.list.get(this.current).length;
				}
				this.pointer = _position - currentstart;
			}
			else
			{
				int position = _position;
				this.current = 0;
				while (this.current < this.list.size() && position >= this.list.get(this.current).length)
				{
					position -= this.list.get(this.current).length;
					this.current++;
				}
				this.pointer = position;
			}
		}
		this.cursor = _position;
	}

	public int cursor()
	{
		return this.cursor;
	}

	public byte getDateFrom(int position)
	{
		this.cursor(position);
		return this.getDate();
	}

	public byte getDate()
	{
		if (this.cursor == this.size)
			throw new IllegalArgumentException("Cursor at the end of the array");
		if (this.pointer == this.list.get(this.current).length)
		{
			this.current++;
			this.pointer = 0;
		}
		return this.list.get(this.current)[this.pointer];
	}

	public byte[] getDataFrom(byte[] dest, int position)
	{
		this.cursor(position);
		return this.getData(dest);
	}

	public byte[] getData(byte[] dest)
	{
		if (this.cursor+dest.length > this.size)
			throw new IllegalArgumentException("Requested position ["+this.cursor+"] + dest.length ["+dest.length+"] >= size of this array ["+this.size+"]");

		int i = this.current;
		int toCopy = Math.min(this.list.get(i).length-this.pointer, dest.length);
		System.arraycopy(this.list.get(i), this.pointer, dest, 0, toCopy);
		int pos = toCopy;
		while (pos < dest.length)
		{
			i++;
			toCopy = Math.min(this.list.get(i).length, dest.length - pos);
			System.arraycopy(this.list.get(i), 0, dest, pos, toCopy);
			pos += toCopy;
		}
		return dest;
	}

	public byte[] datacopy(int sourcePosition, byte[] dest, int destPosition, int length)
	{
		this.cursor(sourcePosition);
		
		if (this.cursor+length > this.size)
			throw new IllegalArgumentException("Requested position ["+this.cursor+"] + length ["+length+"] >= size of this array ["+this.size+"]");

		int i = this.current;
		int toCopy = Math.min(this.list.get(i).length-this.pointer, length);
		System.arraycopy(this.list.get(i), this.pointer, dest, destPosition, toCopy);
		int pos = toCopy;
		while (pos < length)
		{
			i++;
			toCopy = Math.min(this.list.get(i).length, length - pos);
			System.arraycopy(this.list.get(i), 0, dest, destPosition + pos, toCopy);
			pos += toCopy;
		}
		return dest;
	}

	public byte[] getData()
	{
		byte[] arr = new byte[this.size];
		int i = 0;
		int pos = 0;
		while (pos < this.size)
		{
			int toCopy = Math.min(this.list.get(i).length, this.size - pos);
			System.arraycopy(this.list.get(i), 0, arr, pos, toCopy);
			pos += toCopy;
			i++;
		}
		return arr;
	}

	public byte readDate()
	{
		byte b = this.getDate();
		this.incCursor();
		return b;
	}

	public void readData(byte[] dest)
	{
		this.getData(dest);
		this.moveCursor(dest.length);
	}

	@Deprecated
	public List<byte[]> getRAWData()
	{
		return Collections.unmodifiableList(this.list);
	}
	
	public void ensureSize(int size)
	{
		this.ensureCapacity(size);
		this.size = Math.max(this.size, size);
	}

	public int size()
	{
		return this.size;
	}

}
