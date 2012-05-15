// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge.bankswitching;

/**
 * Implements the 12K "FA" bank switching method
 */
public final class Cartridge12K extends CartridgeBanked {

	public Cartridge12K(byte[] content) {
		super(true, 256);
		if (content.length != SIZE)
			throw new IllegalStateException("Invalid size for " + this.getClass().getName() + ": " + content.length);
		setContent(content);
	}

	@Override
	protected int maskAddress(int address) {
		// Check and perform bank-switch as necessary
		int add = super.maskAddress(address);
		switch (add) {
			case 0x0ff8:	// bank 0 selection
				bankAddressOffset = 0;
				break;
			case 0x0ff9:	// bank 1 selection
				bankAddressOffset = 4096;
				break;
			case 0x0ffa:	// bank 2 selection
				bankAddressOffset = 8192;
		}
		return add;
	}

	public static final int SIZE = 12288;

	public static final long serialVersionUID = 1L;

}

