// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge;


import java.io.Serializable;
import java.util.Map;

import org.javatari.atari.board.BUS;
import org.javatari.atari.controls.ConsoleControlsInput;
import org.javatari.atari.controls.ConsoleControls.Control;
import org.javatari.general.av.video.VideoStandard;
import org.javatari.general.board.BUS16Bits;
import org.javatari.general.board.ClockDriven;


public abstract class Cartridge implements BUS16Bits, ClockDriven, Cloneable, Serializable, ConsoleControlsInput {

	protected Cartridge(byte[] content, String contentName, CartridgeFormat format) {
		this.bytes = content;
		this.contentName = contentName;
		this.format = format;
	}
	
	public byte[] content() {
		return bytes;
	}

	public String contentName() {
		return contentName;
	}

	public CartridgeFormat format() {
		return format;
	}

	public void connectBus(BUS bus) {
		this.bus = bus;
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

	public void monitorByteRead(int address, byte data) {
		// Nothing
	}

	public void monitorByteWritten(int address, byte data) {
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

	public VideoStandard suggestedVideoStandard() {
		return suggestedVideoStandard;
	}
	
	public void suggestedVideoStandard(VideoStandard videoStandard) {
		this.suggestedVideoStandard = videoStandard;
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

	protected transient BUS bus;

	protected byte[] bytes;
	private final String contentName;
	private final CartridgeFormat format;

	protected int maskedAddress;

	private VideoStandard suggestedVideoStandard = null;


	private static final int ADDRESS_MASK = 0x0fff;
	
	public static final long serialVersionUID = 1L;

}
