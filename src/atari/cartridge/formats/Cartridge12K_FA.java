// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge.formats;

import atari.cartridge.Cartridge;
import atari.cartridge.CartridgeFormat;
import atari.cartridge.CartridgeFormatOption;

/**
 * Implements the 12K "FA" CBS RAM Plus format
 */
public final class Cartridge12K_FA extends CartridgeBankedByMaskedRange {

	private Cartridge12K_FA(byte[] content, String contentName) {
		super(content, contentName, FORMAT, BASE_BANKSW_ADDRESS, true, 256);		// SuperChip always ON, 256 RAM
	}


	private static final int SIZE = 12288;
	private static final int BASE_BANKSW_ADDRESS = 0x0ff8;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("FA", "12K CBS RAM Plus") {
		@Override
		public Cartridge create(byte[] content, String contentName) {
			return new Cartridge12K_FA(content, contentName);
		}
		@Override
		public CartridgeFormatOption getOption(byte[] content, String contentName) {
			if (content.length != SIZE) return null;
			return new CartridgeFormatOptionHinted(101, this, contentName);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}

