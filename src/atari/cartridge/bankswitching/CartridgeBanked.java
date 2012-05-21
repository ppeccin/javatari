// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge.bankswitching;

import java.util.Arrays;

import atari.cartridge.Cartridge;

/**
 * Implements the generic bank switching method, with or without SuperChip (extra RAM). 
 * Used by several n * 4K bank formats with varying extra RAM sizes
 */
public abstract class CartridgeBanked extends Cartridge {

	protected CartridgeBanked(byte[] content, Boolean superChip, int extraRAMSize) {
		super();
		setContent(content);
		this.extraRAMSize = extraRAMSize;
		// SuperChip mode. null = automatic mode
		if (superChip == null) { 
			superChipMode = false;
			superChipAutoDetect = true;
		} else {
			superChipMode = superChip;
			superChipAutoDetect = false;
		}
		extraRAM = (superChip == null || superChip) ? Arrays.copyOf(bytes, extraRAMSize) : null;
	}

	@Override
	public byte readByte(int address) {		
		// Masking address will perform bank-switching as needed
		int addr = maskAddress(address);
		// Check for SuperChip Extra RAM reads
		if (superChipMode && (addr >= extraRAMSize) && (addr < extraRAMSize * 2))
			return extraRAM[addr - extraRAMSize];
		else
			// Always add the correct offset to access bank selected
			return bytes[addr + bankAddressOffset];	
	}

	@Override
	public void writeByte(int address, byte b) {	
		// Masking address will perform bank-switching as needed
		int addr = maskAddress(address);
		// Check for Extra RAM writes and then turn superChip mode on
		if (addr < extraRAMSize && (superChipMode || superChipAutoDetect)) {
			if (!superChipMode) {
				System.out.println(">>>> SuperChip ON");
				superChipMode = true;
			}
			extraRAM[addr] = b;
		}
	}

	@Override
	public CartridgeBanked clone() {
		CartridgeBanked clone = (CartridgeBanked)super.clone();
		if (extraRAM != null) clone.extraRAM = extraRAM.clone();
		return clone;
	}

	protected int bankAddressOffset = 0;
	private boolean superChipMode = false;
	private final boolean superChipAutoDetect;
	private final int extraRAMSize;
	private byte[] extraRAM;

	public static final long serialVersionUID = 1L;

}