// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;

public final class CartridgeFormatOptionHinted extends CartridgeFormatOption {

	CartridgeFormatOptionHinted(int priority, CartridgeFormat format, String contentName) {
		super(CartridgeDatabase.priorityBoosted(priority, format ,contentName), format);
	}

}
