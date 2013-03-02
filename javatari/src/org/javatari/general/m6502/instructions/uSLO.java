// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;
import org.javatari.general.m6502.OperandType;

public final class uSLO extends Instruction {

	public uSLO(M6502 cpu, int type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {
		if (type == OperandType.Z_PAGE) 	{ ea = cpu.fetchZeroPageAddress(); return 5; }
		if (type == OperandType.Z_PAGE_X) 	{ ea = cpu.fetchZeroPageXAddress(); return 6; }
		if (type == OperandType.ABS) 		{ ea = cpu.fetchAbsoluteAddress(); return 6; }
		if (type == OperandType.ABS_X) 		{ ea = cpu.fetchAbsoluteXAddress(); return 7; }
		if (type == OperandType.ABS_Y) 		{ ea = cpu.fetchAbsoluteYAddress(); return 7; }
		if (type == OperandType.IND_X) 		{ ea = cpu.fetchIndirectXAddress(); return 8; }
		if (type == OperandType.IND_Y) 		{ ea = cpu.fetchIndirectYAddress(); return 8; }
		throw new IllegalStateException("uSLO Invalid Operand Type: " + type);
	}

	@Override
	public void execute() {
		byte val = cpu.bus.readByte(ea); 
		cpu.CARRY = val < 0;		// bit 7 was set
		val = (byte) (val << 1);
		cpu.bus.writeByte(ea, val);

		val = (byte) (cpu.A | val);
		cpu.A = val;
		cpu.ZERO = val == 0;
		cpu.NEGATIVE = val < 0;
	}

	private final int type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;

}
