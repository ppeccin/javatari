	// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.OperandType;
import general.m6502.UndocumentedInstruction;

public class uDCP extends UndocumentedInstruction {

	public uDCP(M6502 cpu, OperandType type) {
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
				ea = cpu.fetchAbsoluteAddress();  return 6;
			case ABS_X:
				ea = cpu.fetchAbsoluteXAddress(); return 7;
			case ABS_Y:
				ea = cpu.fetchAbsoluteYAddress(); return 7;
			case IND_X:
				ea = cpu.fetchIndirectXAddress(); return 8;
			case IND_Y:
				ea = cpu.fetchIndirectYAddress(); return 8;
			default:
				throw new IllegalStateException("uDCP Invalid Operand Type: " + type);
		}
	}

	@Override
	public void execute() {
		final byte val = (byte) (cpu.memory.readByte(ea) - 1); 
		cpu.memory.writeByte(ea, val);
		int uVal = M6502.toUunsignedByte(val); 
		int uA = M6502.toUunsignedByte(cpu.A);
		cpu.CARRY = uA >= uVal;
		cpu.ZERO = uA == uVal;
		cpu.NEGATIVE = ((byte)(uA - uVal)) < 0;
	}

	private final OperandType type;
	
	private int ea;
	

	private static final long serialVersionUID = 1L;

}
