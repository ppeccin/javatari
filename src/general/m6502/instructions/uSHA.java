// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.Instruction;
import general.m6502.M6502;
import general.m6502.OperandType;

public final class uSHA extends Instruction {

	public uSHA(M6502 cpu, int type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {
		if (type == OperandType.ABS_Y) { ea = cpu.fetchZeroPageXAddress(); return 5; }
		if (type == OperandType.IND_Y) { ea = cpu.fetchZeroPageXAddress(); return 6; }
		throw new IllegalStateException("uAXA Invalid Operand Type: " + type);
	}

	@Override
	public void execute() {
		final byte val = (byte) (cpu.A & cpu.X & (byte)(((ea >>> 8) & 0xff) + 1));  // A & X & (High byte of address + 1) !!! 
		cpu.bus.writeByte(ea, val);
	}

	private final int type;
	
	private int ea;
	

	public static final long serialVersionUID = 1L;

}
