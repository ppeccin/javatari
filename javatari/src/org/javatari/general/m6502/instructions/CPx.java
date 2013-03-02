// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;
import org.javatari.general.m6502.OperandType;
import org.javatari.general.m6502.Register;

public final class CPx extends Instruction {

	public CPx(M6502 cpu, int reg, int type) {
		super(cpu);
		this.reg = reg;
		this.type = type;
	}

	@Override
	public int fetch() {
		if (type == OperandType.IMM) 		{ ea = cpu.fetchImmediateAddress(); return 2; }
		if (type == OperandType.Z_PAGE) 	{ ea = cpu.fetchZeroPageAddress(); return 3; }
		if (type == OperandType.Z_PAGE_X) 	{ ea = cpu.fetchZeroPageXAddress(); return 4;	}						// Not all CPx support this mode
		if (type == OperandType.ABS) 		{ ea = cpu.fetchAbsoluteAddress(); return 4; }
		if (type == OperandType.ABS_X) 		{ ea = cpu.fetchAbsoluteXAddress(); return 4 + (cpu.pageCrossed?1:0); }	// Not all CPx support this mode
		if (type == OperandType.ABS_Y) 		{ ea = cpu.fetchAbsoluteYAddress(); return 4 + (cpu.pageCrossed?1:0); }	// Not all CPx support this mode
		if (type == OperandType.IND_X) 		{ ea = cpu.fetchIndirectXAddress(); return 6; }							// Not all CPx support this mode
		if (type == OperandType.IND_Y) 		{ ea = cpu.fetchIndirectYAddress(); return 5 + (cpu.pageCrossed?1:0); }	// Not all CPx support this mode
		throw new IllegalStateException("CPx Invalid Operand Type: " + type);
	}

	@Override
	public void execute() {
		int uVal = M6502.toUnsignedByte(cpu.bus.readByte(ea)); 
		int uR;
		if (reg == Register.rA) 		uR = M6502.toUnsignedByte(cpu.A);
		else if (reg == Register.rX) 	uR = M6502.toUnsignedByte(cpu.X);
		else if (reg == Register.rY) 	uR = M6502.toUnsignedByte(cpu.Y);
		else throw new IllegalStateException("CPx Invalid Register: " + reg);
		cpu.CARRY = uR >= uVal;
		cpu.ZERO = uR == uVal;
		cpu.NEGATIVE = ((byte)(uR - uVal)) < 0;
	}

	private final int reg;
	private final int type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;

}
