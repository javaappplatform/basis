/*
	This file is part of the d3fact network library.
	Copyright (C) 2007-2012 d3fact Project Team

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.network.msg;

import github.javaappplatform.network.internal.Message;

import java.util.Arrays;

public class MessageReader
{

	private final byte[] four  = new byte[4];
	private final byte[] eight = new byte[8];

	private Message msg;
	private int offset;


	public MessageReader()
	{
		//empty constructor
		//first call would have to be to reset.
	}

	public MessageReader(IMessage msg)
	{
		this.reset(msg);
	}


	public void reset(IMessage _msg)
	{
		this.msg = (Message) _msg;
		this.offset = 0;
		if (this.msg.bodyType() == Message.BODYTYPE_SEMIARRAY)
			this.msg.body2().cursor(0);
	}

	public IMessage current()
	{
		return this.msg;
	}

	public int readInt()
	{
		if (this.msg.bodyType() == Message.BODYTYPE_BYTEARRAY)
		{
			int out = Converter.getIntBig(this.msg.body(), this.offset);
			this.offset+=4;
			return out;
		}

		this.msg.body2().readData(this.four);
		return Converter.getIntBig(this.four, 0);
	}


	public long readLong()
	{
		if (this.msg.bodyType() == Message.BODYTYPE_BYTEARRAY)
		{
			long out = Converter.getLongBig(this.msg.body(), this.offset);
			this.offset+=8;
			return out;
		}

		this.msg.body2().readData(this.eight);
		return Converter.getLongBig(this.eight, 0);
	}


	public byte[] readByteArray(int size)
	{
		if (this.msg.bodyType() == Message.BODYTYPE_BYTEARRAY)
		{
			byte[] out = Arrays.copyOfRange(this.msg.body(), this.offset, this.offset+size);
			this.offset+=size;
			return out;
		}

		byte[] read = new byte[size];
		this.msg.body2().readData(read);
		return read;
	}


	public boolean readBoolean()
	{
		if (this.msg.bodyType() == Message.BODYTYPE_BYTEARRAY)
		{
			boolean out = Converter.getBooleanBig(this.msg.body(), this.offset);
			this.offset++;
			return out;
		}

		return Converter.getBooleanBig(this.msg.body2().readDate());
	}


	public byte readByte()
	{
		if (this.msg.bodyType() == Message.BODYTYPE_BYTEARRAY)
		{
			byte out = this.msg.body()[this.offset];
			this.offset++;
			return out;
		}

		return this.msg.body2().readDate();
	}

	public double readDouble()
	{
		if (this.msg.bodyType() == Message.BODYTYPE_BYTEARRAY)
		{
			double out = Converter.getDoubleBig(this.msg.body(), this.offset);
			this.offset+=8;
			return out;
		}

		this.msg.body2().readData(this.eight);
		return Converter.getDoubleBig(this.eight, 0);
	}

	public float readFloat()
	{
		if (this.msg.bodyType() == Message.BODYTYPE_BYTEARRAY)
		{
			float out = Converter.getFloatBig(this.msg.body(), this.offset);
			this.offset+=4;
			return out;
		}

		this.msg.body2().readData(this.four);
		return Converter.getFloatBig(this.four, 0);
	}

	public short readShort()
	{
		throw new UnsupportedOperationException();
	}


	public String readString()
	{
		if (this.msg.bodyType() == Message.BODYTYPE_BYTEARRAY)
		{
			int length = Converter.getIntBig(this.msg.body(), this.offset);
			String str =  Converter.getString(this.msg.body(), this.offset);
			this.offset += length+4;
			return str;
		}

		int length = this.readInt();
		byte[] str = this.readByteArray(length);
		return new String(str, Converter.DEFAULT_CHARSET);
	}

}
