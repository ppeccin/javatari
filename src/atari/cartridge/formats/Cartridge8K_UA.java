// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge.formats;

import atari.cartridge.Cartridge;
import atari.cartridge.CartridgeFormat;
import atari.cartridge.CartridgeFormatOption;

/**
 * Implements the 8K "UA" UA Limited format
 */
public class Cartridge8K_UA extends CartridgeBankedByBusMonitoring {

	protected Cartridge8K_UA(byte[] content, String contentName) {
		super(content, contentName, FORMAT);
	}

	@Override
	protected void performBankSwitchOnMonitoredAccess(int address) {
		if (address == 0x0220) {
			if (bankAddressOffset != 0) bankAddressOffset = 0;
		} else if (address == 0x0240) {
			if (bankAddressOffset != BANK_SIZE) bankAddressOffset = BANK_SIZE;
		}
	}
	

	protected static final int BANK_SIZE = 4096;
	protected static final int SIZE = 2 * BANK_SIZE;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("UA", "8K UA Limited") {
		@Override
		public Cartridge create(byte[] content, String contentName) {
			return new Cartridge8K_UA(content, contentName);
		}
		@Override
		public CartridgeFormatOption getOption(byte content[], String contentName) {
			if (content.length != SIZE) return null;
			return new CartridgeFormatOptionHinted(115, this, contentName);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}