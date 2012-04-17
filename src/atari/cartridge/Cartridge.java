// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge;

import general.av.video.VideoStandard;
import general.board.BUS16Bits;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

public abstract class Cartridge implements BUS16Bits, Cloneable, Serializable {

	public Cartridge(int size) {
		emptyContent(size);
	}

	public Cartridge(byte[] content) throws IOException {
		setContent(content);
	}

	@Override
	public byte readByte(int address) {		
		return bytes[maskAddress(address)];	
	}

	@Override
	public int unsignedByte(int address) {  
		return readByte(address) & 0xff;
	}
	
	@Override
	public void writeByte(int address, byte b) {	
		// Writing to roms is possible, but nothing is changed
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public VideoStandard videoStandard() {
		return videoStandard;
	}
	
	public void forceVideoStandard(VideoStandard videoStandard) {
		this.videoStandard = videoStandard;
	}
	
	protected int maskAddress(int address) {
		return address & 0x0fff;
	}
	
	void setContent(byte[] content) {
		bytes = content;
	}
	
	protected void emptyContent(int size) {
		bytes = new byte[size];
		Arrays.fill(bytes, (byte)0x00);
	}
	
	protected byte[] bytes;
	
	private VideoStandard videoStandard = VideoStandard.NTSC;

	private static final long serialVersionUID = 1L;

}
