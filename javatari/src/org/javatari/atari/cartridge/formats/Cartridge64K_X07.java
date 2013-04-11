// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.cartridge.ROM;

/**
 * Implements the 64K "X07" AtariAge format
 */
public class Cartridge64K_X07 extends CartridgeBankedByBusMonitoring {

	protected Cartridge64K_X07(ROM rom) {
		super(rom, FORMAT);
	}

	@Override
	protected void performBankSwitchOnMonitoredAccess(int address) {
		if ((address & 0x180f) == 0x080d)		// Method 1
			bankAddressOffset = ((address & 0x00f0) >> 4) * BANK_SIZE;	// Pick bank from bits 7-4
		else if (bankAddressOffset >= BANK_14_ADDRESS && (address & 0x1880) == 0x0000) 	// Method 2, only if at bank 14 or 15
			bankAddressOffset = ((address & 0x0040) == 0 ? 14 : 15) * BANK_SIZE;	// Pick bank 14 or 15 from bit 6
	}
	

	protected static final int BANK_SIZE = 4096;
	protected static final int BANK_14_ADDRESS = 14 * BANK_SIZE;
	protected static final int SIZE = 16 * BANK_SIZE;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("X07", "64K AtariAge") {
		@Override
		public Cartridge createCartridge(ROM rom) {
			return new Cartridge64K_X07(rom);
		}
		@Override
		public CartridgeFormatOption getOption(ROM rom) {
			if (rom.content.length != SIZE) return null;
			return new CartridgeFormatOption(102, this, rom);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}