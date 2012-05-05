// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge;

/**
 * Implements the 8K "F8" and "F8SC" bank switching method
 */
public final class Cartridge8K extends CartridgeBanked {

	public Cartridge8K(byte[] content, Boolean superChip) {
		super(superChip, 128);
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
		}
		return add;
	}

	public static final int SIZE = 8192;

	public static final long serialVersionUID = 1L;

}

