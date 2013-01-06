// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge.formats;

import atari.cartridge.Cartridge;
import atari.cartridge.CartridgeFormat;
import atari.cartridge.CartridgeFormatOption;

/**
 * Implements the 8K-512K "SB" Superbanking format
 */
public class Cartridge8K_512K_SB extends CartridgeBankedByBusMonitoring {

	protected Cartridge8K_512K_SB(byte[] content, String contentName, CartridgeFormat format) {
		super(content, contentName, format);
		maxBank = content.length / BANK_SIZE - 1;
	}

	@Override
	protected void performBankSwitchOnMonitoredAccess(int address) {
		// Perform bank switching as needed
		if ((address & 0x1800) != 0x0800) return;
		int bank = address & 0x007f;
		if (bank > maxBank) return;
		bankAddressOffset = bank * BANK_SIZE;
	}
	

	private final int maxBank;

	protected static final int BANK_SIZE = 4096;
	protected static final int MIN_SIZE = 2 * BANK_SIZE;
	protected static final int MAX_SIZE = 64 * BANK_SIZE;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("SB", "8K-512K Superbanking") {
		@Override
		public Cartridge create(byte[] content, String contentName) {
			return new Cartridge8K_512K_SB(content, contentName, FORMAT);
		}
		@Override
		public CartridgeFormatOption getOption(byte content[], String contentName) {
			if (content.length % BANK_SIZE != 0 || content.length < MIN_SIZE || content.length > MAX_SIZE) return null;
			return new CartridgeFormatOptionHinted(113, FORMAT, contentName);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}