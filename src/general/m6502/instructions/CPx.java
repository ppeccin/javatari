// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;
import general.m6502.OperandType;
import general.m6502.Register;

public class CPx extends Instruction {

	public CPx(M6502 cpu, Register reg, OperandType type) {
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
				ea = cpu.fetchZeroPageXAddress(); return 4;		// Not all CPx support this mode
			case ABS:
				ea = cpu.fetchAbsoluteAddress();  return 4;
			case ABS_X:
				ea = cpu.fetchAbsoluteXAddress(); return 4 + (cpu.pageCrossed?1:0);		// Not all CPx support this mode
			case ABS_Y:
				ea = cpu.fetchAbsoluteYAddress(); return 4 + (cpu.pageCrossed?1:0);		// Not all CPx support this mode
			case IND_X:
				ea = cpu.fetchIndirectXAddress(); return 6;		// Not all CPx support this mode
			case IND_Y:
				ea = cpu.fetchIndirectYAddress(); return 5 + (cpu.pageCrossed?1:0);		// Not all CPx support this mode
			default:
				throw new IllegalStateException("CPx Invalid Operand Type: " + type);
		}
	}

	@Override
	public void execute() {
		int uVal = cpu.memory.unsignedByte(ea); 
		int uR;
		switch (reg) {
			case rA:
				uR = M6502.toUunsignedByte(cpu.A);
				break;
			case rX:
				uR = M6502.toUunsignedByte(cpu.X);
				break;
			case rY:
				uR = M6502.toUunsignedByte(cpu.Y);
				break;
			default:
				throw new IllegalStateException("CPx Invalid Register: " + reg);
		}
		cpu.CARRY = uR >= uVal;
		cpu.ZERO = uR == uVal;
		cpu.NEGATIVE = ((byte)(uR - uVal)) < 0;
	}

	private final Register reg;
	private final OperandType type;
	
	private int ea;
	

	private static final long serialVersionUID = 1L;

}
