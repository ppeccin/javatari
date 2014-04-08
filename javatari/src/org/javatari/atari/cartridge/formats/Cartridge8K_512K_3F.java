// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.cartridge.ROM;

/**
 * Implements the 8K-512K "Enhanced 3F" Tigervision format
 */
public class Cartridge8K_512K_3F extends CartridgeBankedByBusMonitoring {

	protected Cartridge8K_512K_3F(ROM rom, CartridgeFormat format) {
		super(rom, format);
		selectableSliceMaxBank = (bytes.length - BANK_SIZE) / BANK_SIZE - 1;
		fixedSliceAddressOffset = bytes.length - BANK_SIZE * 2;
	}

	@Override
	public byte readByte(int address) {		
		maskAddress(address);
		if (maskedAddress >= FIXED_SLICE_START_ADDRESS)			// Fixed slice
			return bytes[fixedSliceAddressOffset + maskedAddress];	
		else
			return bytes[bankAddressOffset + maskedAddress];	// Selectable slice	
	}

	@Override
	public void monitorBusBeforeRead(int address, byte data) {
		// Nothing
	}

	@Override
	public void monitorBusBeforeWrite(int address, byte data) {
		// Perform bank switching as needed
		if (address <= 0x003f) {
			int bank = data & 0xff;		// unsigned
			if (bank <= selectableSliceMaxBank)
				bankAddressOffset = bank * BANK_SIZE;
		}
	}

	@Override
	protected void performBankSwitchOnMonitoredAccess(int address) {
		// Bank switching is done only on monitored writes
	}

	protected final int selectableSliceMaxBank; 
	protected final int fixedSliceAddressOffset;		// This slice is fixed at the last bank 


	protected static final int BANK_SIZE = 2048;
	protected static final int MIN_SIZE = 4 * BANK_SIZE;
	protected static final int MAX_SIZE = 256 * BANK_SIZE;
	protected static final int FIXED_SLICE_START_ADDRESS = 2048;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("3F", "8K-512K Tigervision") {
		@Override
		public Cartridge createCartridge(ROM rom) {
			return new Cartridge8K_512K_3F(rom, this);
		}
		@Override
		public CartridgeFormatOption getOption(ROM rom) {
			if (rom.content.length % BANK_SIZE != 0 || rom.content.length < MIN_SIZE || rom.content.length > MAX_SIZE) return null;
			return new CartridgeFormatOption(112, this, rom);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}