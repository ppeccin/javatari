// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.cartridge.ROM;


/**
 * Implements the 4K unbanked format. Smaller ROMs will be copied multiple times to fill the entire 4K
 */
public final class Cartridge4K extends Cartridge {

	private Cartridge4K(ROM rom) {
		super(rom, FORMAT);
		// Always use a 4K ROM image, multiplying the ROM internally
		bytes = new byte[MAX_SIZE];
		int len = rom.content.length;
		for (int pos = 0; pos < bytes.length; pos += len)
			System.arraycopy(rom.content, 0, bytes, pos, len);
	}


	private static final int MIN_SIZE = 8;
	private static final int MAX_SIZE = 4096;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("4K", "4K Atari") {
		@Override
		public Cartridge createCartridge(ROM rom) {
			return new Cartridge4K(rom);
		}
		@Override
		public CartridgeFormatOption getOption(ROM rom) {
			if (rom.content.length < MIN_SIZE || rom.content.length > MAX_SIZE || MAX_SIZE % rom.content.length != 0) return null;
			return new CartridgeFormatOption(101, this, rom);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}

