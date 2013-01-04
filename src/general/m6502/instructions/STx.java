// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import static general.m6502.Register.rA;
import static general.m6502.Register.rX;
import static general.m6502.Register.rY;
import general.m6502.Instruction;
import general.m6502.M6502;
import general.m6502.OperandType;

public final class STx extends Instruction {

	public STx(M6502 cpu, int reg, int type) {
		super(cpu);
		this.reg = reg;
		this.type = type;
	}

	@Override
	public int fetch() {
		if (type == OperandType.Z_PAGE) 	{ ea = cpu.fetchZeroPageAddress(); return 3; }
		if (type == OperandType.Z_PAGE_X) 	{ ea = cpu.fetchZeroPageXAddress(); return 4; }		// Not all STs support this mode
		if (type == OperandType.Z_PAGE_Y) 	{ ea = cpu.fetchZeroPageYAddress(); return 4; }		// Not all STs support this mode
		if (type == OperandType.ABS) 		{ ea = cpu.fetchAbsoluteAddress(); return 4; }
		if (type == OperandType.ABS_X) 		{ ea = cpu.fetchAbsoluteXAddress(); return 5; }		// Not all STs support this mode
		if (type == OperandType.ABS_Y) 		{ ea = cpu.fetchAbsoluteYAddress(); return 5; }		// Not all STs support this mode
		if (type == OperandType.IND_X) 		{ ea = cpu.fetchIndirectXAddress(); return 6; }		// Not all STs support this mode
		if (type == OperandType.IND_Y) 		{ ea = cpu.fetchIndirectYAddress(); return 6; }		// Not all STs support this mode
		throw new IllegalStateException("STx Invalid Operand Type: " + type);
	}

	@Override
	public void execute() {
		if (reg == rA) 		cpu.memory.writeByte(ea, cpu.A);
		else if (reg == rX) 	cpu.memory.writeByte(ea, cpu.X);
		else if (reg == rY) 	cpu.memory.writeByte(ea, cpu.Y);
		else throw new IllegalStateException("STx Invalid Register: " + reg);
	}

	private final int reg;
	private final int type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;

}
