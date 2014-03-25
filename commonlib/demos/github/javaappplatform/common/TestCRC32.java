/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.common;

public class TestCRC32
{

	private int polynomial = 0xEDB88320;
	private int crc = 0;

	/**
	* Calculates a CRC value for a byte to be used by CRC calculation
	functions.
	*/
	private int CRC32Value(int i) {
	int crc = i;

	for (int j = 8; j > 0; j--) {
	if ((crc & 1) == 1)
	crc = (crc >>> 1) ^ polynomial;
	else
	crc >>>= 1;
	}
	return crc;

	}

	/**
	* Calculates the CRC-32 of a block of data all at once
	*/
	public int calculateCRC32(byte[] buffer, int offset, int length) {
	for (int i = offset; i < offset + length; i++) {
	int tmp1 = (crc >>> 8) & 0x00FFFFFF;
	int tmp2 = CRC32Value(((int) crc ^ buffer[i]) & 0xff);
	crc = tmp1 ^ tmp2;
	}
	return crc;
	}

	/**
	* Calculates the CRC-32 of a block of data all at once
	*/
	public int calculateCRC32(byte[] buffer) {
	return calculateCRC32(buffer, 0, buffer.length);
	}

	/**
	* Resets the state to process more data.
	*/
	public void reset() {
	crc = 0;
	}

	public void setPolynomial(int polynomial) {
	this.polynomial = polynomial;
	}

}
