// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge.formats;

import atari.cartridge.Cartridge;
import atari.cartridge.CartridgeFormat;

/**
 * Implements the generic bank switching method with address offset
 * Used by several n * xK bank formats
 */
public abstract class CartridgeBanked extends Cartridge {

	protected CartridgeBanked(byte[] content, String contentName, CartridgeFormat format) {
		super(content, contentName, format);
	}

	@Override
	public byte readByte(int address) {		
		maskAddress(address);
		// Always add the correct offset to access bank selected
		return bytes[bankAddressOffset + maskedAddress];	
	}

	@Override
	public void maskAddress(int address) {
		super.maskAddress(address);
		// Perform bank switching as needed 
		performBankSwitch();
	}
		
	protected abstract void performBankSwitch();


	protected int bankAddressOffset = 0;

	
	public static final long serialVersionUID = 1L;

}