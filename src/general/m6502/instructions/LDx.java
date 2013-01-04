// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;
import general.m6502.OperandType;
import general.m6502.Register;

public final class LDx extends Instruction {

	public LDx(M6502 cpu, int reg, int type) {
		super(cpu);
		this.reg = reg;
		this.type = type;
	}

	@Override
	public int fetch() {
		if (type == OperandType.IMM) 		{ ea = cpu.fetchImmediateAddress(); return 2; }
		if (type == OperandType.Z_PAGE) 	{ ea = cpu.fetchZeroPageAddress(); return 3; }
		if (type == OperandType.Z_PAGE_X) 	{ ea = cpu.fetchZeroPageXAddress(); return 4;	}						// Not all LDs support this mode
		if (type == OperandType.Z_PAGE_Y) 	{ ea = cpu.fetchZeroPageYAddress(); return 4; }							// Not all LDs support this mode
		if (type == OperandType.ABS) 		{ ea = cpu.fetchAbsoluteAddress(); return 4; }
		if (type == OperandType.ABS_X) 		{ ea = cpu.fetchAbsoluteXAddress(); return 4 + (cpu.pageCrossed?1:0); }	// Not all LDs support this mode
		if (type == OperandType.ABS_Y) 		{ ea = cpu.fetchAbsoluteYAddress(); return 4 + (cpu.pageCrossed?1:0); }	// Not all LDs support this mode
		if (type == OperandType.IND_X) 		{ ea = cpu.fetchIndirectXAddress(); return 6; }							// Not all LDs support this mode
		if (type == OperandType.IND_Y) 		{ ea = cpu.fetchIndirectYAddress(); return 5 + (cpu.pageCrossed?1:0); }	// Not all LDs support this mode }
		throw new IllegalStateException("LDx Invalid Operand Type: " + type);
	}

	@Override
	public void execute() {
		final byte val = cpu.memory.readByte(ea);
		if (reg == Register.rA) 		cpu.A = val;
		else if (reg == Register.rX) 	cpu.X = val;
		else if (reg == Register.rY) 	cpu.Y = val;
		else throw new IllegalStateException("LDx Invalid Register: " + reg);
		cpu.ZERO = val == 0;
		cpu.NEGATIVE = val < 0;
	}

	private final int reg;
	private final int type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;

}
