// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;

public class RTS extends Instruction {

	public RTS(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {
		return 6;
	}

	@Override
	public void execute() {
		cpu.PC = (char) (cpu.pullWord() + 1); 
	}
	

	public static final long serialVersionUID = 1L;

}
