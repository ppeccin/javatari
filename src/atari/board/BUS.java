// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.board;

import general.board.BUS16Bits;
import atari.cartridge.Cartridge;
import atari.pia.PIA;
import atari.pia.RAM;
import atari.tia.TIA;

public final class BUS implements BUS16Bits {

	public BUS(TIA tia, PIA pia, RAM ram) {
		this.ram = ram;
		this.tia = tia;
		this.pia = pia;
	}

	@Override
	public byte readByte(int address) {
		switch (selectDevice(address)) {
			case CART:
				return cartridge.readByte(address);
			case RAM:
				return ram.readByte(address);
			case TIA:
				return tia.readByte(address);
			case PIA:
				return pia.readByte(address);
		}
		throw new IllegalStateException();
	}

	@Override
	public int unsignedByte(int address) {
		switch (selectDevice(address)) {
			case CART:
				return cartridge.unsignedByte(address);
			case RAM:
				return ram.unsignedByte(address);
			case TIA:
				return tia.unsignedByte(address);
			case PIA:
				return pia.unsignedByte(address);
		}
		throw new IllegalStateException();
	}
	
	@Override
	public void writeByte(int address, byte b) {
		switch (selectDevice(address)) {
			case CART:
				cartridge.writeByte(address, b); return;
			case RAM:
				ram.writeByte(address, b); return;
			case TIA:
				tia.writeByte(address, b); return;
			case PIA:
				pia.writeByte(address, b); return;
		}
		throw new IllegalStateException();
	}

	public void cartridge(Cartridge cartridge) {
		this.cartridge = cartridge;
	}

	private int selectDevice(int address) {
		if ((address & CART_MASK) == CART_SEL)
			return CART;
		if ((address & RAM_MASK) == RAM_SEL)
			return RAM;
		if ((address & TIA_MASK) == TIA_SEL)
			return TIA;
		if ((address & PIA_MASK) == PIA_SEL)
			return PIA;
		throw new UnsupportedOperationException(String.format("Address not mapped: $%04x", address));
	}

	public Cartridge cartridge;
	public final RAM ram;
	public final TIA tia;
	public final PIA pia;
	
	private static final int CART = 0;
	private static final int CART_MASK = 0x1000;
	private static final int CART_SEL = 0x1000;
	private static final int RAM = 1;
	private static final int RAM_MASK = 0x1280;
	private static final int RAM_SEL = 0x0080;
	private static final int TIA = 2;
	private static final int TIA_MASK = 0x1080;
	private static final int TIA_SEL = 0x0000;
	private static final int PIA = 3;
	private static final int PIA_MASK = 0x1280;
	private static final int PIA_SEL = 0x0280;

}
