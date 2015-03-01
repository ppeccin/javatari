// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;

public final class uKIL extends Instruction {

	public uKIL(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {

		cpu.debug(">>> Undocumented opcode KIL/HLT/JAM");

		// Actually no cycles should be taken, as the CPU would Halt. 
		// But we will simulate a VERY long instruction
		return Integer.MAX_VALUE;
	}

	@Override
	public void execute() {
		// Forces the CPU to stay stuck in this instruction forever
		if (cpu.PC == 0) cpu.PC = 0xffff;
		else cpu.PC--;
	}

	
	public static final long serialVersionUID = 1L;

}
