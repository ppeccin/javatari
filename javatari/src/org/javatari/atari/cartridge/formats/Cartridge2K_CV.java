// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.cartridge.ROM;

/**
 * Implements the 2K "CV" Commavid + 1K RAM format
 */
public final class Cartridge2K_CV extends Cartridge {

	private Cartridge2K_CV(ROM rom) {
		super(rom, FORMAT);
		// Always use a 4K ROM image, multiplying the ROM internally
		bytes = new byte[SIZE * 2];
		int len = rom.content.length;
		for (int pos = 0; pos < bytes.length; pos += len)
			System.arraycopy(rom.content, 0, bytes, pos, len);
	}

	@Override
	public byte readByte(int address) {		
		maskAddress(address);
		// Check for Extra RAM reads
		if (maskedAddress < 0x0400)				// RAM 
			return extraRAM[maskedAddress];
		return bytes[maskedAddress];			// ROM	
	}

	@Override
	public void writeByte(int address, byte b) {
		maskAddress(address);
		// Check for Extra RAM writes
		if (maskedAddress >= 0x0400 && maskedAddress <= 0x07ff)
			extraRAM[maskedAddress - 0x0400] = b;
	}
		

	@Override
	public Cartridge2K_CV clone() {
		Cartridge2K_CV clone = (Cartridge2K_CV)super.clone();
		clone.extraRAM = extraRAM.clone();
		return clone;
	}

	
	private byte[] extraRAM = new byte[1024];


	private static final int SIZE = 2048;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("CV", "2K Commavid +RAM") {
		@Override
		public Cartridge createCartridge(ROM rom) {
			return new Cartridge2K_CV(rom);
		}
		@Override
		public CartridgeFormatOption getOption(ROM rom) {
			if (rom.content.length != SIZE && rom.content.length != SIZE * 2) return null;	// Also accepts 4K ROMs
			return new CartridgeFormatOption(102, this, rom);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}