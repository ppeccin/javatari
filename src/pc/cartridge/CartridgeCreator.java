package pc.cartridge;

import general.av.video.VideoStandard;
import atari.cartridge.Cartridge;
import atari.cartridge.Cartridge4K;
import atari.cartridge.bankswitching.Cartridge12K;
import atari.cartridge.bankswitching.Cartridge16K;
import atari.cartridge.bankswitching.Cartridge28K;
import atari.cartridge.bankswitching.Cartridge32K;
import atari.cartridge.bankswitching.Cartridge64K;
import atari.cartridge.bankswitching.Cartridge8K;
import atari.cartridge.bankswitching.Cartridge8KSliced;

public class CartridgeCreator {

	// TODO Find a better way to identify the type of bank switching
	static Cartridge create(byte[] content, String name) throws UnsupportedROMFormatException {
		String cartName = name.toUpperCase();

		// Forced SuperChip mode ON or OFF as indicated in name, otherwise leave it in auto mode (null)
		Boolean superChip = null;
		if (cartName.indexOf("(SC)") >= 0) superChip = true;
		else if (cartName.indexOf("(NOSC)") >= 0) superChip = false;

		// Sliced "E0" format as indicated in name
		boolean sliced = cartName.indexOf("(SLICED)") >= 0 || cartName.indexOf("(E0)") >= 0;

		// Build the Cartridge if a supported format is found
		Cartridge cart = findTypeAndCreate(content, superChip, sliced); 

		// Use VideoStandard specified on the name. Default is null (auto)
		if (cartName.indexOf("(PAL)") >= 0) cart.suggestedVideoStandard(VideoStandard.PAL);
		else if (cartName.indexOf("(NTSC)") >= 0) cart.suggestedVideoStandard(VideoStandard.NTSC);

		return cart;
	}

	private static Cartridge findTypeAndCreate(byte[] content, Boolean superChip, boolean sliced) throws UnsupportedROMFormatException {
		if (content.length == 0) return null;
		if (Cartridge4K.accepts(content, superChip, sliced)) return new Cartridge4K(content);
		if (Cartridge8K.accepts(content, superChip, sliced)) return new Cartridge8K(content, superChip);
		if (Cartridge8KSliced.accepts(content, superChip, sliced)) return new Cartridge8KSliced(content);
		if (Cartridge12K.accepts(content, superChip, sliced)) return new Cartridge12K(content);
		if (Cartridge16K.accepts(content, superChip, sliced)) return new Cartridge16K(content, superChip);
		if (Cartridge28K.accepts(content, superChip, sliced)) return new Cartridge28K(content);
		if (Cartridge32K.accepts(content, superChip, sliced)) return new Cartridge32K(content, superChip);
		if (Cartridge64K.accepts(content, superChip, sliced)) return new Cartridge64K(content, superChip);
		throw new UnsupportedROMFormatException(
				"Size: " + content.length + 
				", SuperChip: " + (superChip == null ? "auto" : superChip ? "yes" : "no") +
				", Sliced: " + (sliced ? "yes" : "no"));
	}

}
