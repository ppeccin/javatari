// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.cartridge.ROM;

/**
 * Implements the 8K-64K "EF" H. Runner (+RAM) format
 */
public final class Cartridge8K_64K_EF extends CartridgeBankedByMaskedRange {

	private Cartridge8K_64K_EF(ROM rom) {
		super(rom, FORMAT, BASE_BANKSW_ADDRESS, null, 128);		// 128 RAM if SC mode ON
	}


	private static final int MIN_SIZE = 8192;
	private static final int MAX_SIZE = 65536;
	private static final int BANK_SIZE = 4096;
	private static final int BASE_BANKSW_ADDRESS = 0x0fe0;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("EF", "8K-64K H. Runner (+RAM)") {
		@Override
		public Cartridge createCartridge(ROM rom) {
			return new Cartridge8K_64K_EF(rom);
		}
		@Override
		public CartridgeFormatOption getOption(ROM rom) {
			if (rom.content.length % BANK_SIZE != 0 || rom.content.length < MIN_SIZE || rom.content.length > MAX_SIZE) return null;
			return new CartridgeFormatOption(114, this, rom);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}

