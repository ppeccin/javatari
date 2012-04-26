// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.UndocumentedInstruction;

public class uLAS extends UndocumentedInstruction {

	public uLAS(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {
		ea = cpu.fetchAbsoluteYAddress(); return 4 + (cpu.pageCrossed?1:0);		
	}
	
	@Override
	public void execute() {
		final byte val = (byte) (cpu.SP & cpu.memory.readByte(ea)); 
		cpu.A = val;
		cpu.X = val;
		cpu.SP = val;
		cpu.ZERO = val == 0;
		cpu.NEGATIVE = val < 0;
	}

	private int ea;

	
	private static final long serialVersionUID = 1L;

}
