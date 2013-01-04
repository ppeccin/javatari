// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;
import general.m6502.OperandType;

public final class BIT extends Instruction {

	public BIT(M6502 cpu, int type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {
		if (type == OperandType.Z_PAGE)	{ ea = cpu.fetchZeroPageAddress(); return 3; }
		if (type == OperandType.ABS)	{ ea = cpu.fetchAbsoluteAddress(); return 4; }
		throw new IllegalStateException("BIT Invalid Operand Type: " + type);
	}

	@Override
	public void execute() {
		final byte val = cpu.memory.readByte(ea);
		cpu.ZERO = (val & cpu.A) == 0;
		cpu.OVERFLOW = (val & 0x40) != 0;		// value of bit 6 from memory
		cpu.NEGATIVE = (val & 0x80) != 0;		// value of bit 7 from memory
	}

	private final int type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;
	
}
