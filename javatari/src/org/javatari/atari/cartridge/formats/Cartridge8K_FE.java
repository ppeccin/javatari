// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;

/**
 * Implements the 8K "FE" Robotank/Decathlon format
 */
public final class Cartridge8K_FE extends CartridgeBanked {

	private Cartridge8K_FE(byte[] content, String contentName) {
		super(content, contentName, FORMAT);
		bankAddressOffset = BANK_SIZE;
	}

	@Override
	public void maskAddress(int address) {
		// Bankswitching: Look at the address to determine the correct bank to be
		if ((address & 0x2000) != 0) {		// Check bit 13. Address is like Fxxx or Dxxx?
			if (bankAddressOffset != 0) bankAddressOffset = 0;
		} else {
			if (bankAddressOffset != BANK_SIZE) bankAddressOffset = BANK_SIZE;
		}
		super.maskAddress(address);
	}

	@Override
	protected void performBankSwitchOnMaskedAddress() {
		// Bank switching is not done within masked address range
		// Its done directly before masking address
	}

	
	private static final int SIZE = 8192;
	private static final int BANK_SIZE = 4096;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("FE", "8K Robotank/Decathlon") {
		@Override
		public Cartridge create(byte[] content, String contentName) {
			return new Cartridge8K_FE(content, contentName);
		}
		@Override
		public CartridgeFormatOption getOption(byte content[], String contentName) {
			if (content.length != SIZE) return null;
			return new CartridgeFormatOptionHinted(103, this, contentName);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}


/*

 OLD implementation via stack access monitoring. Bugged.

private int lastRead = 0;
private int lastWrite = 0;
private boolean switchTriggered = false;

@Override
public void monitorByteRead(int address, byte data) {
	if (switchTriggered) {
		performBankSwitch(data);
		return;
	} else {
		if (lastWrite != 0) lastWrite = 0;
		// If out of switching range (stack), reset
		if (address < 0x0100 || address > 0x01ff) {
			if (lastRead != 0) lastRead = 0;
			return;
		}
		// We need to detect 2 consecutive reads
		// If this is the first access, save it 
		if (lastRead == 0) {
			lastRead = address;
			return;
		}
		// Second consecutive read. We found it!
		performBankSwitch(data);	// Use value from this read
	}
}

@Override
public void monitorByteWritten(int address, byte data) {
	if (lastRead != 0) lastRead = 0;
	// If out of switching range (stack), reset
	if (address < 0x0100 || address > 0x01ff) {
		if (lastWrite != 0) lastWrite = 0;
		return;
	}
	// We need to detect 2 consecutive writes
	// If this is the first access, save it 
	if (lastWrite == 0) {
		lastWrite = address;
		return;
	}
	// Second consecutive write. We found it!
	switchTriggered = true;			// Will use value from next read
}

private void performBankSwitch(byte data) {
	switchTriggered = false;
	lastRead = lastWrite = 0;
	if ((data & 0x20) != 0)		// Check bit 5
		bankAddressOffset = 0;
	else
		bankAddressOffset = BANK_SIZE;
}

*/