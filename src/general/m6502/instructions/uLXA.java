// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.UndocumentedInstruction;

public class uLXA extends UndocumentedInstruction {

	public uLXA(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {
		ea = cpu.fetchImmediateAddress(); return 2;		
	}
	
	@Override
	// TODO Check. Some sources say its an OR with $EE then AND with IMM, others exclude the OR, others exclude both the OR and the AND. Exluding just the OR
	public void execute() {
		byte val = (byte) (cpu.A /* | 0xEE) */ & cpu.memory.readByte(ea)); 
		cpu.A = val;
		cpu.X = val;
		cpu.ZERO = val == 0;
		cpu.NEGATIVE = val < 0;
	}

	private int ea;

	
	private static final long serialVersionUID = 1L;

}
