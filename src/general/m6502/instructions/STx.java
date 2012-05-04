// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;
import general.m6502.OperandType;
import general.m6502.Register;

public class STx extends Instruction {

	public STx(M6502 cpu, Register reg, OperandType type) {
		super(cpu);
		this.reg = reg;
		this.type = type;
	}

	@Override
	public int fetch() {
		switch (type) {
			case Z_PAGE:
				ea = cpu.fetchZeroPageAddress(); return 3;
			case Z_PAGE_X:
				ea = cpu.fetchZeroPageXAddress(); return 4;		// Not all STs support this mode
			case Z_PAGE_Y:
				ea = cpu.fetchZeroPageYAddress(); return 4;		// Not all STs support this mode
			case ABS:
				ea = cpu.fetchAbsoluteAddress(); return 4;
			case ABS_X:
				ea = cpu.fetchAbsoluteXAddress(); return 5;		// Not all STs support this mode
			case ABS_Y:
				ea = cpu.fetchAbsoluteYAddress(); return 5;		// Not all STs support this mode
			case IND_X:
				ea = cpu.fetchIndirectXAddress(); return 6;		// Not all STs support this mode
			case IND_Y:
				ea = cpu.fetchIndirectYAddress(); return 6;		// Not all STs support this mode
			default:
				throw new IllegalStateException("STx Invalid Operand Type: " + type);
		}
	}

	@Override
	public void execute() {
		switch (reg) {
			case rA:
			cpu.memory.writeByte(ea, cpu.A); break;
			case rX:
			cpu.memory.writeByte(ea, cpu.X); break;
			case rY:
			cpu.memory.writeByte(ea, cpu.Y); break;
			default:
				throw new IllegalStateException("STx Invalid Register: " + reg);
		}
	}

	private final Register reg;
	private final OperandType type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;

}
