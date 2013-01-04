// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge.formats;

import atari.cartridge.CartridgeFormat;
import atari.cartridge.CartridgeFormatOption;

public final class CartridgeFormatOptionHinted extends CartridgeFormatOption {

	CartridgeFormatOptionHinted(int priority, CartridgeFormat format, String contentName) {
		super(CartridgeDatabase.priorityBoosted(priority, format ,contentName), format);
	}

}
