// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge;

import general.av.video.VideoStandard;
import general.board.BUS16Bits;

import java.io.Serializable;
import java.util.Arrays;

public abstract class Cartridge implements BUS16Bits, Cloneable, Serializable {

	@Override
	public byte readByte(int address) {		
		return bytes[maskAddress(address)];	
	}

	@Override
	public void writeByte(int address, byte b) {	
		// Writing to ROMs is possible, but nothing is changed
	}

	@Override
	public Cartridge clone() {
		try { return (Cartridge)super.clone(); } catch (CloneNotSupportedException e) {}
		return null;
	}

	public VideoStandard suggestedVideoStandard() {
		return suggestedVideoStandard;
	}
	
	public void suggestedVideoStandard(VideoStandard videoStandard) {
		this.suggestedVideoStandard = videoStandard;
	}
	
	protected int maskAddress(int address) {
		return address & 0x0fff;
	}
	
	protected void setContent(byte[] content) {
		bytes = content;
	}
	
	protected void emptyContent(int size) {
		bytes = new byte[size];
		Arrays.fill(bytes, (byte)0x00);
	}
	
	protected byte[] bytes;
	
	private VideoStandard suggestedVideoStandard = null;

	public static final long serialVersionUID = 1L;

}
