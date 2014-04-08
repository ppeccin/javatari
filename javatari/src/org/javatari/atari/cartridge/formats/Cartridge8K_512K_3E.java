// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.cartridge.ROM;


/**
 * Implements the 8K-512K "3E" Tigervision (+RAM) format
 */
public final class Cartridge8K_512K_3E extends Cartridge8K_512K_3F {

	private Cartridge8K_512K_3E(ROM rom) {
		super(rom, FORMAT);
	}

	@Override
	public byte readByte(int address) {		
		maskAddress(address);
		if (maskedAddress >= FIXED_SLICE_START_ADDRESS)							// ROM Fixed Slice
			return bytes[fixedSliceAddressOffset + maskedAddress];	
		else
			if (extraRAMBankAddressOffset >= 0 && maskedAddress < 0x0400)		// RAM 
				return extraRAM[extraRAMBankAddressOffset + maskedAddress];
			else
				return bytes[bankAddressOffset + maskedAddress];				// ROM Selectable Slice	
	}

	@Override
	public void writeByte(int address, byte b) {
		// Check if Extra RAM bank is selected
		if (extraRAMBankAddressOffset < 0) return;
		
		maskAddress(address);
		// Check for Extra RAM writes
		if (maskedAddress >= 0x0400 && maskedAddress <= 0x07ff)
			extraRAM[extraRAMBankAddressOffset + maskedAddress - 0x0400] = b;
	}
		
	@Override
	public void monitorBusBeforeWrite(int address, byte data) {
		// Perform ROM bank switching as needed
		if (address == 0x003f) {
			int bank = data & 0xff;		// unsigned
			if (bank <= selectableSliceMaxBank) {
				bankAddressOffset = bank * BANK_SIZE;
				extraRAMBankAddressOffset = -1;
			}
			return;
		}
		// Perform RAM bank switching as needed
		if (address == 0x003e) {
			int bank = data & 0xff;		// unsigned
			adjustExtraRAMSize(bank);
			extraRAMBankAddressOffset = bank * EXTRA_RAM_BANK_SIZE;
		}
	}
	
	private void adjustExtraRAMSize(int bank) {
		if (extraRAM.length >= (bank + 1) * EXTRA_RAM_BANK_SIZE) return;
		
		// Increase extraRAM size to match bank
		byte[] newExtraRAM = new byte[(bank + 1) * EXTRA_RAM_BANK_SIZE];
		System.arraycopy(extraRAM, 0, newExtraRAM, 0, extraRAM.length);
		extraRAM = newExtraRAM;
	}

	@Override
	public Cartridge8K_512K_3E clone() {
		Cartridge8K_512K_3E clone = (Cartridge8K_512K_3E)super.clone();
		clone.extraRAM = extraRAM.clone();
		return clone;
	}

	
	private int extraRAMBankAddressOffset = -1;		// No Extra RAM bank selected 
	private byte[] extraRAM = new byte[0];


	private static final int EXTRA_RAM_BANK_SIZE = 1024;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("3E", "8K-512K Tigervision (+RAM)") {
		@Override
		public Cartridge createCartridge(ROM rom) {
			return new Cartridge8K_512K_3E(rom);
		}
		@Override
		public CartridgeFormatOption getOption(ROM rom) {
			if (rom.content.length % BANK_SIZE != 0 || rom.content.length < MIN_SIZE || rom.content.length > MAX_SIZE) return null;
			return new CartridgeFormatOption(111, this, rom);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}