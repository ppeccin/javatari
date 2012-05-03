// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;
import general.m6502.OperandType;

import static general.m6502.OperandType.*;

public class ROL extends Instruction {

	public ROL(M6502 cpu, OperandType type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {
		switch (type) {
			case ACC:
				ea = -1;
				return 2;
			case Z_PAGE:
				ea = cpu.fetchZeroPageAddress(); return 5;
			case Z_PAGE_X:
				ea = cpu.fetchZeroPageXAddress(); return 6;
			case ABS:
				ea = cpu.fetchAbsoluteAddress(); return 6;
			case ABS_X:
				ea = cpu.fetchAbsoluteXAddress(); return 7;
			default:
				throw new IllegalStateException("ROL Invalid Operand Type: " + type);
		}
	}

	@Override
	public void execute() {
		// Special case for ACC
		if (type == ACC) {
			byte val = cpu.A;
			int oldCarry = cpu.CARRY?1:0;
			cpu.CARRY = val < 0;		// bit 7 was set
			val = (byte) ((val << 1) + oldCarry);
			cpu.A = val;
			cpu.ZERO = val == 0;
			cpu.NEGATIVE = val < 0;
		} else {
			byte val = cpu.memory.readByte(ea); 
			int oldCarry = cpu.CARRY?1:0;
			cpu.CARRY = val < 0;		// bit 7 was set
			val = (byte) ((val << 1) | oldCarry);
			cpu.ZERO = val == 0;
			cpu.NEGATIVE = val < 0;
			cpu.memory.writeByte(ea, val);
		}
	}

	private final OperandType type;
	
	private int ea;
	

	private static final long serialVersionUID = 1L;

}
