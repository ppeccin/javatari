// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502.instructions;

import general.m6502.M6502;
import general.m6502.UndocumentedInstruction;

public class uSBX extends UndocumentedInstruction {

	public uSBX(M6502 cpu) {
		super(cpu);
	}

	@Override
	public int fetch() {
		ea = cpu.fetchImmediateAddress(); return 2;		
	}

	@Override
	public void execute() {
		byte b = (byte) (cpu.A & cpu.X);
		int uB = M6502.toUunsignedByte(b);
		int uVal = cpu.memory.unsignedByte(ea); 
		byte newX = (byte)(uB - uVal);
		cpu.X = newX;
		
		cpu.CARRY = uB >= uVal;
		cpu.ZERO = newX == 0;
		cpu.NEGATIVE = newX < 0;
	}

	private int ea;
	

	private static final long serialVersionUID = 1L;

}
