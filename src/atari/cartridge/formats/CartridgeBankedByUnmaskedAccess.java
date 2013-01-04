// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge.formats;

import atari.cartridge.CartridgeFormat;

/**
 * Implements generic bank switching using unmasked address access via bus monitoring (outside Cart area)
 */
public abstract class CartridgeBankedByUnmaskedAccess extends CartridgeBanked {

	protected CartridgeBankedByUnmaskedAccess(byte[] content, String contentName, CartridgeFormat format) {
		super(content, contentName, format);
	}

	@Override
	public void monitorByteRead(int address, byte data) {
		performBankSwitch(address);
	}

	@Override
	public void monitorByteWritten(int address, byte data) {
		performBankSwitch(address);
	}
	
	@Override
	protected void performBankSwitch() {
		// Bank switching is not done within masked address range
		// Its done directly via bus monitoring
	}

	protected abstract void performBankSwitch(int address);

	
	public static final long serialVersionUID = 1L;

}