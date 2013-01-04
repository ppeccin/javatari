// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge;

import general.av.video.VideoStandard;
import general.board.BUS16Bits;

import java.io.Serializable;

public abstract class Cartridge implements BUS16Bits, Cloneable, Serializable {

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
	}

	public void monitorByteWritten(int address, byte data) {
	}

	protected void maskAddress(int address) {
		maskedAddress = address & 0x0fff;
	}

	public VideoStandard suggestedVideoStandard() {
		return suggestedVideoStandard;
	}
	
	public void suggestedVideoStandard(VideoStandard videoStandard) {
		this.suggestedVideoStandard = videoStandard;
	}
	
	@Override
	public Cartridge clone() {
		try { return (Cartridge)super.clone(); } catch (CloneNotSupportedException e) {}
		return null;
	}


	protected byte[] bytes;
	private final String contentName;
	private final CartridgeFormat format;

	protected int maskedAddress;

	private VideoStandard suggestedVideoStandard = null;

	
	public static final long serialVersionUID = 1L;

}
