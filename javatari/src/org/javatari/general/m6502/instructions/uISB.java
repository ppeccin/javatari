// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;
import org.javatari.general.m6502.OperandType;

public final class uISB extends Instruction {

	public uISB(M6502 cpu, int type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {

		cpu.debug(">>> Undocumented opcode ISB");

		if (type == OperandType.Z_PAGE) 	{ ea = cpu.fetchZeroPageAddress(); return 5; }
		if (type == OperandType.Z_PAGE_X) 	{ ea = cpu.fetchZeroPageXAddress(); return 6; }
		if (type == OperandType.ABS) 		{ ea = cpu.fetchAbsoluteAddress(); return 6; }
		if (type == OperandType.ABS_X) 		{ ea = cpu.fetchAbsoluteXAddress(); return 7; }
		if (type == OperandType.ABS_Y) 		{ ea = cpu.fetchAbsoluteYAddress(); return 7; }
		if (type == OperandType.IND_X) 		{ ea = cpu.fetchIndirectXAddress(); return 8; }
		if (type == OperandType.IND_Y) 		{ ea = cpu.fetchIndirectYAddress(); return 8; }
		throw new IllegalStateException("uISB Invalid Operand Type: " + type);
	}

	@Override
	public void execute() {
		final byte val = (byte) (cpu.bus.readByte(ea) + 1); 
		cpu.bus.writeByte(ea, val);

		// Same as SBC from here
		final int b = val;
		final int uB = M6502.toUnsignedByte(val);
		final int oldA = cpu.A;
		final int uOldA = M6502.toUnsignedByte(oldA);

		int oldCarryNot = cpu.CARRY?0:1;
		final int aux = oldA - b - oldCarryNot; 
		int uAux = uOldA - uB - oldCarryNot; 
		
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
		uAux = (uOldA & 0x0f) - (uB & 0x0f) - oldCarryNot;
		if (uAux < 0) uAux = ((uAux - 0x06) & 0x0f) - 0x10;
		uAux = (uOldA & 0xf0) - (uB & 0xf0) + uAux;
		if (uAux < 0) uAux -= 0x60;
		cpu.A = (byte) M6502.toUnsignedByte(uAux);
	}

	private final int type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;

}
