// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.UndocumentedInstruction;

public class uARR extends UndocumentedInstruction {

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
		switch(cpu.A & 0x60) {
			case 0x60:
				cpu.CARRY = true; cpu.OVERFLOW = false; break;
			case 0x00:
				cpu.CARRY = false; cpu.OVERFLOW = false; break;
			case 0x20:
				cpu.CARRY = false; cpu.OVERFLOW = true; break;
			case 0x40:
				cpu.CARRY = true; cpu.OVERFLOW = true; break;
		}
	}

	private int ea;
	

	public static final long serialVersionUID = 1L;

}
