// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;

/**
 * Implements as Enhanced version of Pitfall2 with TIA audio updates every DPC audio clock!
 */
public class CartridgePitfall2EnhancedDPCAudio extends Cartridge10K_DPC {

	private CartridgePitfall2EnhancedDPCAudio(byte[] content, String contentName) {
		super(content, contentName, FORMAT);
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
		public Cartridge create(byte[] content, String contentName) {
			return new CartridgePitfall2EnhancedDPCAudio(content, contentName);
		}
		@Override
		public CartridgeFormatOption getOption(byte content[], String contentName) {
			if (content.length < ROM_SIZE || content.length > ROM_SIZE + SIZE_TOLERANCE) return null;
			return new CartridgeFormatOptionHinted(111, this, contentName);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}

