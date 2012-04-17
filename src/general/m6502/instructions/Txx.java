// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;
import general.m6502.Register;

import static general.m6502.Register.*;

public class Txx extends Instruction {

	public Txx(M6502 cpu, Register source, Register dest) {
		super(cpu);
		this.source = source;
		this.dest = dest;
	}

	@Override
	public int fetch() {
		return 2;
	}

	@Override
	public void execute() {
		final byte val;
		switch (source) {
			case rA:
				val = cpu.A; break;
			case rX:
				val = cpu.X; break;
			case rY:
				val = cpu.Y; break;
			case rSP:
				val = cpu.SP; break;
			default:
				throw new IllegalStateException("Tx Invalid Source Register: " + source);
		}
		switch (dest) {
			case rA:
				cpu.A = val; break;
			case rX:
				cpu.X = val; break;
			case rY:
				cpu.Y = val; break;
			case rSP:
				cpu.SP = val; break;
			default:
				throw new IllegalStateException("Tx Invalid Destination Register: " + dest);
		}
		if (dest != rSP) {		// Does not affect Status Bits when transferring to SP
			cpu.ZERO = val == 0;
			cpu.NEGATIVE = val < 0;
		}
	}

	private final Register source;
	private final Register dest;
	

	private static final long serialVersionUID = 1L;

}
