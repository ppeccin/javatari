// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.Instruction;
import general.m6502.OperandType;

public class JMP extends Instruction {

	public JMP(M6502 cpu, OperandType type) {
		super(cpu);
		this.type = type;
	}

	@Override
	public int fetch() {
		switch (type) {
			case ABS:
				newPC = (char) cpu.fetchAbsoluteAddress(); return 3;
			case IND:
				newPC = (char) cpu.fetchIndirectAddress(); return 5;
			default:
				throw new IllegalStateException("JMP Invalid Operand Type: " + type);
		}
	}

	@Override
	public void execute() {
		cpu.PC = newPC; 
	}

	private final OperandType type;

	private char newPC;
	

	private static final long serialVersionUID = 1L;
	
}
