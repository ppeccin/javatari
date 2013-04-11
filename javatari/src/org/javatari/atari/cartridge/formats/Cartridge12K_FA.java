// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.cartridge.ROM;

/**
 * Implements the 12K "FA" CBS RAM Plus format
 */
public final class Cartridge12K_FA extends CartridgeBankedByMaskedRange {

	private Cartridge12K_FA(ROM rom) {
		super(rom, FORMAT, BASE_BANKSW_ADDRESS, true, 256);		// SuperChip always ON, 256 RAM
	}


	private static final int SIZE = 12288;
	private static final int BASE_BANKSW_ADDRESS = 0x0ff8;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("FA", "12K CBS RAM Plus") {
		@Override
		public Cartridge createCartridge(ROM rom) {
			return new Cartridge12K_FA(rom);
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

