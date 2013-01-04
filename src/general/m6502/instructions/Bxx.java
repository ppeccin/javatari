// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import static general.m6502.StatusBit.bCARRY;
import static general.m6502.StatusBit.bNEGATIVE;
import static general.m6502.StatusBit.bOVERFLOW;
import static general.m6502.StatusBit.bZERO;
import general.m6502.Instruction;
import general.m6502.M6502;

public final class Bxx extends Instruction {


	public Bxx(M6502 cpu, int bit, boolean cond) {
		super(cpu);
		this.bit = bit;
		this.cond = cond;
	}

	@Override
	public int fetch() {
		newPC = (char) cpu.fetchRelativeAddress();		// Reads operand regardless of the branch being taken or not
		if (bit == bZERO) 			{ branch = cpu.ZERO == cond; }
		else if (bit == bNEGATIVE)	{ branch = cpu.NEGATIVE == cond; }
		else if (bit == bCARRY)		{ branch = cpu.CARRY == cond; }
		else if (bit == bOVERFLOW)	{ branch = cpu.OVERFLOW == cond; }
		else throw new IllegalStateException("Bxx Invalid StatusBit: " + bit);

		return branch ? (cpu.pageCrossed ? 4:3) : 2; 
	}

	@Override
	public void execute() {
		if (branch) cpu.PC = newPC;	
	}

	private final int bit;
	private final boolean cond;
	
	private char newPC;
	private boolean branch;
	
	
	public static final long serialVersionUID = 1L;

}
