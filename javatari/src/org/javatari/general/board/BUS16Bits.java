// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.board;

public interface BUS16Bits {

	// Address (int) may be out of bounds. Implementations should mask it to 0xfff 

	public byte readByte(int address);

	public void writeByte(int address, byte b);

}
