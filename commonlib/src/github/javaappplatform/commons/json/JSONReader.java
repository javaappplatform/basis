/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.json;

import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.list.array.TShortArrayList;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonToken;

public class JSONReader implements Closeable
{

	private final JsonParser parser;


	public JSONReader(JsonParser parser)
	{
		this.parser = parser;
	}

	public JSONReader(byte[] data) throws IOException
	{
		this(data, 0, data.length);
	}

	public JSONReader(File f) throws IOException
	{
		this((new JsonFactory()).createParser(f));
		this.parser.enable(Feature.ALLOW_COMMENTS);
	}

	public JSONReader(InputStream in) throws IOException
	{
		this((new JsonFactory()).createParser(in));
		this.parser.enable(Feature.ALLOW_COMMENTS);
	}

	public JSONReader(Reader r) throws IOException
	{
		this((new JsonFactory()).createParser(r));
		this.parser.enable(Feature.ALLOW_COMMENTS);
	}

	public JSONReader(String content) throws IOException
	{
		this((new JsonFactory()).createParser(content));
		this.parser.enable(Feature.ALLOW_COMMENTS);
	}

	public JSONReader(URL url) throws IOException
	{
		this(url.openStream());
	}

	public JSONReader(byte[] data, int offset, int len) throws IOException
	{
		this((new JsonFactory()).createParser(data, offset, len));
		this.parser.enable(Feature.ALLOW_COMMENTS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		this.parser.close();
	}


	public BigInteger getBigIntegerValue() throws IOException
	{
		return this.parser.getBigIntegerValue();
	}

	public byte[] getBinaryValue() throws JsonParseException, IOException
	{
		return this.parser.getBinaryValue();
	}

	public boolean getBooleanValue() throws IOException
	{
		return this.parser.getBooleanValue();
	}

	public byte getByteValue() throws IOException
	{
		return this.parser.getByteValue();
	}

	public JsonLocation getCurrentLocation()
	{
		return this.parser.getCurrentLocation();
	}

	public String getCurrentName() throws IOException
	{
		return this.parser.getCurrentName();
	}

	public JsonToken getCurrentToken()
	{
		return this.parser.getCurrentToken();
	}

	public void currentToken(JsonToken token) throws IOException
	{
		if (this.getCurrentToken() != token)
			throw new JsonParseException("Invalid token " + this.parser.getCurrentToken() + ". Expected token " + token, this.parser.getCurrentLocation());
	}

	public double getDoubleValue() throws IOException
	{
		return this.parser.getDoubleValue();
	}

	public float getFloatValue() throws IOException
	{
		return this.parser.getFloatValue();
	}

	public int getIntValue() throws IOException
	{
		return this.parser.getIntValue();
	}

	public long getLongValue() throws IOException
	{
		return this.parser.getLongValue();
	}

	public short getShortValue() throws IOException
	{
		return this.parser.getShortValue();
	}

	public String getCurrentText() throws IOException
	{
		return this.parser.getText();
	}

	public BigInteger nextBigIntegerValue() throws IOException
	{
		this.nextToken(JsonToken.VALUE_NUMBER_INT);
		return this.parser.getBigIntegerValue();
	}

	public byte[] nextBinaryValue() throws IOException
	{
		this.nextToken(JsonToken.VALUE_STRING);
		return this.parser.getBinaryValue();
	}

	public boolean nextBooleanValue() throws IOException
	{
		JsonToken token = this.parser.nextToken();
		if (token != JsonToken.VALUE_TRUE && token != JsonToken.VALUE_FALSE)
			throw new JsonParseException("Invalid token " + token + ". Expected token " + JsonToken.VALUE_TRUE +" or "+ JsonToken.VALUE_FALSE, this.parser.getCurrentLocation());
		return this.parser.getBooleanValue();
	}

	public byte nextByteValue() throws IOException
	{
		this.nextToken(JsonToken.VALUE_NUMBER_INT);
		return this.parser.getByteValue();
	}

	public double nextDoubleValue() throws IOException
	{
		this.nextToken(JsonToken.VALUE_NUMBER_FLOAT);
		return this.parser.getDoubleValue();
	}

	public float nextFloatValue() throws IOException
	{
		this.nextToken(JsonToken.VALUE_NUMBER_FLOAT);
		return this.parser.getFloatValue();
	}

	public int nextIntValue() throws IOException
	{
		this.nextToken(JsonToken.VALUE_NUMBER_INT);
		return this.parser.getIntValue();
	}

	public long nextLongValue() throws IOException
	{
		this.nextToken(JsonToken.VALUE_NUMBER_INT);
		return this.parser.getLongValue();
	}

	public short nextShortValue() throws IOException
	{
		this.nextToken(JsonToken.VALUE_NUMBER_INT);
		return this.parser.getShortValue();
	}

	public String nextText() throws IOException
	{
		this.nextToken();
		return this.parser.getText();
	}

	public BigInteger nextBigIntegerField(String name) throws IOException
	{
		this.nextFieldName(name);
		return this.nextBigIntegerValue();
	}

	public boolean nextBooleanField(String name) throws IOException
	{
		this.nextFieldName(name);
		return this.nextBooleanValue();
	}

	public byte nextByteField(String name) throws IOException
	{
		this.nextFieldName(name);
		return this.nextByteValue();
	}

	public double nextDoubleField(String name) throws IOException
	{
		this.nextFieldName(name);
		return this.nextDoubleValue();
	}

	public float nextFloatField(String name) throws IOException
	{
		this.nextFieldName(name);
		return this.nextFloatValue();
	}

	public int nextIntField(String name) throws IOException
	{
		this.nextFieldName(name);
		return this.nextIntValue();
	}

	public long nextLongField(String name) throws IOException
	{
		this.nextFieldName(name);
		return this.nextLongValue();
	}

	public short nextShortField(String name) throws IOException
	{
		this.nextFieldName(name);
		return this.nextShortValue();
	}

	public String nextTextField(String name) throws IOException
	{
		this.nextFieldName(name);
		return this.nextText();
	}

	public boolean[] nextBooleanArray() throws IOException
	{
		ArrayList<Boolean> list = new ArrayList<Boolean>();
		this.nextToken(JsonToken.START_ARRAY);
		while (this.nextToken() != JsonToken.END_ARRAY)
		{
			list.add(Boolean.valueOf(this.getBooleanValue()));
		}
		boolean[] b = new boolean[list.size()];
		for (int i = 0; i < b.length; i++)
			b[i] = list.get(i).booleanValue();
		return b;
	}

	public byte[] nextByteArray() throws IOException
	{
		final TByteArrayList list = new TByteArrayList();
		this.nextToken(JsonToken.START_ARRAY);
		while (this.nextToken() != JsonToken.END_ARRAY)
		{
			list.add(this.getByteValue());
		}
		return list.toArray();
	}

	public short[] nextShortArray() throws IOException
	{
		final TShortArrayList list = new TShortArrayList();
		this.nextToken(JsonToken.START_ARRAY);
		while (this.nextToken() != JsonToken.END_ARRAY)
		{
			list.add(this.getShortValue());
		}
		return list.toArray();
	}

	public int[] nextIntArray() throws IOException
	{
		final TIntArrayList list = new TIntArrayList();
		this.nextToken(JsonToken.START_ARRAY);
		while (this.nextToken() != JsonToken.END_ARRAY)
		{
			list.add(this.getIntValue());
		}
		return list.toArray();
	}

	public long[] nextLongArray() throws IOException
	{
		final TLongArrayList list = new TLongArrayList();
		this.nextToken(JsonToken.START_ARRAY);
		while (this.nextToken() != JsonToken.END_ARRAY)
		{
			list.add(this.getLongValue());
		}
		return list.toArray();
	}

	public float[] nextFloatArray() throws IOException
	{
		final TFloatArrayList list = new TFloatArrayList();
		this.nextToken(JsonToken.START_ARRAY);
		while (this.nextToken() != JsonToken.END_ARRAY)
		{
			list.add(this.getFloatValue());
		}
		return list.toArray();
	}

	public double[] nextDoubleArray() throws IOException
	{
		final TDoubleArrayList list = new TDoubleArrayList();
		this.nextToken(JsonToken.START_ARRAY);
		while (this.nextToken() != JsonToken.END_ARRAY)
		{
			list.add(this.getDoubleValue());
		}
		return list.toArray();
	}

	public String[] nextStringArray() throws IOException
	{
		final ArrayList<String> list = new ArrayList<String>();
		this.nextToken(JsonToken.START_ARRAY);
		while (this.nextToken() != JsonToken.END_ARRAY)
		{
			list.add(this.getCurrentText());
		}
		return list.toArray(new String[list.size()]);
	}

	public void nextArrayStart(String fieldname) throws IOException
	{
		this.nextFieldName(fieldname);
		this.nextToken(JsonToken.START_ARRAY);
	}

	public void nextEndArray() throws IOException
	{
		this.nextToken(JsonToken.END_ARRAY);
	}

	public void nextObjectStart(String fieldname) throws IOException
	{
		this.nextFieldName(fieldname);
		this.nextToken(JsonToken.START_OBJECT);
	}

	public void nextEndObject() throws IOException
	{
		this.nextToken(JsonToken.END_OBJECT);
	}

	private boolean _nextToken = false;

	public JsonToken nextToken() throws IOException
	{
		if(this._nextToken)
		{
			this._nextToken = false;
			return this.getCurrentToken();
		}

		return this.parser.nextToken();
	}

	public JsonToken nextValue() throws IOException
	{
		return this.parser.nextValue();
	}

	public void skipChildren() throws IOException
	{
		this.parser.skipChildren();
	}

	public void nextToken(JsonToken token) throws IOException
	{
		if (this.nextToken() != token)
			throw new JsonParseException("Invalid token " + this.parser.getCurrentToken() + ". Expected token " + token, this.parser.getCurrentLocation());
	}

	public void nextFieldName(String name) throws IOException
	{
		this.nextToken(JsonToken.FIELD_NAME);
		if (!this.getCurrentName().equals(name))
			throw new JsonParseException("Found invalid field " + this.getCurrentName() + ". Expected field " + name, this.parser.getCurrentLocation());
	}

	public void nextNullField(String name) throws IOException
	{
		this.nextFieldName(name);
		this.nextToken(JsonToken.VALUE_NULL);
	}

	public void validateState(JsonToken token) throws IOException
	{
		if (this.getCurrentToken() != token)
			throw new JsonParseException("Invalid token " + this.parser.getCurrentToken() + ". Expected token " + token, this.parser.getCurrentLocation());
	}

}
