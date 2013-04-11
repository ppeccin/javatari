// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge;


public class CartridgeFormatOption implements Comparable<CartridgeFormatOption> {

	public CartridgeFormatOption(int priority, CartridgeFormat format, ROM rom) {
		super();
		this.priority = priority;
		this.format = format;
	}

	@Override
	public int compareTo(CartridgeFormatOption o) {
		return new Integer(priority).compareTo(new Integer(o.priority));
	}

	@Override
	public String toString() {
		return "Format: " + format + ", priority: " + priority;
	}
	
	
	public int priority;
	public final CartridgeFormat format;

}
