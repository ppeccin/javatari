// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.Instruction;
import general.m6502.M6502;
import general.m6502.OperandType;

public final class uSAX extends Instruction {

	public uSAX(M6502 cpu, int type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {
		if (type == OperandType.Z_PAGE) 	{ ea = cpu.fetchZeroPageAddress(); return 3; }
		if (type == OperandType.Z_PAGE_Y) 	{ ea = cpu.fetchZeroPageXAddress(); return 4; }
		if (type == OperandType.IND_X) 		{ ea = cpu.fetchZeroPageXAddress(); return 6; }
		if (type == OperandType.ABS) 		{ ea = cpu.fetchAbsoluteAddress(); return 4; }
		throw new IllegalStateException("uAAX Invalid Operand Type: " + type);
	}

	@Override
	// Some sources say it would affect N and Z flags, some say it woudnt't. Chose not to affect
	public void execute() {
		final byte val = (byte) (cpu.A & cpu.X);
		cpu.bus.writeByte(ea, val);
	}

	private final int type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;

}
