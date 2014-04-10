// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.console.savestate;

public interface SaveStateSocket {

	public void connectMedia(SaveStateMedia media);

	public SaveStateMedia media();
	
	public void externalStateChange();

	public void saveStateFile();
	
}
