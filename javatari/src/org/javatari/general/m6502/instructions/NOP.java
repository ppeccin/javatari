// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;

public final class NOP extends Instruction {

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
