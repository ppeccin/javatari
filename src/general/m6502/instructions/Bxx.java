// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;
import general.m6502.StatusBit;

public class Bxx extends Instruction {


	public Bxx(M6502 cpu, StatusBit bit, boolean cond) {
		super(cpu);
		this.bit = bit;
		this.cond = cond;
	}

	@Override
	public int fetch() {
		newPC = (char) cpu.fetchRelativeAddress();		// Reads operand regardless of the branch being taken or not
		switch (bit) {
			case bZERO:
				branch = cpu.ZERO == cond; break;
			case bNEGATIVE:
				branch = cpu.NEGATIVE == cond; break;
			case bCARRY:
				branch = cpu.CARRY == cond; break;
			case bOVERFLOW:
				branch = cpu.OVERFLOW == cond; break;
			default:
				throw new IllegalStateException("Bxx Invalid StatusBit: " + bit);
		}
		return branch ? (cpu.pageCrossed ? 4:3) : 2; 
	}

	@Override
	public void execute() {
		if (branch) cpu.PC = newPC;	
	}

	private final StatusBit bit;
	private final boolean cond;
	
	private char newPC;
	private boolean branch;
	
	
	private static final long serialVersionUID = 1L;

}
