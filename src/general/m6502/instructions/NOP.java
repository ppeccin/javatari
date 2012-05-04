// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;

public class NOP extends Instruction {

	public NOP(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {
		return 2;
	}

	@Override
	public void execute() {
		// No effects
	}
	

	public static final long serialVersionUID = 1L;
	
}
