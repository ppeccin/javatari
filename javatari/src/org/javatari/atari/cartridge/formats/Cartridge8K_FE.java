// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.cartridge.ROM;

/**
 * Implements the 8K "FE" Robotank/Decathlon format
 */
public final class Cartridge8K_FE extends CartridgeBanked {

	private Cartridge8K_FE(ROM rom) {
		super(rom, FORMAT);
		bankAddressOffset = BANK_SIZE;
	}

	@Override
	public void maskAddress(int address) {
		// Bankswitching: Look at the address to determine the correct bank to be
		if ((address & 0x2000) != 0) {		// Check bit 13. Address is like Fxxx or Dxxx?
			if (bankAddressOffset != 0) bankAddressOffset = 0;
		} else {
			if (bankAddressOffset != BANK_SIZE) bankAddressOffset = BANK_SIZE;
		}
		super.maskAddress(address);
	}

	@Override
	protected void performBankSwitchOnMaskedAddress() {
		// Bank switching is not done within masked address range
		// Its done directly before masking address
	}

	
	private static final int SIZE = 8192;
	private static final int BANK_SIZE = 4096;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("FE", "8K Robotank/Decathlon") {
		@Override
		public Cartridge createCartridge(ROM rom) {
			return new Cartridge8K_FE(rom);
		}
		@Override
		public CartridgeFormatOption getOption(ROM rom) {
			if (rom.content.length != SIZE) return null;
			return new CartridgeFormatOption(103, this, rom);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}
