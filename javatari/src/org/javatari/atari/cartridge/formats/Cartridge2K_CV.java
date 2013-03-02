// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;

/**
 * Implements the 2K "CV" Commavid + 1K RAM format
 */
public final class Cartridge2K_CV extends Cartridge {

	private Cartridge2K_CV(byte[] content, String contentName) {
		// Always use a 4K ROM image, duplicating 2K ROMs
		super(new byte[SIZE * 2], contentName, FORMAT);
		int len = content.length;
		for (int pos = 0; pos < SIZE * 2; pos += len)
			System.arraycopy(content, 0, bytes, pos, len);
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
		public Cartridge create(byte[] content, String contentName) {
			return new Cartridge2K_CV(content, contentName);
		}
		@Override
		public CartridgeFormatOption getOption(byte content[], String contentName) {
			if (content.length != SIZE && content.length != SIZE * 2) return null;	// Also accepts 4K ROMs
			return new CartridgeFormatOptionHinted(102, this, contentName);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}