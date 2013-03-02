// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import static org.javatari.general.m6502.StatusBit.bCARRY;
import static org.javatari.general.m6502.StatusBit.bDECIMAL_MODE;
import static org.javatari.general.m6502.StatusBit.bINTERRUPT_DISABLE;
import static org.javatari.general.m6502.StatusBit.bOVERFLOW;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;


public final class CLx extends Instruction {

	public CLx(M6502 cpu, int bit) {
		super(cpu);
		this.bit = bit;
	}

	@Override
	public int fetch() {
		return 2;
	}

	@Override
	public void execute() {
		if (bit == bCARRY) 					{ cpu.CARRY = false; }
		else if (bit == bDECIMAL_MODE) 		{ cpu.DECIMAL_MODE = false; }
		else if (bit == bINTERRUPT_DISABLE) { cpu.INTERRUPT_DISABLE = false; }
		else if (bit == bOVERFLOW) 			{ cpu.OVERFLOW = false; }
		else throw new IllegalStateException("CLx Invalid StatusBit: " + bit);
	}

	private final int bit;
	

	public static final long serialVersionUID = 1L;

}
