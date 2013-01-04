// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import static general.m6502.StatusBit.bCARRY;
import static general.m6502.StatusBit.bDECIMAL_MODE;
import static general.m6502.StatusBit.bINTERRUPT_DISABLE;
import general.m6502.Instruction;
import general.m6502.M6502;

public final class SEx extends Instruction {

	public SEx(M6502 cpu, int bit) {
		super(cpu);
		this.bit = bit;
	}

	@Override
	public int fetch() {
		return 2;
	}

	@Override
	public void execute() {
		if (bit == bCARRY) 					cpu.CARRY = true;
		else if (bit == bDECIMAL_MODE) 		cpu.DECIMAL_MODE = true;
		else if (bit == bINTERRUPT_DISABLE) 	cpu.INTERRUPT_DISABLE = true;
		else throw new IllegalStateException("SEx Invalid StatusBit: " + bit);
	}

	private final int bit;
	

	public static final long serialVersionUID = 1L;

}
