// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;
import org.javatari.general.m6502.OperandType;

public final class uNOP extends Instruction {

	public uNOP(M6502 cpu, int type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {

		cpu.debug(">>> Undocumented opcode DOP");

		if (type == OperandType.IMM) 		{ ea = cpu.fetchImmediateAddress(); return 2; }
		if (type == OperandType.Z_PAGE) 	{ ea = cpu.fetchZeroPageAddress(); return 3; }
		if (type == OperandType.Z_PAGE_X) 	{ ea = cpu.fetchZeroPageXAddress(); return 4; }
		if (type == OperandType.ABS) 		{ ea = cpu.fetchAbsoluteAddress(); return 4; }
		if (type == OperandType.ABS_X) 		{ ea = cpu.fetchAbsoluteXAddress(); return 4 + (cpu.pageCrossed?1:0); }
		throw new IllegalStateException("uNOP Invalid Operand Type: " + type);
	}

	@Override
	public void execute() {
		cpu.bus.readByte(ea);
		// No effects besides fetching and reading memory
	}

	private final int type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;

}
