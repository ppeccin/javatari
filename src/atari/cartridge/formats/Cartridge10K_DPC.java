// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.cartridge.formats;

import atari.cartridge.Cartridge;
import atari.cartridge.CartridgeFormat;
import atari.cartridge.CartridgeFormatOption;
import atari.controls.ConsoleControls.Control;

/**
 * Implements the 8K + 2K "DPC" (Pitfall2) format
 */
public class Cartridge10K_DPC extends CartridgeBankedByMaskedRange {

	protected Cartridge10K_DPC(byte[] content, String contentName, CartridgeFormat format) {
		super(content, contentName, format, BASE_BANKSW_ADDRESS, false, 0);		// SuperChip always OFF, no RAM
	}

	@Override
	public boolean needsClock() {
		return true;
	}

	@Override
	public byte readByte(int address) {
		maskAddress(address);
		if (maskedAddress <= 0x03f || (maskedAddress >= 0x800 && maskedAddress <= 0x83f))	// DPC register read
			return readDPCRegister(maskedAddress & 0x00ff);
		// Always add the correct bank offset
		return bytes[bankAddressOffset + maskedAddress];	// ROM	
	}

	@Override
	public void writeByte(int address, byte b) {
		maskAddress(address);
		if ((maskedAddress >= 0x040 && maskedAddress <= 0x07f) ||
			(maskedAddress >= 0x840 && maskedAddress <= 0x87f))		// DPC register write
			writeDPCRegister(maskedAddress & 0x00ff, b);
	}
	
	@Override
	public void clockPulse() {
		if (audioClockCounter-- > 0) return;
		audioClockCounter = audioClockDivider;
		for (int f = 5; f <= 7; f++) {
			if (!audioMode[f]) continue;
			fetcherPointer[f]--;
			if ((fetcherPointer[f] & 0x00ff) == 0xff) 
				setFetcherPointer(f, fetcherPointer[f] & 0xff00 | fetcherStart[f]);
			updateFetcherMask(f);
			if (!audioChanged) audioChanged = true;
		}
		
	}

	@Override
	public void controlStateChanged(Control control, boolean state) {
		if (!state) return;
		switch (control) {
			case CARTRIDGE_CLOCK_DEC:
				if (audioClockDivider < 150) audioClockDivider++; 
				showClockOSD();	break;
			case CARTRIDGE_CLOCK_INC:
				if (audioClockDivider > 1) audioClockDivider--;
				showClockOSD(); break;
		}
	}

	private byte readDPCRegister(int reg) {
		// Random number
		if (reg >= 0x00 && reg <= 0x03) {
			clockRandomNumber(); 
			return randomNumber; 
		}
		// Audio value (MOVAMT not supported)
		if (reg >= 0x04 && reg <= 0x07) {
			if (audioChanged) updateAudioOutput();
			return audioOutput;
		}
		// Fetcher unmasked value
		if (reg >= 0x08 && reg <= 0x0f) {
			byte res = bytes[DPC_ROM_END - fetcherPointer[reg - 0x08]];
			clockFetcher(reg - 0x08);
			return res;
		}
		// Fetcher masked value
		if (reg >= 0x10 && reg <= 0x17) {
			byte res = (byte) (bytes[DPC_ROM_END - fetcherPointer[reg - 0x10]] & fetcherMask[reg - 0x10]);
			clockFetcher(reg - 0x10);
			return res;
		}
		// Fetcher masked value, nibbles swapped
		if (reg >= 0x18 && reg <= 0x1f) {
			byte res = (byte) (bytes[DPC_ROM_END - fetcherPointer[reg - 0x18]] & fetcherMask[reg - 0x18]);
			clockFetcher(reg - 0x18);
			res = (byte) ((res & 0x0f << 4) | (res & 0xf0 >> 4)); 
			return res;
		}
		// Fetcher masked value, byte reversed
		if (reg >= 0x20 && reg <= 0x27) {
			byte res = (byte) (bytes[DPC_ROM_END - fetcherPointer[reg - 0x20]] & fetcherMask[reg - 0x20]);
			clockFetcher(reg - 0x20);
			res = (byte) (Integer.reverse(res) >>> (Integer.SIZE - Byte.SIZE));
			return res;
		}
		// Fetcher masked value, byte rotated right
		if (reg >= 0x28 && reg <= 0x2f) {
			byte res = (byte) (bytes[DPC_ROM_END - fetcherPointer[reg - 0x28]] & fetcherMask[reg - 0x28]);
			clockFetcher(reg - 0x28);
			res = (byte) (((res >>> 1) | (res << 7)) & 0xff);
			return res;
		}
		// Fetcher masked value, byte rotated left
		if (reg >= 0x30 && reg <= 0x37) {
			byte res = (byte) (bytes[DPC_ROM_END - fetcherPointer[reg - 0x30]] & fetcherMask[reg - 0x30]);
			clockFetcher(reg - 0x30);
			res = (byte) (((res << 1) | ((res >> 7) & 0x01)) & 0xff);
			return res;
		}
		// Fetcher mask
		if (reg >= 0x38 && reg <= 0x3f) {
			return fetcherMask[reg - 0x38];
		}
		return 0;
	}

	private void writeDPCRegister(int reg, byte b) {
		// Fetchers Start
		if (reg >= 0x40 && reg <= 0x47) {
			int f = reg - 0x40;
			fetcherStart[f] = b;
 			if ((byte)(fetcherPointer[f] & 0xff) == fetcherStart[f]) fetcherMask[f] = (byte)0xff;
 			return;	 
		}
		// Fetchers End
		if (reg >= 0x48 && reg <= 0x4f) {
			fetcherEnd[reg - 0x48] = b; fetcherMask[reg - 0x48] = (byte)0x00; return;	 
		}
		// Fetchers Pointers LSB
		if (reg >= 0x50 && reg <= 0x57) {
			setFetcherPointer(reg - 0x50, (fetcherPointer[reg - 0x50] & 0xff00) | (b & 0xff)); return;			// LSB
		}
		// Fetchers 0-3 Pointers MSB
		if (reg >= 0x58 && reg <= 0x5b) {
			setFetcherPointer(reg - 0x58, (fetcherPointer[reg - 0x58] & 0x00ff) | ((b & (0x07)) << 8)); return;	// MSB bits 0-2
		}
		// Fetchers 4 Pointers MSB (Draw Line enable not supported)
		if (reg == 0x5c) {
			setFetcherPointer(4, (fetcherPointer[4] & 0x00ff) | ((b & (0x07)) << 8));							// MSB bits 0-2 
			return;
		}
		// Fetchers 5-7 Pointers MSB and Audio Mode enable
		if (reg >= 0x5d && reg <= 0x5f) {
			setFetcherPointer(reg - 0x58, (fetcherPointer[reg - 0x58] & 0x00ff) + ((b & (0x07)) << 8));			// MSB bits 0-2
			audioMode[reg - 0x58] = (b & 0x10) != 0;
			return;
		}
		// Draw Line MOVAMT value (not supported)
		if (reg >= 0x60 && reg <= 0x67) {
			return;
		}
		// 0x68 - 0x6f Not used
		// Random Number reset
		if (reg >= 0x70 && reg <= 0x77) {
			randomNumber = (byte) 0x00; return;
		}
		// 0x78 - 0x7f Not used
	}

	private void setFetcherPointer(int f, int pointer) {
		fetcherPointer[f] = pointer;
	}

	private void clockFetcher(int f) {
		int newPointer = fetcherPointer[f] - 1;
		if (newPointer < 0 ) newPointer = 0x07ff;
		setFetcherPointer(f, newPointer);
		updateFetcherMask(f); 
	}

	private void updateFetcherMask(int f) {
		byte lsb = (byte)(fetcherPointer[f] & 0xff);
		if (lsb == fetcherStart[f]) fetcherMask[f] = (byte)0xff;
		else if (lsb == fetcherEnd[f]) fetcherMask[f] = (byte)0x00;
	}

	private void clockRandomNumber() {
		randomNumber = (byte) ((randomNumber << 1) | 
				(~((randomNumber >> 7) ^ (randomNumber >> 5) ^ 
				(randomNumber >> 4) ^ (randomNumber >> 3)) & 0x01));
		if (randomNumber == (byte) 0xff) randomNumber = 0;
	}

	protected void updateAudioOutput() {
		audioOutput = AUDIO_MIXED_OUTPUT[
		    (audioMode[5] ? fetcherMask[5] & 0x04 : 0) | 
		    (audioMode[6] ? fetcherMask[6] & 0x02 : 0) | 
		    (audioMode[7] ? fetcherMask[7] & 0x01 : 0)];
		audioChanged = false;
	}

	private void showClockOSD() {
		bus.tia.videoOutput().showOSD(
			String.format("DPC Audio Clock: ~%.1f kHz", 1194720f / audioClockDivider / 1000), true);
	}

	
	private byte randomNumber = (byte) 0x00;
	private final int[] fetcherPointer = new int[8];
	private final byte[] fetcherStart = new byte[8];
	private final byte[] fetcherEnd = new byte[8];
	private final byte[] fetcherMask = new byte[8];
	private final boolean[] audioMode = new boolean[8];
	private int audioClockDivider = AUDIO_CLOCK_DEFAULT_DIVIDER;
	private int audioClockCounter = 0;
	protected boolean audioChanged = true;
	protected byte audioOutput = 0;
	

	private static final int AUDIO_CLOCK_DEFAULT_DIVIDER = 60;
	private static final byte[] AUDIO_MIXED_OUTPUT = new byte[] { 0x0, 0x5, 0x5, 0xa, 0x5, 0xa, 0xa, 0xf };
	//	private static final byte[] AUDIO_MIXED_OUTPUT = new byte[] { 0x0, 0x4, 0x5, 0x9, 0x6, 0xa, 0xb, 0xf };   // Per specification
	
	protected static final int ROM_SIZE = 8192 + 2048;
	protected static final int DPC_ROM_END = 8192 + 2048 - 1;
	protected static final int SIZE_TOLERANCE = 256;		// Pitfall2 ROMs have 255 extra bytes for the random number stream
	protected static final int BASE_BANKSW_ADDRESS = 0x0ff8;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("DPC", "10K DPC (Pitfall 2)") {
		@Override
		public Cartridge create(byte[] content, String contentName) {
			return new Cartridge10K_DPC(content, contentName, this);
		}
		@Override
		public CartridgeFormatOption getOption(byte content[], String contentName) {
			if (content.length < ROM_SIZE || content.length > ROM_SIZE + SIZE_TOLERANCE) return null;
			return new CartridgeFormatOptionHinted(101, this, contentName);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}

