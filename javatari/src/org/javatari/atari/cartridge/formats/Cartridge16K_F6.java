// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;

/**
 * Implements the 16K "F6" and "F6SC" formats
 */
public final class Cartridge16K_F6 extends CartridgeBankedByMaskedRange {

	private Cartridge16K_F6(byte[] content, String contentName) {
		super(content, contentName, FORMAT, BASE_BANKSW_ADDRESS, null, 128);		// 128 RAM if SC mode ON
	}


	private static final int SIZE = 16384;
	private static final int BASE_BANKSW_ADDRESS = 0x0ff6;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("F6", "16K Atari (+RAM)") {
		@Override
		public Cartridge create(byte[] content, String contentName) {
			return new Cartridge16K_F6(content, contentName);
		}
		@Override
		public CartridgeFormatOption getOption(byte content[], String contentName) {
			if (content.length != SIZE) return null;
			return new CartridgeFormatOptionHinted(101, this, contentName);
		}
		private static final long serialVersionUID = 1L;
	};
	
	
	public static final long serialVersionUID = 1L;

}

