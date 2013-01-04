// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge;

import java.io.Serializable;


public abstract class CartridgeFormat implements Serializable {

	public CartridgeFormat(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	public Cartridge create(Cartridge cartridge) {
		return create(cartridge.content(), cartridge.contentName());
	}
	
	public abstract Cartridge create(byte[] content, String contentName);

	public abstract CartridgeFormatOption getOption(byte content[], String contentName);
	
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
