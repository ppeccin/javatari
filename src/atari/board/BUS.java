// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.board;

import parameters.Parameters;
import utils.Randomizer;
import general.board.BUS16Bits;
import general.m6502.M6502;
import atari.cartridge.Cartridge;
import atari.pia.PIA;
import atari.pia.RAM;
import atari.tia.TIA;

public final class BUS implements BUS16Bits {

	public BUS(M6502 cpu, TIA tia, PIA pia, RAM ram) {
		cpu.connectBus(this);
		this.ram = ram;
		this.tia = tia;
		this.pia = pia;
	}

	public void powerOn() {
		// Data in the bus come random at powerOn
		if (cartridge == null) data = (byte)Randomizer.instance.nextInt(256);
		else data = 0;
	}

	public void powerOff() {
	}

	@Override
	public byte readByte(int address) {
		if ((address & CART_MASK) == CART_SEL) {					// CART selected?
			if (cartridge != null) data = cartridge.readByte(address);
		} else if ((address & RAM_MASK) == RAM_SEL)					// RAM selected?
			data = ram.readByte(address);
		else if ((address & PIA_MASK) == PIA_SEL)					// PIA selected?
			data = pia.readByte(address);
		else														// TIA selected...
			// Only bit 7 and 6 are connected to TIA read registers.
			if (DATA_RETENTION)
				data = (byte)(data & 0x3f | tia.readByte(address));		// Use the retained data for bits 5-0
			else
				data = tia.readByte(address);							// As if all bits were provided by TIA

		// CART Bus monitoring
		if (cartridge != null)	cartridge.monitorByteRead(address, data);

		return data;
	}

	@Override
	public void writeByte(int address, byte b) {
		data = b;
		
		if ((address & RAM_MASK) == RAM_SEL) ram.writeByte(address, b);	// RAM selected?
		else if	((address & TIA_MASK) == TIA_SEL) tia.writeByte(address, b);	// TIA selected?
		else if	((address & PIA_MASK) == PIA_SEL) pia.writeByte(address, b);	// PIA selected?
		else 																	// CART selected...
			if	(cartridge != null) cartridge.writeByte(address, b);				

		// CART Bus monitoring
		if (cartridge != null) cartridge.monitorByteWritten(address, b);
	}

	public void cartridge(Cartridge cartridge) {
		this.cartridge = cartridge;
	}


	public Cartridge cartridge;
	public final RAM ram;
	public final TIA tia;
	public final PIA pia;

	private byte data = 0;

	private static final int CART_MASK = 0x1000;
	private static final int CART_SEL = 0x1000;
	private static final int RAM_MASK = 0x1280;
	private static final int RAM_SEL = 0x0080;
	private static final int TIA_MASK = 0x1080;
	private static final int TIA_SEL = 0x0000;
	private static final int PIA_MASK = 0x1280;
	private static final int PIA_SEL = 0x0280;

	private static final boolean DATA_RETENTION = Parameters.BUS_DATA_RETENTION;

}
