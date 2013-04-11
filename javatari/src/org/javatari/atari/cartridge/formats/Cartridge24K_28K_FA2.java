// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.cartridge.ROM;

/**
 * Implements the 24K-28K "FA2" CBS RAM Plus format
 */
public final class Cartridge24K_28K_FA2 extends CartridgeBankedByMaskedRange {

	private Cartridge24K_28K_FA2(ROM rom) {
		super(rom, FORMAT, BASE_BANKSW_ADDRESS, true, 256);		// SuperChip always ON, 256 RAM
	}


	private static final int SIZE24K = 32768 - 8192;
	private static final int SIZE28K = 32768 - 4096;
	private static final int BASE_BANKSW_ADDRESS = 0x0ff5;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("FA2", "24K-28K CBS RAM Plus") {
		@Override
		public Cartridge createCartridge(ROM rom) {
			return new Cartridge24K_28K_FA2(rom);
		}
		@Override
		public CartridgeFormatOption getOption(ROM rom) {
			if (rom.content.length != SIZE24K && rom.content.length != SIZE28K) return null;
			return new CartridgeFormatOption(101, this, rom);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}

