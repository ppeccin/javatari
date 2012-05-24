package pc.cartridge;

import general.av.video.VideoStandard;
import atari.cartridge.Cartridge;
import atari.cartridge.Cartridge4K;
import atari.cartridge.bankswitching.Cartridge12K;
import atari.cartridge.bankswitching.Cartridge16K;
import atari.cartridge.bankswitching.Cartridge28K;
import atari.cartridge.bankswitching.Cartridge32K;
import atari.cartridge.bankswitching.Cartridge8K;
import atari.cartridge.bankswitching.Cartridge8KSliced;

public class CartridgeCreator {

	// TODO Find a better way to identify the type of bank switching and the VideoStandard of Cartridges
	static Cartridge create(byte[] content, String name) throws UnsupportedROMFormatException {
		String cartName = name.toUpperCase();
		Cartridge cart = null; 
		// Special case for Sliced "E0" format as indicated in name
		if (cartName.indexOf("(SLICED)") >= 0 || cartName.indexOf("(E0)") >= 0) {
			if (content.length == Cartridge8KSliced.SIZE) 
				cart = new Cartridge8KSliced(content);
			else 
				throw new UnsupportedROMFormatException("ROM (SLICED, E0) size not supported: " + content.length);
		} else {
			// Force SuperChip mode ON or OFF as indicated in name, otherwise leave it in auto mode (null)
			Boolean sc = null;
			if (cartName.indexOf("(SC)") >= 0) sc = true;
			else if (cartName.indexOf("(NOSC)") >= 0) sc = false;
			switch (content.length) {
				case 0:
					return null;
				case Cartridge4K.HALF_SIZE:
				case Cartridge4K.SIZE:
					cart = new Cartridge4K(content); break;
				case Cartridge8K.SIZE:
					cart = new Cartridge8K(content, sc); break;
				case Cartridge12K.SIZE:
					cart = new Cartridge12K(content); break;
				case Cartridge16K.SIZE:
					cart = new Cartridge16K(content, sc); break;
				case Cartridge28K.SIZE:
					cart = new Cartridge28K(content); break;
				case Cartridge32K.SIZE:
					cart = new Cartridge32K(content, sc); break;
				default:
					throw new UnsupportedROMFormatException("ROM size not supported: " + content.length);
			}
		}
		// Use VideoStandard specified on the name. Default is null (auto)
		if (cartName.indexOf("(PAL)") >= 0) cart.suggestedVideoStandard(VideoStandard.PAL);
		else if (cartName.indexOf("(NTSC)") >= 0) cart.suggestedVideoStandard(VideoStandard.NTSC);
		return cart;
	}

}
