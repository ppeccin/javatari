// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;
import org.javatari.general.m6502.Register;

import static org.javatari.general.m6502.Register.*;

public final class Txx extends Instruction {

	public Txx(M6502 cpu, int src, int dest) {
		super(cpu);
		this.source = src;
		this.dest = dest;
	}

	@Override
	public int fetch() {
		return 2;
	}

	@Override
	public void execute() {
		final byte val;
		if (source == Register.rA) 			val = cpu.A;
		else if (source == Register.rX) 	val = cpu.X;
		else if (source == Register.rY) 	val = cpu.Y;
		else if (source == Register.rSP) 	val = cpu.SP;
		else throw new IllegalStateException("Txx Invalid Source Register: " + source);

		if (dest == Register.rA) 		cpu.A = val;
		else if (dest == Register.rX) 	cpu.X = val;
		else if (dest == Register.rY) 	cpu.Y = val;
		else if (dest == Register.rSP) 	cpu.SP = val;
		else throw new IllegalStateException("Txx Invalid Destination Register: " + dest);

		if (dest != rSP) {		// Does not affect Status Bits when transferring to SP
			cpu.ZERO = val == 0;
			cpu.NEGATIVE = val < 0;
		}
	}

	private final int source;
	private final int dest;
	

	public static final long serialVersionUID = 1L;

}
