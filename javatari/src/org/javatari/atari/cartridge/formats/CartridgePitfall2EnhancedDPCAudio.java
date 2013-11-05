// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.cartridge.ROM;

/**
 * Implements an Enhanced version of Pitfall2 with TIA audio updates every DPC audio clock!
 */
public class CartridgePitfall2EnhancedDPCAudio extends Cartridge10K_DPC {

	private CartridgePitfall2EnhancedDPCAudio(ROM rom) {
		super(rom, FORMAT);
	}

	@Override
	public void clockPulse() {
		super.clockPulse();
		if (!audioChanged) return;
		// Send a volume update do TIA Audio Channel 0
		updateAudioOutput();
		bus.tia.writeByte(0x19, audioOutput);	
	}
	
	public static final CartridgeFormat FORMAT = new CartridgeFormat("DPCa", "10K DPC (Pitfall 2 Enhanced Audio)") {
		@Override
		public Cartridge createCartridge(ROM rom) {
			return new CartridgePitfall2EnhancedDPCAudio(rom);
		}
		@Override
		public CartridgeFormatOption getOption(ROM rom) {
			if (rom.content.length < ROM_SIZE || rom.content.length > ROM_SIZE + SIZE_TOLERANCE) return null;
			return new CartridgeFormatOption(111, this, rom);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}

