// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.m6502;

import java.io.Serializable;

public abstract class Instruction implements Serializable {

	public Instruction(M6502 cpu) {
		this.cpu = cpu;
	}

	public abstract int fetch();

	public abstract void execute();

	protected transient M6502 cpu;

	
	private static final long serialVersionUID = 1L;

}
