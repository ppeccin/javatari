// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.UndocumentedInstruction;

public class uANE extends UndocumentedInstruction {

	public uANE(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {
		ea = cpu.fetchImmediateAddress(); return 2;		
	}

	@Override
	public void execute() {
		cpu.memory.readByte(ea);
		// Exact operation unknown. Lets do nothing!
		cpu.debug(">>> Undocumented opcode ANE (XAA)");
	}

	private int ea;
	

	private static final long serialVersionUID = 1L;

}
