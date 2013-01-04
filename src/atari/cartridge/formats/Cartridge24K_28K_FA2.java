// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge.formats;

import atari.cartridge.Cartridge;
import atari.cartridge.CartridgeFormat;
import atari.cartridge.CartridgeFormatOption;

/**
 * Implements the 24K-28K "FA2" CBS RAM Plus format
 */
public final class Cartridge24K_28K_FA2 extends CartridgeBankedByMaskedRange {

	private Cartridge24K_28K_FA2(byte[] content, String contentName) {
		super(content, contentName, FORMAT, BASE_BANKSW_ADDRESS, true, 256);		// SuperChip always ON, 256 RAM
	}


	private static final int SIZE24K = 32768 - 8192;
	private static final int SIZE28K = 32768 - 4096;
	private static final int BASE_BANKSW_ADDRESS = 0x0ff5;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("FA2", "24K-28K CBS RAM Plus") {
		@Override
		public Cartridge create(byte[] content, String contentName) {
			return new Cartridge24K_28K_FA2(content, contentName);
		}
		@Override
		public CartridgeFormatOption getOption(byte content[], String contentName) {
			if (content.length != SIZE24K && content.length != SIZE28K) return null;
			return new CartridgeFormatOptionHinted(101, FORMAT, contentName);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}

