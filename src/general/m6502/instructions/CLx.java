// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;
import general.m6502.StatusBit;

public class CLx extends Instruction {

	public CLx(M6502 cpu, StatusBit bit) {
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
				cpu.CARRY = false; break;
			case bDECIMAL_MODE:
				cpu.DECIMAL_MODE = false; break;
			case bINTERRUPT_DISABLE:
				cpu.INTERRUPT_DISABLE = false; break;
			case bOVERFLOW:
				cpu.OVERFLOW = false; break;
			default:
				throw new IllegalStateException("CLx Invalid StatusBit: " + bit);
		}
	}

	private final StatusBit bit;
	

	public static final long serialVersionUID = 1L;

}
