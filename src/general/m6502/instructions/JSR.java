// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;

public class JSR extends Instruction {

	public JSR(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {
		newPC = (char) cpu.fetchAbsoluteAddress();
		return 6;
	}

	@Override
	public void execute() {
		cpu.pushWord((char) (cpu.PC - 1));		// JSR should push the return address - 1
		cpu.PC = newPC;
	}

	private char newPC;
	

	private static final long serialVersionUID = 1L;
	
}
