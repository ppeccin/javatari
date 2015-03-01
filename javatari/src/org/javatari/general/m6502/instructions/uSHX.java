// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;

public final class uSHX extends Instruction {

	public uSHX(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {

		cpu.debug(">>> Undocumented opcode SHX");

		ea = cpu.fetchAbsoluteYAddress(); return 5;		
	}
	
	@Override
	public void execute() {
		final byte val = (byte) (cpu.X & (byte)(((ea >>> 8) & 0xff) + 1));  // X & (High byte of address + 1) !!! 
		cpu.bus.writeByte(ea, val);
	}

	private int ea;

	
	public static final long serialVersionUID = 1L;

}
