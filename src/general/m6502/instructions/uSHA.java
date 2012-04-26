// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.OperandType;
import general.m6502.UndocumentedInstruction;

public class uSHA extends UndocumentedInstruction {

	public uSHA(M6502 cpu, OperandType type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {
		switch (type) {
			case ABS_Y:
				ea = cpu.fetchZeroPageXAddress(); return 5;
			case IND_Y:
				ea = cpu.fetchZeroPageXAddress(); return 6;
			default:
				throw new IllegalStateException("uAXA Invalid Operand Type: " + type);
		}
	}

	@Override
	public void execute() {
		final byte val = (byte) (cpu.A & cpu.X & (byte)(((ea >>> 8) & 0xff) + 1));  // A & X & (High byte of address + 1) !!! 
		cpu.memory.writeByte(ea, val);
	}

	private final OperandType type;
	
	private int ea;
	

	private static final long serialVersionUID = 1L;

}
