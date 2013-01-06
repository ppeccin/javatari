// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge.formats;

import atari.cartridge.Cartridge;
import atari.cartridge.CartridgeFormat;
import atari.cartridge.CartridgeFormatOption;

/**
 * Implements the 8K "0840" Econobanking format
 */
public class Cartridge8K_0840 extends CartridgeBankedByBusMonitoring {

	protected Cartridge8K_0840(byte[] content, String contentName, CartridgeFormat format) {
		super(content, contentName, format);
	}

	@Override
	protected void performBankSwitchOnMonitoredAccess(int address) {
		int addrBank = address & 0x1840;
		if (addrBank == 0x0800) {
			if (bankAddressOffset != 0) bankAddressOffset = 0;
		} else if (addrBank == 0x0840) {
			if (bankAddressOffset != BANK_SIZE) bankAddressOffset = BANK_SIZE;
		}
	}
	

	protected static final int BANK_SIZE = 4096;
	protected static final int SIZE = 2 * BANK_SIZE;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("0840", "8K Econobanking") {
		@Override
		public Cartridge create(byte[] content, String contentName) {
			return new Cartridge8K_0840(content, contentName, FORMAT);
		}
		@Override
		public CartridgeFormatOption getOption(byte content[], String contentName) {
			if (content.length != SIZE) return null;
			return new CartridgeFormatOptionHinted(116, FORMAT, contentName);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}