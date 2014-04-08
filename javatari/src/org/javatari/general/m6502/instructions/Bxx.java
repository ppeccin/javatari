// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import static org.javatari.general.m6502.StatusBit.bCARRY;
import static org.javatari.general.m6502.StatusBit.bNEGATIVE;
import static org.javatari.general.m6502.StatusBit.bOVERFLOW;
import static org.javatari.general.m6502.StatusBit.bZERO;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;

public final class Bxx extends Instruction {


	public Bxx(M6502 cpu, int bit, boolean cond) {
		super(cpu);
		this.bit = bit;
		this.cond = cond;
	}

	@Override
	public int fetch() {
		newPC = cpu.fetchRelativeAddress();		// Reads operand regardless of the branch being taken or not
		if (bit == bZERO) 			{ branch = cpu.ZERO == cond; }
		else if (bit == bNEGATIVE)	{ branch = cpu.NEGATIVE == cond; }
		else if (bit == bCARRY)		{ branch = cpu.CARRY == cond; }
		else if (bit == bOVERFLOW)	{ branch = cpu.OVERFLOW == cond; }
		else throw new IllegalStateException("Bxx Invalid StatusBit: " + bit);

		return branch ? (cpu.pageCrossed ? 4:3) : 2; 
	}

	@Override
	public void execute() {
		if (branch) cpu.PC = newPC;		// TODO Check if we have to make additional reads here
	}
	
	// TODO Verify
	public int fetch2() {
		newPC = cpu.fetchRelativeAddress() + cpu.PC;		// Reads operand regardless of the branch being taken or not
		if (bit == bZERO) 			{ branch = cpu.ZERO == cond; }
		else if (bit == bNEGATIVE)	{ branch = cpu.NEGATIVE == cond; }
		else if (bit == bCARRY)		{ branch = cpu.CARRY == cond; }
		else if (bit == bOVERFLOW)	{ branch = cpu.OVERFLOW == cond; }
		else throw new IllegalStateException("Bxx Invalid StatusBit: " + bit);

		if (!branch) return 2;
		if ((newPC & 0xff00) != (cpu.PC & 0xff00))
			return 4;
		else
			return 3;
	}

	public void execute2() {
		if (branch) cpu.PC = newPC;	
	}


	private final int bit;
	private final boolean cond;
	
	private int newPC;
	private boolean branch;
	
	
	public static final long serialVersionUID = 1L;

}
