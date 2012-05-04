// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;
import general.m6502.Register;

public class INx extends Instruction {

	public INx(M6502 cpu, Register type) {
		super(cpu);
		this.reg = type;
	}

	@Override
	public int fetch() {
		return 2;
	}

	@Override
	public void execute() {
		final byte val;
		switch (reg) {
			case rX:
				val = (byte) (cpu.X + 1); 
				cpu.X = val;
				break;
			case rY:
				val = (byte) (cpu.Y + 1); 
				cpu.Y = val;
				break;
			default:
				throw new IllegalStateException("INx Invalid Register: " + reg);
		}
		cpu.ZERO = val == 0;
		cpu.NEGATIVE = val < 0;
	}

	private final Register reg;
	

	public static final long serialVersionUID = 1L;

}
