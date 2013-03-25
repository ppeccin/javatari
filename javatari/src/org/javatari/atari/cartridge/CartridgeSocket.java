// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge;

public interface CartridgeSocket extends CartridgeInsertedListener {

	public void insert(Cartridge cartridge, boolean autoPowerControl);

	public Cartridge inserted();
	
	public void addCartridgeInsertedListener(CartridgeInsertedListener listener);
	public void removeCartridgeInsertedListener(CartridgeInsertedListener listener);
	
}
