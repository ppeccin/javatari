// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge;

public interface CartridgeSocket extends CartridgeInsertionListener {

	public void insert(Cartridge cartridge, boolean autoPowerControl);

	public Cartridge inserted();
	
	public void addInsertionListener(CartridgeInsertionListener listener);
	public void removeInsertionListener(CartridgeInsertionListener listener);
	
}
