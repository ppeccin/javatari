// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;

/**
 * Implements the 8K-64K "EF" H. Runner (+RAM) format
 */
public final class Cartridge8K_64K_EF extends CartridgeBankedByMaskedRange {

	private Cartridge8K_64K_EF(byte[] content, String contentName) {
		super(content, contentName, FORMAT, BASE_BANKSW_ADDRESS, null, 128);		// 128 RAM if SC mode ON
	}


	private static final int MIN_SIZE = 8192;
	private static final int MAX_SIZE = 65536;
	private static final int BANK_SIZE = 4096;
	private static final int BASE_BANKSW_ADDRESS = 0x0fe0;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("EF", "8K-64K H. Runner (+RAM)") {
		@Override
		public Cartridge create(byte[] content, String contentName) {
			return new Cartridge8K_64K_EF(content, contentName);
		}
		@Override
		public CartridgeFormatOption getOption(byte content[], String contentName) {
			if (content.length % BANK_SIZE != 0 || content.length < MIN_SIZE || content.length > MAX_SIZE) return null;
			return new CartridgeFormatOptionHinted(114, this, contentName);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}

