// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;

public final class uANE extends Instruction {

	public uANE(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {
		ea = cpu.fetchImmediateAddress(); return 2;		
	}

	@Override
	public void execute() {
		cpu.bus.readByte(ea);
		// Exact operation unknown. Lets do nothing!
		cpu.debug(">>> Undocumented opcode ANE (XAA)");
	}

	private int ea;
	

	public static final long serialVersionUID = 1L;

}
