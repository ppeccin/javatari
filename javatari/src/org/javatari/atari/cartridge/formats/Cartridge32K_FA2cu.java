// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import java.util.Arrays;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.cartridge.ROM;

/**
 * Implements the 32K "FA2" CBS RAM Plus in Harmony CU Image format
 */
public final class Cartridge32K_FA2cu extends Cartridge24K_28K_32K_FA2 {

	private Cartridge32K_FA2cu(ROM rom) {
		super(rom, FORMAT);
		// ROM is only 28K. The first 1024 bytes are custom ARM content. ROM begins after that
		bankAddressOffset = romStartAddress = 1024;
	    // There are a maximum of 7 * 4K banks = 28K, never 8 banks
		if (topBankSwitchAddress > 0x0ffb) topBankSwitchAddress = 0xffb;
	}

	
	private static final int SIZE32K_Harmony = 32768;
	private static final byte[] cuMagicWord = new byte[] {(byte)0x1e, (byte)0xab, (byte)0xad, (byte)0x10};

	public static final CartridgeFormat FORMAT = new CartridgeFormat("FA2cu", "32K CBS RAM Plus CU Image") {
		@Override
		public Cartridge createCartridge(ROM rom) {
			return new Cartridge32K_FA2cu(rom);
		}
		@Override
		public CartridgeFormatOption getOption(ROM rom) {
			if (rom.content.length != SIZE32K_Harmony) return null;

			// Check for the values $10adab1e, for "loadable", starting at position 32 (33rd byte)
			// This is a hint that the content is in CU format
			byte[] hint = Arrays.copyOfRange(rom.content, 32 , 32 + 4);
			boolean foundHint = Arrays.equals(hint, cuMagicWord);
			
			return new CartridgeFormatOption(foundHint ? 103 - 50 : 103, this, rom);
		}
		private static final long serialVersionUID = 1L;
	};
	

	public static final long serialVersionUID = 1L;

}
