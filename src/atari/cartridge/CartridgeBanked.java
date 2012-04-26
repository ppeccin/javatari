// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge;

/**
 * Implements the generic bank switching method, with or without SuperChip (extra RAM). Used by several n * 4K bank formats.
 */
public abstract class CartridgeBanked extends Cartridge {

	protected CartridgeBanked(boolean superChip) {
		super();
		this.superChip = superChip;
		if (this.superChip)
			extraRAM = new byte[128];
		else 
			extraRAM = null;
	}

	@Override
	public byte readByte(int address) {		
		// Masking address will perform bank-switching as needed
		int addr = maskAddress(address);
		// Check for an Extra RAM read
		if (superChip && (addr >= 0x80) && (addr <= 0xff))
			return extraRAM[addr - 0x80];
		// Always add the correct offset to access bank selected
		return bytes[addr + bankAddressOffset];	
	}

	@Override
	public void writeByte(int address, byte b) {	
		// Masking address will perform bank-switching as needed
		int addr = maskAddress(address);
		// Check for an Extra RAM write
		if (superChip && (addr <= 0x7f))
			extraRAM[addr] = b;
	}

	protected int bankAddressOffset = 0;
	
	private final boolean superChip;
	private final byte[] extraRAM;

	private static final long serialVersionUID = 1L;

}