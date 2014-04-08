// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;

public final class RTS extends Instruction {

	public RTS(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {
		return 6;
	}

	@Override
	public void execute() {
		cpu.dummyStackRead();
		cpu.PC = cpu.pullWord();
		cpu.bus.readByte(cpu.PC++);		// Perform a dummy PC read before PC increment, discard data
	}

	public static final long serialVersionUID = 1L;

}
