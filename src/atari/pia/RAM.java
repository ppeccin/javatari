// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.pia;

import general.board.BUS16Bits;

import java.io.Serializable;

import parameters.Parameters;
import utils.Randomizer;

public final class RAM implements BUS16Bits {

	public RAM() {
		// RAMs come totally random at creation
		Randomizer.instance.nextBytes(bytes);
	}

	public void powerOn() {
	}

	public void powerOff() {
	}

	@Override
	public byte readByte(int address) {		
		return bytes[(address & ADDRESS_MASK)];	
	}

	@Override
	public void writeByte(int address, byte b) {	
		bytes[(address & ADDRESS_MASK)] = b;		
	}

	public void dump() {
		System.out.println("RAM DUMP:");
		for(int i = 0; i < bytes.length; i++)
			System.out.printf("%02x ", bytes[i]);
		System.out.println();
	}
	
	public RAMState saveState() {
		RAMState state = new RAMState();
		state.bytes = bytes.clone();
		return state;
	}
	
	public void loadState(RAMState state) {
		System.arraycopy(state.bytes, 0, bytes, 0, bytes.length);
	}
	
	public void powerFry() {
		float var = 1 - FRY_VARIANCE + 2 * Randomizer.instance.nextFloat() * FRY_VARIANCE;
		// Randomly put "0" in bits on the ram
		int fryZeroBits = (int)(var * FRY_ZERO_BITS);
		for (int i = 0; i < fryZeroBits; i++)
			bytes[Randomizer.instance.nextInt(128)] &= (byte)Randomizer.instance.nextInt(256);
		// Randomly put "1" in bits on the ram
		int fryOneBits = (int)(var * FRY_ONE_BITS);
		for (int i = 0; i < fryOneBits; i++)
			bytes[Randomizer.instance.nextInt(128)] |= (byte)(0x01 << Randomizer.instance.nextInt(8));
	}

	// State Variables --------------------------------------
	private final byte[] bytes = new byte[128];
	
	
	// Constants -------------------------------------------
	private static final int ADDRESS_MASK = 0x007f;

	private static final int FRY_ZERO_BITS = Parameters.RAM_FRY_ZERO_BITS;
	private static final int FRY_ONE_BITS = Parameters.RAM_FRY_ONE_BITS;
	private static final float FRY_VARIANCE = Parameters.RAM_FRY_VARIANCE;


	// Used to save/load states
	public static class RAMState implements Serializable {
		byte[] bytes;
		public static final long serialVersionUID = 2L;
	}

}
