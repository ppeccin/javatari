// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.cartridge;

import java.util.ArrayList;

import atari.cartridge.Cartridge;
import atari.cartridge.CartridgeFormatOption;
import atari.cartridge.formats.CartridgeDatabase;

public final class CartridgeCreator {

	public static Cartridge create(byte[] content, String contentName) throws UnsupportedROMFormatException {
		// Build the Cartridge if a supported format is found
		Cartridge cart = findBestFormatAndCreate(content, contentName); 
		
		// By now, always use auto mode
		// Use VideoStandard specified on the name. Default is null (auto)
		// if (cartName.indexOf("(PAL)") >= 0) cart.suggestedVideoStandard(VideoStandard.PAL);
		// else if (cartName.indexOf("(NTSC)") >= 0) cart.suggestedVideoStandard(VideoStandard.NTSC);

		return cart;
	}

	// TODO Find a better way to auto-detect the best format
	private static Cartridge findBestFormatAndCreate(byte[] content, String contentName) throws UnsupportedROMFormatException {
		ArrayList<CartridgeFormatOption> options = CartridgeDatabase.getFormatOptions(content, contentName);
		if (options.isEmpty())
			throw new UnsupportedROMFormatException("Size: " + content.length);
		
		// Choose the best option
		CartridgeFormatOption bestOption = options.get(0);
		System.out.println(bestOption);
		return bestOption.format.create(content, contentName);
	}

}
