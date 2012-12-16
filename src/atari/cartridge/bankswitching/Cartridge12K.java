// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge.bankswitching;

/**
 * Implements the 12K "FA" bank switching method
 */
public final class Cartridge12K extends CartridgeBanked {

	public Cartridge12K(byte[] content) {
		super(content, true, 256);			// SuperChip always ON
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


	public static boolean accepts(byte[] content, Boolean superChip, boolean sliced) {
		return content.length == SIZE && (superChip == null || superChip) && !sliced;
	}

	public static final int SIZE = 12288;

	public static final long serialVersionUID = 1L;

}

