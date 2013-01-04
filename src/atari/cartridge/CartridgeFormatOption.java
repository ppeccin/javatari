// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge;


public class CartridgeFormatOption implements Comparable<CartridgeFormatOption> {

	public CartridgeFormatOption(int priority, CartridgeFormat format) {
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
