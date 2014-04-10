// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.console.savestate;

public interface SaveStateMedia {

	public boolean saveStateFile(ConsoleState state);

	public boolean saveState(int slot, ConsoleState state);
	public ConsoleState loadState(int slot);
	
	public boolean saveResourceToFile(String name, Object data);
	public Object loadResourceFromFile(String name);
	
}
