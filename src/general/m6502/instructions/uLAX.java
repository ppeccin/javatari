// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.Instruction;
import general.m6502.M6502;
import general.m6502.OperandType;

public final class uLAX extends Instruction {

	public uLAX(M6502 cpu, int type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {
		if (type == OperandType.Z_PAGE) 	{ ea = cpu.fetchZeroPageAddress(); return 3; }
		if (type == OperandType.Z_PAGE_Y) 	{ ea = cpu.fetchZeroPageYAddress(); return 4; }
		if (type == OperandType.ABS) 		{ ea = cpu.fetchAbsoluteAddress(); return 4; }
		if (type == OperandType.ABS_Y) 		{ ea = cpu.fetchAbsoluteYAddress(); return 4 + (cpu.pageCrossed?1:0); }
		if (type == OperandType.IND_X) 		{ ea = cpu.fetchIndirectXAddress(); return 6; }
		if (type == OperandType.IND_Y) 		{ ea = cpu.fetchIndirectYAddress(); return 5 + (cpu.pageCrossed?1:0); }
		throw new IllegalStateException("uLAX Invalid Operand Type: " + type);
	}

	@Override
	public void execute() {
		final byte val = cpu.bus.readByte(ea);
		cpu.A = val;
		cpu.X = val;
		cpu.ZERO = val == 0;
		cpu.NEGATIVE = val < 0;
	}

	private final int type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;

}
