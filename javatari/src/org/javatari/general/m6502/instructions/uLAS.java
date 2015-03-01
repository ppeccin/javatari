// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;

public final class uLAS extends Instruction {

	public uLAS(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {

		cpu.debug(">>> Undocumented opcode LAS");

		ea = cpu.fetchAbsoluteYAddress(); return 4 + (cpu.pageCrossed?1:0);		
	}
	
	@Override
	public void execute() {
		final byte val = (byte) (cpu.SP & cpu.bus.readByte(ea)); 
		cpu.A = val;
		cpu.X = val;
		cpu.SP = val;
		cpu.ZERO = val == 0;
		cpu.NEGATIVE = val < 0;
	}

	private int ea;

	
	public static final long serialVersionUID = 1L;

}
