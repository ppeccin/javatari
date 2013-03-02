// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;

/**
 * Implements the 8K "E0" Parker Bros. format
 */
public final class Cartridge8K_E0 extends CartridgeBanked {

	private Cartridge8K_E0(byte[] content, String contentName) {
		super(content, contentName, FORMAT);
	}

	@Override
	public byte readByte(int address) {		
		maskAddress(address);
		// Always add the correct offset to access bank selected on the corresponding slice
		if (maskedAddress < 0x0400)		// Slice 0
			return bytes[slice0AddressOffset + maskedAddress];
		if (maskedAddress < 0x0800)		// Slice 1
			return bytes[slice1AddressOffset + maskedAddress - 0x0400];
		if (maskedAddress < 0x0c00)		// Slice 2
			return bytes[slice2AddressOffset + maskedAddress - 0x0800];
		// Slice 3 (0x0c00 - 0x0fff) is always at 0x1c00 (bank 7)
		return bytes[0x1000 + maskedAddress];	
	}

	@Override
	protected void performBankSwitchOnMaskedAddress() {
		// Check if address is within range of bank selection
		if (maskedAddress < 0x0fe0 || maskedAddress > 0x0ff7)
			return;
					
		// Each bank is 0x0400 bytes each, 0 to 7
		if (/* maskedAddress >= 0x0fe0 && */ maskedAddress <= 0x0fe7)	// Slice 0 bank selection
			slice0AddressOffset = (maskedAddress - 0x0fe0) * 0x0400;
		else if (/* maskedAddress >= 0x0fe8 && */ maskedAddress <= 0x0fef)	// Slice 1 bank selection
				slice1AddressOffset = (maskedAddress - 0x0fe8) * 0x0400;
			else if (/* maskedAddress >= 0x0ff0 && */ maskedAddress <= 0x0ff7)	// Slice 2 bank selection
				slice2AddressOffset = (maskedAddress - 0x0ff0) * 0x0400;
	}
	

	private int slice0AddressOffset = 0;
	private int slice1AddressOffset = 0;
	private int slice2AddressOffset = 0;
	// Slice 3 is fixed at bank 7

	
	private static final int SIZE = 8192;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("E0", "8K Parker Bros.") {
		@Override
		public Cartridge create(byte[] content, String contentName) {
			return new Cartridge8K_E0(content, contentName);
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