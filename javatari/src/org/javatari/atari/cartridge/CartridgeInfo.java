// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge;

import java.io.Serializable;

public class CartridgeInfo implements Serializable {
	
	public String name;
	public String label;
	public int labelColor = -1;
	public int labelBackColor = -1;
	public int labelBorderColor = -1;
	public int paddles = -1;
	public int crtMode = -1;
	public String format;
	public String hash;
	

	private static final long serialVersionUID = 1L;
	
}
