// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge;

import java.util.ArrayList;


public final class CartridgeCreator {

	public static Cartridge create(ROM rom) throws ROMFormatUnsupportedException {
		// Build the Cartridge if a supported format is found
		ArrayList<CartridgeFormatOption> options = CartridgeDatabase.getFormatOptions(rom);
		if (options.isEmpty())
			throw new ROMFormatUnsupportedException("Size: " + rom.content.length);
		
		// Choose the best option
		CartridgeFormatOption bestOption = options.get(0);
		System.out.println(bestOption);
		return bestOption.format.createCartridge(rom);
	}

}
