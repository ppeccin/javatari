// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;
import general.m6502.OperandType;

public class DEC extends Instruction {

	public DEC(M6502 cpu, OperandType type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {
		switch (type) {
			case Z_PAGE:
				ea = cpu.fetchZeroPageAddress(); return 5;
			case Z_PAGE_X:
				ea = cpu.fetchZeroPageXAddress(); return 6;
			case ABS:
				ea = cpu.fetchAbsoluteAddress(); return 6;
			case ABS_X:
				ea = cpu.fetchAbsoluteXAddress(); return 7;
			default:
				throw new IllegalStateException("DEC Invalid Operand Type: " + type);
		}
	}

	@Override
	public void execute() {
		final byte val = (byte) (cpu.memory.readByte(ea) - 1); 
		cpu.memory.writeByte(ea, val);
		cpu.ZERO = val == 0;
		cpu.NEGATIVE = val < 0;
	}

	private final OperandType type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;

}
