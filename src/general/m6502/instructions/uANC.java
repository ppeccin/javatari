// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.UndocumentedInstruction;

public class uANC extends UndocumentedInstruction {

	public uANC(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {
		ea = cpu.fetchImmediateAddress(); return 2;		
	}
	
	@Override
	public void execute() {
		final byte val = (byte) (cpu.A & cpu.memory.readByte(ea)); 
		cpu.A = val;
		cpu.ZERO = val == 0;
		cpu.CARRY = cpu.NEGATIVE = val < 0;
	}

	private int ea;

	
	private static final long serialVersionUID = 1L;

}
