// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;

public final class JSR extends Instruction {

	public JSR(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {
		ea = cpu.fetchImmediateAddress();
		return 6;
	}

	@Override
	public void execute() {
		byte PCL = cpu.bus.readByte(ea);
		cpu.pushWord(cpu.PC);				// JSR should push the return address - 1
		byte PCH = cpu.bus.readByte(ea + 1);
		cpu.PC = (M6502.toUnsignedByte(PCH) << 8) | M6502.toUnsignedByte(PCL);
	}

	private int ea;

	
	public static final long serialVersionUID = 1L;
	
}
