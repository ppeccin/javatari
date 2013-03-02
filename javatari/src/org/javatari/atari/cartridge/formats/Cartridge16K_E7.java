// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;

/**
 * Implements the 16K "E7" M-Network format
 */
public final class Cartridge16K_E7 extends CartridgeBanked {

	private Cartridge16K_E7(byte[] content, String contentName) {
		super(content, contentName, FORMAT);
	}

	@Override
	public byte readByte(int address) {	
		maskAddress(address);
		// Check for Extra RAM Slice1 (always ON)
		if (maskedAddress >= 0x0900 && maskedAddress <= 0x09ff)
			return extraRAM[extraRAMSlice1Offset + maskedAddress - 0x0900];
		// Check for Extra RAM Slice0
		if (extraRAMSlice0Active && maskedAddress >= 0x0400 && maskedAddress <= 0x07ff)
			return extraRAM[maskedAddress - 0x0400];
		// ROM			
		if (maskedAddress < ROM_FIXED_SLICE_START)
			return bytes[bankAddressOffset + maskedAddress];		// ROM Selectable Slice
		else
			return bytes[ROM_FIXED_SLICE_OFFSET + maskedAddress];	// ROM Fixed Slice
	}

	@Override
	public void writeByte(int address, byte b) {	
		maskAddress(address);
		// Check for Extra RAM Slice1 (always ON)
		if (maskedAddress >= 0x0800 && maskedAddress <= 0x08ff)
			extraRAM[extraRAMSlice1Offset + maskedAddress - 0x0800] = b;
		else // Check for Extra RAM Slice0
 			if (extraRAMSlice0Active && maskedAddress <= 0x03ff)
				extraRAM[maskedAddress] = b;
	}

	@Override
	protected void performBankSwitchOnMaskedAddress() {
		// Check if address is within range of bank selection
		if (maskedAddress < 0x0fe0 || maskedAddress > 0x0feb)
			return;

		if (/* maskedAddress >= 0x0fe0 && */ maskedAddress <= 0x0fe6)	// Selectable ROM Slice
			bankAddressOffset = BANK_SIZE * (maskedAddress - 0x0fe0);
		else if (maskedAddress == 0x0fe7)								// Extra RAM Slice0
				extraRAMSlice0Active = true;							
			else if (/* maskedAddress >= 0x0fe8 && */ maskedAddress <= 0x0feb)	// Extra RAM Slice1
					extraRAMSlice1Offset = EXTRA_RAM_SLICE1_START + EXTRA_RAM_SLICE1_BANK_SIZE * (maskedAddress - 0x0fe8);
	}

	@Override
	public Cartridge16K_E7 clone() {
		Cartridge16K_E7 clone = (Cartridge16K_E7)super.clone();
		if (extraRAM != null) clone.extraRAM = extraRAM.clone();
		return clone;
	}


	private byte[] extraRAM = new byte[2048];
	private boolean extraRAMSlice0Active = false;
	private int extraRAMSlice1Offset = EXTRA_RAM_SLICE1_START;

	
	private static final int SIZE = 16384;
	private static final int BANK_SIZE = 2048;
	private static final int ROM_FIXED_SLICE_START = 0x0800;
	private static final int ROM_FIXED_SLICE_OFFSET = SIZE - BANK_SIZE - ROM_FIXED_SLICE_START;
	private static final int EXTRA_RAM_SLICE1_START = 1024;
	private static final int EXTRA_RAM_SLICE1_BANK_SIZE = 256;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("E7", "16K M-Network") {
		@Override
		public Cartridge create(byte[] content, String contentName) {
			return new Cartridge16K_E7(content, contentName);
		}
		@Override
		public CartridgeFormatOption getOption(byte content[], String contentName) {
			if (content.length != SIZE) return null;
			return new CartridgeFormatOptionHinted(102, this, contentName);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}