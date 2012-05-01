// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.UndocumentedInstruction;

public class uASR extends UndocumentedInstruction {

	public uASR(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {
		ea = cpu.fetchImmediateAddress(); return 2;
	}

	@Override
	public void execute() {
		byte val = (byte) (cpu.A & cpu.memory.readByte(ea)); 
		cpu.CARRY = (val & 0x01) > 0;		// bit 0 was set
		val = (byte) ((val & 0xff) >>> 1);
		cpu.A = val;
		cpu.ZERO = val == 0;
		cpu.NEGATIVE = false;
	}

	private int ea;
	

	private static final long serialVersionUID = 1L;
	
}
