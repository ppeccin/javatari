// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.cartridge.ROM;

/**
 * Implements the 32K "F4" and "F4SC" formats
 */
public final class Cartridge32K_F4 extends CartridgeBankedByMaskedRange {

	private Cartridge32K_F4(ROM rom) {
		super(rom, FORMAT, BASE_BANKSW_ADDRESS, null, 128);		// 128 RAM if SC mode ON
	}

	
	private static final int SIZE = 32768;
	private static final int BASE_BANKSW_ADDRESS = 0x0ff4;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("F4", "32K Atari (+RAM)") {
		@Override
		public Cartridge createCartridge(ROM rom) {
			return new Cartridge32K_F4(rom);
		}
		@Override
		public CartridgeFormatOption getOption(ROM rom) {
			if (rom.content.length != SIZE) return null;
			return new CartridgeFormatOption(101, this, rom);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}

