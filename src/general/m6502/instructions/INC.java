// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;
import general.m6502.OperandType;

public final class INC extends Instruction {

	public INC(M6502 cpu, int type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {
		if (type == OperandType.Z_PAGE) 	{ ea = cpu.fetchZeroPageAddress(); return 5; }
		if (type == OperandType.Z_PAGE_X) 	{ ea = cpu.fetchZeroPageXAddress(); return 6; }
		if (type == OperandType.ABS) 		{ ea = cpu.fetchAbsoluteAddress(); return 6; }
		if (type == OperandType.ABS_X) 		{ ea = cpu.fetchAbsoluteXAddress(); return 7; }
		throw new IllegalStateException("INC Invalid Operand Type: " + type);
	}

	@Override
	public void execute() {
		final byte val = (byte) (cpu.bus.readByte(ea) + 1); 
		cpu.ZERO = val == 0;
		cpu.NEGATIVE = val < 0;
		cpu.bus.writeByte(ea, val);
	}

	private final int type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;

}
