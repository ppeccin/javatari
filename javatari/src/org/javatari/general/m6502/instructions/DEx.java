// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;
import org.javatari.general.m6502.Register;

public final class DEx extends Instruction {

	public DEx(M6502 cpu, int r) {
		super(cpu);
		this.reg = r;
	}

	@Override
	public int fetch() {
		return 2;
	}
	
	@Override
	public void execute() {
		final byte val;
		if (reg == Register.rX) 		{ val = (byte) (cpu.X - 1); cpu.X = val; }
		else if (reg == Register.rY) 	{ val = (byte) (cpu.Y - 1); cpu.Y = val; }
		else throw new IllegalStateException("DEx Invalid Register: " + reg);
		cpu.ZERO = val == 0;
		cpu.NEGATIVE = val < 0;
	}

	private final int reg;
	

	public static final long serialVersionUID = 1L;

}
