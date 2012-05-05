// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge;

/**
 * Implements the 28K "FA2" bank switching method
 */
public final class Cartridge28K extends CartridgeBanked {

	public Cartridge28K(byte[] content) {
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
			case 0x0ff5:	// bank 0 selection
				bankAddressOffset = 0;
				break;
			case 0x0ff6:	// bank 1 selection
				bankAddressOffset = 4096;
				break;
			case 0x0ff7:	// bank 2 selection
				bankAddressOffset = 8192;
				break;
			case 0x0ff8:	// bank 3 selection
				bankAddressOffset = 12288;
				break;
			case 0x0ff9:	// bank 4 selection
				bankAddressOffset = 16384;
				break;
			case 0x0ffa:	// bank 5 selection
				bankAddressOffset = 16384 + 4096;
				break;
			case 0x0ffb:	// bank 6 selection
				bankAddressOffset = 16384 + 8192;
				break;
		}
		return add;
	}

	public static final int SIZE = 32768 - 4096;

	public static final long serialVersionUID = 1L;

}

