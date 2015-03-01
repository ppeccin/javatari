// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;
import org.javatari.general.m6502.OperandType;

public final class uSAX extends Instruction {

	public uSAX(M6502 cpu, int type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {

		cpu.debug(">>> Undocumented opcode SAX");

		if (type == OperandType.Z_PAGE) 	{ ea = cpu.fetchZeroPageAddress(); return 3; }
		if (type == OperandType.Z_PAGE_Y) 	{ ea = cpu.fetchZeroPageYAddress(); return 4; }
		if (type == OperandType.IND_X) 		{ ea = cpu.fetchIndirectXAddress(); return 6; }
		if (type == OperandType.ABS) 		{ ea = cpu.fetchAbsoluteAddress(); return 4; }
		throw new IllegalStateException("uSAX Invalid Operand Type: " + type);
	}

	@Override
	// Some sources say it would affect N and Z flags, some say it wouldn't. Chose not to affect
	public void execute() {
		final byte val = (byte) (cpu.A & cpu.X);
		cpu.bus.writeByte(ea, val);
	}

	private final int type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;

}
