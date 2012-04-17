// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge;

public abstract class CartridgeBanked extends Cartridge {

	protected CartridgeBanked(int size) {
		super(size);
	}

	public byte readByte(int address) {		
		// Always add the correct offset to access bank selected
		return bytes[maskAddress(address) + bankAddressOffset];	
	}

	public void writeByte(int address, byte b) {	
		// Writing to roms is possible, but nothing is changed. 
		// Mask address anyway to perform bank switching as needed
		maskAddress(address);
	}

	protected int bankAddressOffset = 0;

	private static final long serialVersionUID = 1L;

}