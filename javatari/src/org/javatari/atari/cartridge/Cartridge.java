// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge;

import java.io.Serializable;
import java.util.Map;

import org.javatari.atari.board.BUS;
import org.javatari.atari.console.savestate.SaveStateSocket;
import org.javatari.atari.controls.ConsoleControls.Control;
import org.javatari.atari.controls.ConsoleControlsInput;
import org.javatari.general.board.BUS16Bits;
import org.javatari.general.board.ClockDriven;


public abstract class Cartridge implements BUS16Bits, ClockDriven, Cloneable, Serializable, ConsoleControlsInput {

	protected Cartridge(ROM rom, CartridgeFormat format) {
		this.rom = rom;
		this.bytes = rom.content;	// uses the content of the ROM directly
		this.format = format;
	}

	public ROM rom() {
		return rom;
	}

	public CartridgeFormat format() {
		return format;
	}
	
	public CartridgeInfo getInfo() {
		return rom.info;
	}

	public void connectBus(BUS bus) {
		// Nothing
	}

	public void connectSaveStateSocket(SaveStateSocket socket) {
		// Nothing
	}

	public void powerOn() {
		// Nothing
	}
	
	public void powerOff() {
		// Nothing
	}

	public Cartridge saveState() {
		return this.clone();
	}

	@Override
	public void clockPulse() {
		// Nothing
	}

	@Override
	public byte readByte(int address) {
		maskAddress(address);
		return bytes[maskedAddress];	
	}

	@Override
	public void writeByte(int address, byte b) {	
		maskAddress(address);
		// Writing to ROMs is possible, but nothing is changed
	}

	public void monitorBusBeforeRead(int address, byte data) {
		// Nothing
	}

	public void monitorBusBeforeWrite(int address, byte data) {
		// Nothing
	}

	public boolean needsClock() {
		return false;
	}

	public boolean needsBusMonitoring() {
		return false;
	}

	protected void maskAddress(int address) {
		maskedAddress = address & ADDRESS_MASK;
	}

	@Override
	public Cartridge clone() {
		try { 
			return (Cartridge)super.clone(); 
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public void controlStateChanged(Control control, boolean state) {
		// Nothing
	}

	@Override
	public void controlStateChanged(Control control, int position) {
		// Nothing
	}

	@Override
	public void controlsStateReport(Map<Control, Boolean> report) {
		// Nothing
	}
	

	protected final ROM rom;
	protected byte[] bytes;		// for fast access to ROM content
	private final CartridgeFormat format;
	
	protected int maskedAddress;


	public static final int CHIP_MASK = 0x1000;
	public static final int CHIP_SELECT = 0x1000;

	private static final int ADDRESS_MASK = 0x0fff;

	public static final long serialVersionUID = 2L;		// Embedded ROM version

}
