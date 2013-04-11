// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge;

import java.io.Serializable;


public abstract class CartridgeFormat implements Serializable {

	public CartridgeFormat(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	public abstract Cartridge createCartridge(ROM rom);

	public abstract CartridgeFormatOption getOption(ROM rom);
	
	@Override
	public String toString() {
		return id + ": " + name;
	}
	
		
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CartridgeFormat)) return false;
		return id.equals(((CartridgeFormat) obj).id);
	}

	
	public final String id;
	public final String name;

	
	private static final long serialVersionUID = 1L;
	
}
