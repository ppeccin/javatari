// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;
import general.m6502.OperandType;

public final class JMP extends Instruction {

	public JMP(M6502 cpu, int type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {
		if (type == OperandType.ABS) { newPC = cpu.fetchAbsoluteAddress(); return 3; }
		if (type == OperandType.IND) { newPC = cpu.fetchIndirectAddress(); return 5; }
		throw new IllegalStateException("JMP Invalid Operand Type: " + type);
	}

	@Override
	public void execute() {
		cpu.PC = newPC; 
	}

	private final int type;

	private int newPC;
	

	public static final long serialVersionUID = 1L;
	
}
