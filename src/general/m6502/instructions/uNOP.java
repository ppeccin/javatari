// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.OperandType;
import general.m6502.UndocumentedInstruction;

public class uNOP extends UndocumentedInstruction {

	public uNOP(M6502 cpu, OperandType type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {
		switch (type) {
			case IMM:
				ea = cpu.fetchImmediateAddress(); return 2;		
			case Z_PAGE:
				ea = cpu.fetchZeroPageAddress(); return 3;
			case Z_PAGE_X:
				ea = cpu.fetchZeroPageXAddress(); return 4;
			case ABS:
				ea = cpu.fetchAbsoluteAddress(); return 4;
			case ABS_X:
				ea = cpu.fetchAbsoluteXAddress(); return 4 + (cpu.pageCrossed?1:0);
			default:
				throw new IllegalStateException("uNOP Invalid Operand Type: " + type);
		}
	}

	@Override
	// TODO Check. Should it really ready the target memory or just fetch the effective address?
	public void execute() {
		cpu.memory.readByte(ea);
		// No effects besides fetching and reading memory
	}

	private final OperandType type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;

}
