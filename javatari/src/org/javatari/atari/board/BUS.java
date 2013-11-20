// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.board;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.pia.PIA;
import org.javatari.atari.pia.RAM;
import org.javatari.atari.tia.TIA;
import org.javatari.general.board.BUS16Bits;
import org.javatari.general.board.ClockDriven;
import org.javatari.general.m6502.M6502;
import org.javatari.parameters.Parameters;
import org.javatari.utils.Randomizer;


public final class BUS implements BUS16Bits, ClockDriven {

	public BUS(M6502 cpu, TIA tia, PIA pia, RAM ram) {
		this.cpu = cpu;
		this.ram = ram;
		this.tia = tia;
		this.pia = pia;
		cpu.connectBus(this);
		tia.connectBus(this);
		pia.connectBus(this);
	}

	public void powerOn() {
		// Data in the bus come random at powerOn
		// TODO This is a source of indeterminism. Potential problem in multiplayer sync
		if (cartridge == null) data = (byte)Randomizer.instance.nextInt(256);
		else data = 0;
	}

	public void powerOff() {
		// Nothing
	}

	@Override
	public void clockPulse() {
		pia.clockPulse();
		cpu.clockPulse();
		if (cartridgeNeedsClock) cartridge.clockPulse();
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
		if (cartridgeNeedsBusMonitoring) cartridge.monitorByteRead(address, data);

		return data;
	}

	@Override
	public void writeByte(int address, byte b) {
		data = b;
		
		if ((address & RAM_MASK) == RAM_SEL) ram.writeByte(address, b);			// RAM selected?
		else if	((address & TIA_MASK) == TIA_SEL) tia.writeByte(address, b);	// TIA selected?
		else if	((address & PIA_MASK) == PIA_SEL) pia.writeByte(address, b);	// PIA selected?
		else 																	// CART selected...
			if	(cartridge != null) cartridge.writeByte(address, b);				

		// CART Bus monitoring
		if (cartridgeNeedsBusMonitoring) cartridge.monitorByteWritten(address, b);
	}

	public void cartridge(Cartridge cartridge) {
		this.cartridge = cartridge;
		if (cartridge != null) cartridge.connectBus(this);
		cartridgeNeedsClock = cartridge == null ? false : cartridge.needsClock();
		cartridgeNeedsBusMonitoring = cartridge == null ? false : cartridge.needsBusMonitoring();
	}


	public Cartridge cartridge;
	public final M6502 cpu;
	public final RAM ram;
	public final TIA tia;
	public final PIA pia;

	private byte data = 0;
	private boolean cartridgeNeedsClock = false;
	private boolean cartridgeNeedsBusMonitoring = false;

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
