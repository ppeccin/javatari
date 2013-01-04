// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;

public final class JSR extends Instruction {

	public JSR(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {
		ea = cpu.fetchImmediateAddress();
		return 6;
	}

	@Override
	public void execute() {
		byte PCL = cpu.memory.readByte(ea);
		// Does not perform the dummy stack read
		cpu.pushWord((char) (cpu.PC));				// JSR should push the return address - 1
		byte PCH = cpu.memory.readByte(ea + 1);
		cpu.PC = (char) ((M6502.toUnsignedByte(PCH) << 8) + M6502.toUnsignedByte(PCL));
	}

	private char ea;

	
	public static final long serialVersionUID = 1L;
	
}
