// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;

public class Illegal extends Instruction {

	public Illegal(M6502 cpu) {
		super(cpu);
	}

	@Override 
	public int fetch() {
		return 0;		// Consumes zero cycles to run
	}

	@Override 
	public void execute() {
		cpu.debug(">>> ILLEGAL OPCODE");
	}


	private static final long serialVersionUID = 1L;

}
