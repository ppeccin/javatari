// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge.formats;

import atari.cartridge.Cartridge;
import atari.cartridge.CartridgeFormat;
import atari.cartridge.CartridgeFormatOption;

/**
 * Implements the 8K "F8" and "F8SC" formats
 */
public final class Cartridge8K_F8 extends CartridgeBankedByMaskedRange {

	private Cartridge8K_F8(byte[] content, String contentName) {
		super(content, contentName, FORMAT, BASE_BANKSW_ADDRESS, null, 128);		// 128 RAM if SC mode ON
	}

	
	private static final int SIZE = 8192;
	private static final int BASE_BANKSW_ADDRESS = 0x0ff8;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("F8", "8K Atari (+RAM)") {
		@Override
		public Cartridge create(byte[] content, String contentName) {
			return new Cartridge8K_F8(content, contentName);
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

