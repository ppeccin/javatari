// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge;

/**
 * Implements the 16K "F6" bank switching method
 */
public final class Cartridge16K extends CartridgeBanked {

	public Cartridge16K(byte[] content, Boolean superChip) {
		super(superChip);
		if (content.length != SIZE)
			throw new IllegalStateException("Invalid size for " + this.getClass().getName() + ": " + content.length);
		setContent(content);
	}

	@Override
	protected int maskAddress(int address) {
		// Check and perform bank-switch as necessary
		int add = super.maskAddress(address);
		switch (add) {
			case 0x0ff6:	// bank 0 selection
				bankAddressOffset = 0;
				break;
			case 0x0ff7:	// bank 1 selection
				bankAddressOffset = 4096;
				break;
			case 0x0ff8:	// bank 2 selection
				bankAddressOffset = 8192;
				break;
			case 0x0ff9:	// bank 3 selection
				bankAddressOffset = 12288;
				break;
		}
		return add;
	}

	public static final int SIZE = 16384;

	private static final long serialVersionUID = 1L;

}

