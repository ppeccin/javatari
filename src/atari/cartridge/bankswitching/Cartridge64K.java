// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge.bankswitching;

/**
 * Implements the 64K bank switching method with SuperChip support
 */
public final class Cartridge64K extends CartridgeBanked {

	public Cartridge64K(byte[] content, Boolean superChip) {
		super(content, superChip, 128);
	}

	@Override
	protected int maskAddress(int address) {
		// Check and perform bank-switch as necessary
		int add = super.maskAddress(address);
		switch (add) {
			case 0x0ff0:	// bank 0 selection
				bankAddressOffset = 0;
				break;
			case 0x0ff1:	// bank 1 selection
				bankAddressOffset = 4096;
				break;
			case 0x0ff2:	// bank 2 selection
				bankAddressOffset = 8192;
				break;
			case 0x0ff3:	// bank 3 selection
				bankAddressOffset = 12288;
				break;
			case 0x0ff4:	// bank 4 selection
				bankAddressOffset = 16384;
				break;
			case 0x0ff5:	// bank 5 selection
				bankAddressOffset = 16384 + 4096;
				break;
			case 0x0ff6:	// bank 6 selection
				bankAddressOffset = 16384 + 8192;
				break;
			case 0x0ff7:	// bank 7 selection
				bankAddressOffset = 16384 + 12288;
				break;
			case 0x0ff8:	// bank 8 selection
				bankAddressOffset = 32768;
				break;
			case 0x0ff9:	// bank 9 selection
				bankAddressOffset = 32768 + 4096;
				break;
			case 0x0ffa:	// bank 10 selection
				bankAddressOffset = 32768 + 8192;
				break;
			case 0x0ffb:	// bank 11 selection
				bankAddressOffset = 32768 + 12288;
				break;
			case 0x0ffc:	// bank 12 selection
				bankAddressOffset = 32768 + 16384;
				break;
			case 0x0ffd:	// bank 13 selection
				bankAddressOffset = 32768 + 16384 + 4096;
				break;
			case 0x0ffe:	// bank 14 selection
				bankAddressOffset = 32768 + 16384 + 8192;
				break;
			case 0x0fff:	// bank 15 selection
				bankAddressOffset = 32768 + 16384 + 12288;
		}
		return add;
	}

	
	public static boolean accepts(byte[] content, Boolean superChip, boolean sliced) {
		return content.length == SIZE && !sliced;
	}

	public static final int SIZE = 65536;

	public static final long serialVersionUID = 1L;

}

