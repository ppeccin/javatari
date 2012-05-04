// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;
import general.m6502.OperandType;

public class INC extends Instruction {

	public INC(M6502 cpu, OperandType type) {
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
				throw new IllegalStateException("INC Invalid Operand Type: " + type);
		}
	}

	@Override
	public void execute() {
		final byte val = (byte) (cpu.memory.readByte(ea) + 1); 
		cpu.ZERO = val == 0;
		cpu.NEGATIVE = val < 0;
		cpu.memory.writeByte(ea, val);
	}

	private final OperandType type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;

}
