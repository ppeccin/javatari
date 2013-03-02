// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;


/**
 * Implements the 4K unbanked format. Smaller ROMs will be copied multiple times to fill the entire 4K
 */
public final class Cartridge4K extends Cartridge {

	private Cartridge4K(byte[] content, String contentName) {
		super(new byte[MAX_SIZE], contentName, FORMAT);
		int len = content.length;
		for (int pos = 0; pos < MAX_SIZE; pos += len)
			System.arraycopy(content, 0, bytes, pos, len);
	}


	private static final int MIN_SIZE = 8;
	private static final int MAX_SIZE = 4096;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("4K", "4K Atari") {
		@Override
		public Cartridge create(byte[] content, String contentName) {
			return new Cartridge4K(content, contentName);
		}
		@Override
		public CartridgeFormatOption getOption(byte content[], String contentName) {
			if (content.length < MIN_SIZE || content.length > MAX_SIZE || MAX_SIZE % content.length != 0) return null;
			return new CartridgeFormatOptionHinted(101, this, contentName);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}

