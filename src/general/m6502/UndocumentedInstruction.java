// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502;


public abstract class UndocumentedInstruction extends Instruction {

	public UndocumentedInstruction(M6502 cpu) {
		super(cpu);
	}

	@Override 
	public void execute() {
		// cpu.debug(">>> ILLEGAL OPCODE: " + String.format("%02x", opcode));
	}


	public static final long serialVersionUID = 1L;

}
