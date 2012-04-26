// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge;

import utils.Randomizer;

/**
 * Implements no cartridge connected
 */
public final class CartridgeDisconnected extends Cartridge {

	public CartridgeDisconnected() {
		super();
	}

	@Override
	public byte readByte(int address) {
		return (byte) (Randomizer.instance.nextInt());
	}

	public static final int SIZE = 0;

	private static final long serialVersionUID = 1L;

}

