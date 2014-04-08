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
		data = 0;
		if (cartridge == null) {
			tia.videoOutput().showOSD("NO CARTRIDGE INSERTED!", true);
			// Data in the bus comes random at powerOn if no Cartridge is present
			data = (byte)Randomizer.instance.nextInt(256);
		}
		// Power on devices connected to the BUS
		ram.powerOn();
		pia.powerOn();
		tia.powerOn();
		if (cartridge != null) cartridge.powerOn();
	}

	public void powerOff() {
		// Power off devices connected to the BUS
		if (cartridge != null) cartridge.powerOff();
		tia.powerOff();
		pia.powerOff();
		ram.powerOff();
	}

	@Override
	public void clockPulse() {
		pia.clockPulse();
		cpu.clockPulse();
		if (cartridgeNeedsClock) cartridge.clockPulse();
	}
	
	@Override
	public byte readByte(int address) {
		// CART Bus monitoring
		if (cartridgeNeedsBusMonitoring) cartridge.monitorBusBeforeRead(address, data);

		if ((address & Cartridge.CHIP_MASK) == Cartridge.CHIP_SELECT) {		// CART selected?
			if (cartridge != null) data = cartridge.readByte(address);
		} else if ((address & RAM.CHIP_MASK) == RAM.CHIP_SELECT)			// RAM selected?
			data = ram.readByte(address);
		else if ((address & PIA.CHIP_MASK) == PIA.CHIP_SELECT)				// PIA selected?
			data = pia.readByte(address);
		else																// TIA selected...
			// Only bit 7 and 6 are connected to TIA read registers.
			if (DATA_RETENTION)
				data = (byte)(data & 0x3f | tia.readByte(address));		// Use the retained data for bits 5-0
			else
				data = tia.readByte(address);							// As if all bits were provided by TIA

		return data;
	}

	@Override
	public void writeByte(int address, byte b) {
		// CART Bus monitoring
		if (cartridgeNeedsBusMonitoring) cartridge.monitorBusBeforeWrite(address, b);

		data = b;
		
		if	((address & TIA.CHIP_MASK) == TIA.CHIP_SELECT) tia.writeByte(address, b);		// TIA selected?
		else if ((address & RAM.CHIP_MASK) == RAM.CHIP_SELECT) ram.writeByte(address, b);	// RAM selected?
		else if	((address & PIA.CHIP_MASK) == PIA.CHIP_SELECT) pia.writeByte(address, b);	// PIA selected?
		else 																				// CART selected...
			if	(cartridge != null) cartridge.writeByte(address, b);				
	}

	public void cartridge(Cartridge cartridge) {
		this.cartridge = cartridge;
		if (cartridge != null) {
			data = 0;
			cartridge.connectBus(this);
			cartridgeNeedsClock = cartridge.needsClock();
			cartridgeNeedsBusMonitoring = cartridge.needsBusMonitoring();
		} else {
			cartridgeNeedsClock = false;
			cartridgeNeedsBusMonitoring = false;
		}
	}


	public Cartridge cartridge;
	public final M6502 cpu;
	public final RAM ram;
	public final TIA tia;
	public final PIA pia;

	private byte data = 0;
	private boolean cartridgeNeedsClock = false;
	private boolean cartridgeNeedsBusMonitoring = false;


	private static final boolean DATA_RETENTION = Parameters.BUS_DATA_RETENTION;

}
