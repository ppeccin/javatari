// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge;

import java.io.Serializable;


public class ROM implements Serializable {

	public ROM(String source, byte[] content) {
		super();
		this.source = source;
		this.content = content;
		this.info = CartridgeDatabase.produceInfo(this);
	}

	public final String source;
	public final byte[] content;
	public final CartridgeInfo info;


	private static final long serialVersionUID = 1L;
	
}
