// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;


import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;

public final class BRK extends Instruction {

	public BRK(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {
		// BRK requires one extra unused byte after the opcode, as per specification
		// Lets use this byte as a parameter for debug purposes!
		par = M6502.toUnsignedByte(cpu.bus.readByte(cpu.fetchImmediateAddress()));	// This acts like a dummy PC read and increment

		return 7;
	}

	@Override
	public void execute() {
		cpu.debug(">>> BREAK: " + par);
		cpu.pushWord(cpu.PC);		
		cpu.pushByte(cpu.PS());
		cpu.PC = cpu.memoryReadWord(M6502.IRQ_HANDLER_ADDRESS);
	}

	private int par;
	

	public static final long serialVersionUID = 1L;
	
}
