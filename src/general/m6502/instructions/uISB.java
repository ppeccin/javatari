// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.OperandType;
import general.m6502.UndocumentedInstruction;

public class uISB extends UndocumentedInstruction {

	public uISB(M6502 cpu, OperandType type) {
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
				throw new IllegalStateException("uISB Invalid Operand Type: " + type);
		}
	}

	@Override
	public void execute() {
		final byte val = (byte) (cpu.memory.readByte(ea) + 1); 
		cpu.memory.writeByte(ea, val);

		// Same as SBC from here
		int b = val;
		int uB = M6502.toUnsignedByte(val);
		int oldA = cpu.A;
		int uOldA = M6502.toUnsignedByte(oldA);

		boolean oldCarry = cpu.CARRY;
		int aux = oldA - b - (!oldCarry?1:0); 
		int uAux = uOldA - uB - (!oldCarry?1:0); 
		
		// Flags are affected always as in Binary mode
		byte newA = (byte) M6502.toUnsignedByte(uAux);		// Could be aux 
		cpu.ZERO = newA == 0;
		cpu.NEGATIVE = newA < 0;
		cpu.OVERFLOW = aux > 127 || aux < -128; 
		cpu.CARRY = !(uAux < 0);

		// But the ACC is computed differently in Decimal Mode
		if (!cpu.DECIMAL_MODE) {
			cpu.A = newA;
			return;
		}

		// Decimal Mode computations
		uAux = (uOldA & 0x0f) - (uB & 0x0f) - (!oldCarry?1:0);
		if (uAux < 0) uAux = ((uAux - 0x06) & 0x0f) - 0x10;
		uAux = (uOldA & 0xf0) - (uB & 0xf0) + uAux;
		if (uAux < 0) uAux -= 0x60;
		cpu.A = (byte) M6502.toUnsignedByte(uAux);
	}

	private final OperandType type;
	
	private int ea;
	

	private static final long serialVersionUID = 1L;

}
