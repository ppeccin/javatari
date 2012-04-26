// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.OperandType;
import general.m6502.UndocumentedInstruction;

public class uLAX extends UndocumentedInstruction {

	public uLAX(M6502 cpu, OperandType type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {
		switch (type) {
			case Z_PAGE:
				ea = cpu.fetchZeroPageAddress(); return 3;
			case Z_PAGE_Y:
				ea = cpu.fetchZeroPageYAddress(); return 4;
			case ABS:
				ea = cpu.fetchAbsoluteAddress();  return 4;
			case ABS_Y:
				ea = cpu.fetchAbsoluteYAddress(); return 4 + (cpu.pageCrossed?1:0);
			case IND_X:
				ea = cpu.fetchIndirectXAddress(); return 6;
			case IND_Y:
				ea = cpu.fetchIndirectYAddress(); return 5 + (cpu.pageCrossed?1:0);
			default:
				throw new IllegalStateException("uLAX Invalid Operand Type: " + type);
		}
	}

	@Override
	public void execute() {
		final byte val = cpu.memory.readByte(ea);
		cpu.A = val;
		cpu.X = val;
		cpu.ZERO = val == 0;
		cpu.NEGATIVE = val < 0;
	}

	private final OperandType type;
	
	private int ea;
	

	private static final long serialVersionUID = 1L;

}
