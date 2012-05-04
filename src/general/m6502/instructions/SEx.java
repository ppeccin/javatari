// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;
import general.m6502.StatusBit;

public class SEx extends Instruction {

	public SEx(M6502 cpu, StatusBit bit) {
		super(cpu);
		this.bit = bit;
	}

	@Override
	public int fetch() {
		return 2;
	}

	@Override
	public void execute() {
		switch (bit) {
			case bCARRY:
				cpu.CARRY = true; break;
			case bDECIMAL_MODE:
				cpu.DECIMAL_MODE = true; break;
			case bINTERRUPT_DISABLE:
				cpu.INTERRUPT_DISABLE = true; break;
			default:
				throw new IllegalStateException("SEx Invalid StatusBit: " + bit);
		}
	}

	private final StatusBit bit;
	

	public static final long serialVersionUID = 1L;

}
