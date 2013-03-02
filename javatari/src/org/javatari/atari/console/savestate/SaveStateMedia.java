// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.console.savestate;

public interface SaveStateMedia {

	public boolean save(int slot, ConsoleState state);

	public ConsoleState load(int slot);
	
}
