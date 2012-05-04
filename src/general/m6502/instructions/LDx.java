// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;
import general.m6502.OperandType;
import general.m6502.Register;

public class LDx extends Instruction {

	public LDx(M6502 cpu, Register reg, OperandType type) {
		super(cpu);
		this.reg = reg;
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
				ea = cpu.fetchZeroPageXAddress(); return 4;		// Not all LDs support this mode
			case Z_PAGE_Y:
				ea = cpu.fetchZeroPageYAddress(); return 4;		// Not all LDs support this mode
			case ABS:
				ea = cpu.fetchAbsoluteAddress(); return 4;
			case ABS_X:
				ea = cpu.fetchAbsoluteXAddress(); return 4 + (cpu.pageCrossed?1:0);		// Not all LDs support this mode
			case ABS_Y:
				ea = cpu.fetchAbsoluteYAddress(); return 4 + (cpu.pageCrossed?1:0);		// Not all LDs support this mode
			case IND_X:
				ea = cpu.fetchIndirectXAddress(); return 6;		// Not all LDs support this mode
			case IND_Y:
				ea = cpu.fetchIndirectYAddress(); return 5 + (cpu.pageCrossed?1:0);		// Not all LDs support this mode
			default:
				throw new IllegalStateException("LDx Invalid Operand Type: " + type);
		}
	}

	@Override
	public void execute() {
		final byte val = cpu.memory.readByte(ea);
		switch (reg) {
			case rA:
				cpu.A = val; break;
			case rX:
				cpu.X = val; break;
			case rY:
				cpu.Y = val; break;
			default:
				throw new IllegalStateException("LDx Invalid Register: " + reg);
		}
		cpu.ZERO = val == 0;
		cpu.NEGATIVE = val < 0;
	}

	private final Register reg;
	private final OperandType type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;

}
