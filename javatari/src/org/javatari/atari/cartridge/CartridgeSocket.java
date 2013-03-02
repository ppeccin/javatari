// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge;

public interface CartridgeSocket {

	public void insert(Cartridge cartridge, boolean autoPowerControl);
	public Cartridge inserted();
	
}
