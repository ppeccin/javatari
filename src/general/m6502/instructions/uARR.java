// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.UndocumentedInstruction;

public final class uARR extends UndocumentedInstruction {

	public uARR(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {
		ea = cpu.fetchImmediateAddress(); return 2;
	}

	@Override
	// Some sources say flags are affected per ROR, others say its more complex. The complex one is chosen
	public void execute() {
		byte val = (byte) (cpu.A & cpu.memory.readByte(ea)); 
		int oldCarry = cpu.CARRY?1:0;

		// Per ROR
		// cpu.CARRY = (val & 0x01) > 0;		// bit 0 was set

		val = (byte) (((val & 0xff) >>> 1) + oldCarry * 0x80);
		cpu.A = val;
		cpu.ZERO = val == 0;
		cpu.NEGATIVE = val < 0;
		
		// Complex
		int comp = cpu.A & 0x60;
		if (comp == 0x60) 		{ cpu.CARRY = true; cpu.OVERFLOW = false; }
		else if (comp == 0x00) 	{ cpu.CARRY = false; cpu.OVERFLOW = false; }
		else if (comp == 0x20) 	{ cpu.CARRY = false; cpu.OVERFLOW = true; }
		else if (comp == 0x40) 	{ cpu.CARRY = true; cpu.OVERFLOW = true; }
	}

	private int ea;
	

	public static final long serialVersionUID = 1L;

}
