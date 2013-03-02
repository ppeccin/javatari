// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;
import org.javatari.general.m6502.OperandType;

public final class SBC extends Instruction {

	public SBC(M6502 cpu, int type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {
		if (type == OperandType.IMM) 		{ ea = cpu.fetchImmediateAddress(); return 2; }
		if (type == OperandType.Z_PAGE) 	{ ea = cpu.fetchZeroPageAddress(); return 3; }
		if (type == OperandType.Z_PAGE_X) 	{ ea = cpu.fetchZeroPageXAddress(); return 4; }
		if (type == OperandType.ABS) 		{ ea = cpu.fetchAbsoluteAddress(); return 4; }
		if (type == OperandType.ABS_X) 		{ ea = cpu.fetchAbsoluteXAddress(); return 4 + (cpu.pageCrossed?1:0); }
		if (type == OperandType.ABS_Y) 		{ ea = cpu.fetchAbsoluteYAddress(); return 4 + (cpu.pageCrossed?1:0); }
		if (type == OperandType.IND_X) 		{ ea = cpu.fetchIndirectXAddress(); return 6; }
		if (type == OperandType.IND_Y) 		{ ea = cpu.fetchIndirectYAddress(); return 5 + (cpu.pageCrossed?1:0); }
		throw new IllegalStateException("SBC Invalid Operand Type: " + type);
	}

	@Override
	public void execute() {
		final int b = cpu.bus.readByte(ea);
		final int uB = M6502.toUnsignedByte(b);
		final int oldA = cpu.A;
		final int uOldA = M6502.toUnsignedByte(oldA);

		final boolean oldCarry = cpu.CARRY;
		final int aux = oldA - b - (!oldCarry?1:0); 
		int uAux = uOldA - uB - (!oldCarry?1:0); 
		
		// Flags are affected always as in Binary mode
		final byte newA = (byte) M6502.toUnsignedByte(uAux);		// Could be aux 
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

	private final int type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;

}
