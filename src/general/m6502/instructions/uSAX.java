// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.OperandType;
import general.m6502.UndocumentedInstruction;

public class uSAX extends UndocumentedInstruction {

	public uSAX(M6502 cpu, OperandType type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {
		switch (type) {
			case Z_PAGE:
				ea = cpu.fetchZeroPageAddress(); return 3;
			case Z_PAGE_Y:
				ea = cpu.fetchZeroPageXAddress(); return 4;
			case IND_X:
				ea = cpu.fetchZeroPageXAddress(); return 6;
			case ABS:
				ea = cpu.fetchAbsoluteAddress(); return 4;
			default:
				throw new IllegalStateException("uAAX Invalid Operand Type: " + type);
		}
	}

	@Override
	// TODO Check. Some sources say it would affect N and Z flags, some say it woudnt't. Chose not to affect
	public void execute() {
		final byte val = (byte) (cpu.A & cpu.X);
		cpu.memory.writeByte(ea, val);
	}

	private final OperandType type;
	
	private int ea;
	

	private static final long serialVersionUID = 1L;

}
