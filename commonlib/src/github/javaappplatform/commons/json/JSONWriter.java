/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.json;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * TODO javadoc
 * @author funsheep
 */
public class JSONWriter implements Flushable, Closeable
{


	private final JsonGenerator gen;


	/**
	 *
	 */
	public JSONWriter(Writer w) throws IOException
	{
		this.gen = (new JsonFactory()).createGenerator(w);
	}

	public JSONWriter(OutputStream strem, String enc) throws IOException
	{
		this.gen = (new JsonFactory()).createGenerator(strem, JsonEncoding.UTF8);
	}

	public JSONWriter(JsonGenerator generator)
	{
		this.gen = generator;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flush() throws IOException
	{
		this.gen.flush();
	}


	public void writeArrayFieldStart(String fieldName) throws IOException
	{
		this.gen.writeArrayFieldStart(fieldName);
	}

	public void writeBinary(byte[] data) throws IOException
	{
		this.gen.writeBinary(data);
	}

	public void writeBinary(byte[] data, int offset, int length) throws IOException
	{
		this.gen.writeBinary(data, offset, length);
	}

	public void writeBinaryField(String fieldName, byte[] data) throws IOException
	{
		this.gen.writeBinaryField(fieldName, data);
	}

	public void writeBinaryField(String fieldName, InputStream stream) throws IOException
	{
		this.gen.writeFieldName(fieldName);
		this.gen.writeBinary(stream, -1);
	}

	public void write(boolean bool) throws IOException
	{
		this.gen.writeBoolean(bool);
	}

	public void writeField(String fieldName, boolean state) throws IOException
	{
		this.gen.writeBooleanField(fieldName, state);
	}

	public void endArray() throws IOException
	{
		this.gen.writeEndArray();
	}

	public void endObject() throws IOException
	{
		this.gen.writeEndObject();
	}

	public void writeFieldName(String fieldName) throws IOException
	{
		this.gen.writeFieldName(fieldName);
	}

	public void writeNull() throws IOException
	{
		this.gen.writeNull();
	}

	public void writeNullField(String fieldName) throws IOException
	{
		this.gen.writeNullField(fieldName);
	}

	public void write(double number) throws IOException
	{
		this.gen.writeNumber(number);
	}

	public void write(float number) throws IOException
	{
		this.gen.writeNumber(number);
	}

	public void write(int number) throws IOException
	{
		this.gen.writeNumber(number);
	}

	public void write(BigDecimal number) throws IOException
	{
		this.gen.writeNumber(number);
	}

	public void write(BigInteger number) throws IOException
	{
		this.gen.writeNumber(number);
	}

	public void write(long number) throws IOException
	{
		this.gen.writeNumber(number);
	}

	public void writeField(String field, float number) throws IOException
	{
		this.gen.writeNumberField(field, number);
	}

	public void writeField(String field, double number) throws IOException
	{
		this.gen.writeNumberField(field, number);
	}

	public void writeField(String field, int number) throws IOException
	{
		this.gen.writeNumberField(field, number);
	}

	public void writeField(String field, BigDecimal number) throws IOException
	{
		this.gen.writeNumberField(field, number);
	}

	public void writeField(String field, long number) throws IOException
	{
		this.gen.writeNumberField(field, number);
	}

	public void startObjectField(String field) throws IOException
	{
		this.gen.writeObjectFieldStart(field);
	}

	public void startArrayField(String field) throws IOException
	{
		this.gen.writeArrayFieldStart(field);
	}

	public void startArray() throws IOException
	{
		this.gen.writeStartArray();
	}

	public void startObject() throws IOException
	{
		this.gen.writeStartObject();
	}

	public void write(char[] text, int offset, int len) throws IOException
	{
		this.gen.writeString(text, offset, len);
	}

	public void write(String text) throws IOException
	{
		this.gen.writeString(text);
	}

	public void writeField(String fieldName, String text) throws IOException
	{
		this.gen.writeStringField(fieldName, text);
	}


	public void writeArray(boolean...bb) throws IOException
	{
		this.gen.writeStartArray();
		for (boolean b : bb)
			this.gen.writeBoolean(b);
		this.gen.writeEndArray();
	}

	public void writeArray(byte...bb) throws IOException
	{
		this.gen.writeStartArray();
		for (byte b : bb)
			this.gen.writeNumber(b);
		this.gen.writeEndArray();
	}

	public void writeArray(short...bb) throws IOException
	{
		this.gen.writeStartArray();
		for (short b : bb)
			this.gen.writeNumber(b);
		this.gen.writeEndArray();
	}

	public void writeArray(int...bb) throws IOException
	{
		this.gen.writeStartArray();
		for (int b : bb)
			this.gen.writeNumber(b);
		this.gen.writeEndArray();
	}

	public void writeArray(long...bb) throws IOException
	{
		this.gen.writeStartArray();
		for (long b : bb)
			this.gen.writeNumber(b);
		this.gen.writeEndArray();
	}

	public void writeArray(float...bb) throws IOException
	{
		this.gen.writeStartArray();
		for (float b : bb)
			this.gen.writeNumber(b);
		this.gen.writeEndArray();
	}

	public void writeArray(double...bb) throws IOException
	{
		this.gen.writeStartArray();
		for (double b : bb)
			this.gen.writeNumber(b);
		this.gen.writeEndArray();
	}

	public void writeArray(String...bb) throws IOException
	{
		this.gen.writeStartArray();
		for (String b : bb)
			this.gen.writeString(b);
		this.gen.writeEndArray();
	}


	public void writeArrayField(String key, boolean...bb) throws IOException
	{
		this.gen.writeArrayFieldStart(key);
		for (boolean b : bb)
			this.gen.writeBoolean(b);
		this.gen.writeEndArray();
	}

	public void writeArrayField(String key, byte...bb) throws IOException
	{
		this.gen.writeArrayFieldStart(key);
		for (byte b : bb)
			this.gen.writeNumber(b);
		this.gen.writeEndArray();
	}

	public void writeArrayField(String key, short...bb) throws IOException
	{
		this.gen.writeArrayFieldStart(key);
		for (short b : bb)
			this.gen.writeNumber(b);
		this.gen.writeEndArray();
	}

	public void writeArrayField(String key, int...bb) throws IOException
	{
		this.gen.writeArrayFieldStart(key);
		for (int b : bb)
			this.gen.writeNumber(b);
		this.gen.writeEndArray();
	}

	public void writeArrayField(String key, long...bb) throws IOException
	{
		this.gen.writeArrayFieldStart(key);
		for (long b : bb)
			this.gen.writeNumber(b);
		this.gen.writeEndArray();
	}

	public void writeArrayField(String key, float...bb) throws IOException
	{
		this.gen.writeArrayFieldStart(key);
		for (float b : bb)
			this.gen.writeNumber(b);
		this.gen.writeEndArray();
	}

	public void writeArrayField(String key, double...bb) throws IOException
	{
		this.gen.writeArrayFieldStart(key);
		for (double b : bb)
			this.gen.writeNumber(b);
		this.gen.writeEndArray();
	}

	public void writeArrayField(String key, String...bb) throws IOException
	{
		this.gen.writeArrayFieldStart(key);
		for (String b : bb)
			this.gen.writeString(b);
		this.gen.writeEndArray();
	}


	public void writeRawValue(String jsonValue) throws IOException
	{
		this.gen.writeRawValue(jsonValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		this.gen.close();
	}

}
