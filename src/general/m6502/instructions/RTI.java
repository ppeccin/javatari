// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;

public final class RTI extends Instruction {

	public RTI(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {
		return 6;
	}

	@Override
	public void execute() {
		// Does not perform the dummy PC + 1 read
		// Does not perform the dummy stack read
		cpu.PS(cpu.pullByte());
		cpu.PC = cpu.pullWord();
	}
	

	public static final long serialVersionUID = 1L;

}
