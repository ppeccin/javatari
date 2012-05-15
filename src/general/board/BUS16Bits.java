// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.board;

public interface BUS16Bits {

	// Address (int) may be out of bounds then wrap as converted to char. No problem!

	public byte readByte(int address);

	public void writeByte(int address, byte b);

}
