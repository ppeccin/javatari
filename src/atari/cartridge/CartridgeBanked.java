// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge;

/**
 * Implements the generic bank switching method, with or without SuperChip (extra RAM). Used by several n * 4K bank formats.
 */
public abstract class CartridgeBanked extends Cartridge {

	protected CartridgeBanked(Boolean superChip) {
		super();
		// SuperChip mode. null = automatic mode
		if (superChip == null) { 
			superChipMode = false;
			superChipAutoDetect = true;
		} else {
			superChipMode = superChip;
			superChipAutoDetect = false;
		}
	}

	@Override
	public byte readByte(int address) {		
		// Masking address will perform bank-switching as needed
		int addr = maskAddress(address);
		// Check for Extra RAM reads
		if (superChipMode && (addr >= 0x80) && (addr <= 0xff))
			return extraRAM[addr - 0x80];
		else
			// Always add the correct offset to access bank selected
			return bytes[addr + bankAddressOffset];	
	}

	@Override
	public void writeByte(int address, byte b) {	
		// Masking address will perform bank-switching as needed
		int addr = maskAddress(address);
		// Check for Extra RAM writes and then turn superChip mode on
		if (addr <= 0x7f && (superChipMode || superChipAutoDetect)) {
			if (!superChipMode) superChipMode = true;	// Turn SuperChip mode from now on
			extraRAM[addr] = b;
		}
	}

	protected int bankAddressOffset = 0;
	
	private boolean superChipMode = false;
	private boolean superChipAutoDetect = false;
	private final byte[] extraRAM = new byte[128];

	private static final long serialVersionUID = 1L;

}