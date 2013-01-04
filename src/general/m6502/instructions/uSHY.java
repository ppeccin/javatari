// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.UndocumentedInstruction;

public final class uSHY extends UndocumentedInstruction {

	public uSHY(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {
		ea = cpu.fetchAbsoluteXAddress(); return 5;		
	}
	
	@Override
	public void execute() {
		final byte val = (byte) (cpu.Y & (byte)(((ea >>> 8) & 0xff) + 1));  // Y & (High byte of address + 1) !!! 
		cpu.memory.writeByte(ea, val);
	}

	private int ea;

	
	public static final long serialVersionUID = 1L;

}
