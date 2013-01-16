// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.board;

import java.util.Arrays;

public final class RAM64k implements BUS16Bits {

	public RAM64k() {
		Arrays.fill(bytes, (byte)0x00);
	}
	
	@Override
	public byte readByte(int address) {		
		return bytes[address & 0xffff];
	}

	@Override
	public void writeByte(int address, byte b) {	
		bytes[address & 0xffff] = b;		
	}

	public void dump(int init, int quant) {
		System.out.printf("MEMORY DUMP FROM %04x:\n", init);
		for(int i = init; i < init+quant; i++)
			System.out.printf("%02x ", unsignedByte(i));
		System.out.println();
	}
	
	private int unsignedByte(int address) {  
		return readByte(address) & 0xff;
	}
	
	private byte[] bytes = new byte[65536];

}
