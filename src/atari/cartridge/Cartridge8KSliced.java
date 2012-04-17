// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge;

public  class Cartridge8KSliced extends Cartridge {

	public Cartridge8KSliced(byte[] content) {
		super(SIZE);
		if (content.length != SIZE)
			throw new IllegalStateException("Invalid size for " + this.getClass().getName() + ": " + content.length);
		setContent(content);
	}

	public byte readByte(int address) {		
		// Always add the correct offset to access bank selected on the corresponding slice
		int add = maskAddress(address);
		if (add < 0x0400)		// Slice 0
			add += slice0AddressOffset;
		else if (add < 0x0800)		// Slice 1
				add += (slice1AddressOffset - 0x0400);
			else if (add < 0x0c00)		// Slice 2
					add += (slice2AddressOffset - 0x0800);
				else		// Slice 3 (0x0c00 - 0x0fff) is always at 0x1c00 (bank 7)
					add += 0x1000;
		return bytes[add];	
	}

	public void writeByte(int address, byte b) {	
		// Writing to roms is possible, but nothing is changed. 
		// Mask address anyway to perform bank switching as needed
		maskAddress(address);
	}

	@Override
	protected int maskAddress(int address) {
		// Check and perform bank-switch as necessary
		int add = super.maskAddress(address);
		switch (add) {
			// Each bank is 0x0400 bytes each, 0 to 7
			// Slice 0 bank selection
			case 0x0fe0: case 0x0fe1: case 0x0fe2: case 0x0fe3: case 0x0fe4: case 0x0fe5: case 0x0fe6: case 0x0fe7:	
				slice0AddressOffset = (add - 0x0fe0) * 0x0400;		
				break;
			// Slice 1 bank selection
			case 0x0fe8: case 0x0fe9: case 0x0fea: case 0x0feb: case 0x0fec: case 0x0fed: case 0x0fee: case 0x0fef:	
				slice1AddressOffset = (add - 0x0fe8) * 0x0400;		
				break;
			// Slice 2 bank selection
			case 0x0ff0: case 0x0ff1: case 0x0ff2: case 0x0ff3: case 0x0ff4: case 0x0ff5: case 0x0ff6: case 0x0ff7:	
				slice2AddressOffset = (add - 0x0ff0) * 0x0400;		
				break;
			// Slice 3 is fixed at bank 7
		}
		return add;
	}

	private int slice0AddressOffset = 0;
	private int slice1AddressOffset = 0;
	private int slice2AddressOffset = 0;
	// Slice 3 is fixed at bank 7

	public static final int SIZE = 8192;

	private static final long serialVersionUID = 1L;

}