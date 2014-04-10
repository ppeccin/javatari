// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.javatari.atari.board.BUS;
import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.cartridge.ROM;
import org.javatari.atari.cartridge.ROMFormatDenialDetailException;
import org.javatari.utils.Randomizer;

/**
 * Implements the n * 8448 byte "AR" Arcadia/Starpath/Supercharger tape format
 */
public final class Cartridge8K_64K_AR extends CartridgeBankedByBusMonitoring {

	private Cartridge8K_64K_AR(ROM rom) {
		super(rom, FORMAT);
		// Cannot use the contents of the ROM directly, as cartridge is all RAM and can be modified
		// Also, ROM content represents the entire tape and can have multiple parts
		bytes = new byte[4 * BANK_SIZE]; 
		loadBIOSFromFile();
	}

	@Override
	public void powerOn() {
		// Always start with bank configuration 000 (bank2, bank3 = BIOS ROM), writes disabled and BIOS ROM powered on
		setControlRegister((byte) 0x00);
		// Rewind Tape
		tapeOffset = 0;
		// BIOS will ask to load Part Number 0 at System Reset
	}

	@Override
	public void connectBus(BUS bus) {
		this.bus = bus;
	}

	@Override
	public byte readByte(int address) {
		// maskedAddress already set on bus monitoring method
		// bank0
		if (maskedAddress < BANK_SIZE)
			return bytes[bank0AddressOffset + maskedAddress];
		else 
			// bank1
			return bytes[bank1AddressOffset + maskedAddress - BANK_SIZE];
	}

	@Override
	public void writeByte(int address, byte b) {
		// No direct writes are possible
		// But check for BIOS Load Part Hotspot range
		if (bank1AddressOffset == BIOS_BANK_OFFSET &&
				maskedAddress >= BIOS_INT_EMUL_LOAD_HOTSPOT && maskedAddress < BIOS_INT_EMUL_LOAD_HOTSPOT + 256) {
			loadPart(maskedAddress - BIOS_INT_EMUL_LOAD_HOTSPOT);
			return;
		}
	}
	
	@Override
	protected void performBankSwitchOnMonitoredAccess(int address) {
		maskAddress(address);
		address &= 0x1fff;
		
		// Set ControlRegister if the hotspot was triggered
		if (address == 0x1ff8) {
			setControlRegister(valueToWrite);
			return;
		}

		// Check for writes pending and watch for address transitions
		if (addressChangeCountdown > 0) {
			if (address != lastAddress) {
				lastAddress = address;
				if (--addressChangeCountdown == 0) {
					// 5th address transition detected, perform write
					if ((address & CHIP_MASK) == CHIP_SELECT) {		// Do not write outside Cartridge range
						// bank0
						if (maskedAddress < BANK_SIZE)
							bytes[bank0AddressOffset + maskedAddress] = valueToWrite;
						// bank1
						else if (bank1AddressOffset < BIOS_BANK_OFFSET)	// Do not write to BIOS ROM
							bytes[bank1AddressOffset + maskedAddress - BANK_SIZE] = valueToWrite;
					}
				} 
			}
			return;
		}

		// Check and store desired value to write
		if (((address & CHIP_MASK) == CHIP_SELECT) && maskedAddress <= 0x00ff) {
			valueToWrite = (byte) maskedAddress;
			if (writeEnabled) {
				lastAddress = address;		// Will be watched for the 5th address change
				addressChangeCountdown = 5;
			}
		}
	}

	private void setControlRegister(byte controlRegister) {
		int banksConfig = (controlRegister >> 2) & 0x07;
		switch (banksConfig) {
			case 00: bank0AddressOffset = 2 * BANK_SIZE; bank1AddressOffset = BIOS_BANK_OFFSET; break;
			case 01: bank0AddressOffset = 0 * BANK_SIZE; bank1AddressOffset = BIOS_BANK_OFFSET; break;
			case 02: bank0AddressOffset = 2 * BANK_SIZE; bank1AddressOffset = 0 * BANK_SIZE; break;	// as used in Commie Mutants and many others
			case 03: bank0AddressOffset = 0 * BANK_SIZE; bank1AddressOffset = 2 * BANK_SIZE; break;	// as used in Suicide Mission
			case 04: bank0AddressOffset = 2 * BANK_SIZE; bank1AddressOffset = BIOS_BANK_OFFSET; break;
			case 05: bank0AddressOffset = 1 * BANK_SIZE; bank1AddressOffset = BIOS_BANK_OFFSET; break;
			case 06: bank0AddressOffset = 2 * BANK_SIZE; bank1AddressOffset = 1 * BANK_SIZE; break;	// as used in Killer Satellites
			case 07: bank0AddressOffset = 1 * BANK_SIZE; bank1AddressOffset = 2 * BANK_SIZE; break;	// as we use for 2k/4k ROM cloning		}
			default: throw new IllegalStateException("Invalid bank configuration");
		}
		addressChangeCountdown = 0;	// Setting ControlRegister cancels any pending write
		writeEnabled = (controlRegister & 0x02) != 0;
		biosRomPower = (controlRegister & 0x01) == 0;
		// System.out.println("Banks: " + banksConfig + ", Writes: " + (writeEnabled ? "ON" : "OFF"));
	}

	private void loadPart(int part) {
		boolean tapeRewound = false;
		do {
			// Check for tape end
			if (tapeOffset > rom.content.length - 1) {
				// Check if tape was already rewound once to avoid infinite tries
				if (tapeRewound) {
					if (part == 0) bus.tia.videoOutput().showOSD("Could not load Tape from Start. Not a Start Tape ROM!", true);
					else bus.tia.videoOutput().showOSD("Could not find next Part to load in Tape!", true);
					signalPartLoadedOK(false);		// BIOS will handle this
					return;
				}
				// Rewind tape
				tapeOffset = 0;
				tapeRewound = true;
			}
			// Check if the next part is the one we are looking for	
			if (peekPartNoOnTape(rom.content, tapeOffset) == part) {
				if (part == 0) bus.tia.videoOutput().showOSD("Loaded Tape from Start", true); 
				else bus.tia.videoOutput().showOSD("Loaded next Part from Tape", true);
				loadNextPart();
				return;
			} else {
				// Advance tape
				tapeOffset += PART_SIZE;
			}
		} while(true);
	}

	private void loadNextPart() {
		loadHeaderData();
		loadPagesIntoBanks();
		patchPartDataIntoBIOS();
	}

	private void loadHeaderData() {
		// Store header info
		System.arraycopy(rom.content, tapeOffset + 4 * BANK_SIZE, header, 0, header.length);
		romStartupAddress = (header[1] << 8) | (header[0] & 0xff);
		romControlRegister = header[2];
		romPageCount = header[3];
		romChecksum = header[4];
		romMultiLoadIndex = header[5];
		romProgressBarSpeed = (header[7] << 8) | (header[6] & 0xff);
		romPageOffsets = new byte[romPageCount];
		System.arraycopy(header, 16, romPageOffsets, 0, romPageCount);
	}

	private void loadPagesIntoBanks() {
		// Clear last page of first bank, as per original BIOS	
		Arrays.fill(bytes, 7 * PAGE_SIZE, 8 * PAGE_SIZE - 1, (byte)0);

		// Load pages
		int romOffset = tapeOffset;
		for (int pageInfo : romPageOffsets) {
			int bankOffset = (pageInfo & 0x03) * BANK_SIZE;
			int pageOffset = (pageInfo >> 2) * PAGE_SIZE;
			// Only write if not in BIOS area
			if (bankOffset + pageOffset + 255 < BIOS_BANK_OFFSET)
				System.arraycopy(rom.content, romOffset, bytes, bankOffset + pageOffset, PAGE_SIZE);
			romOffset += PAGE_SIZE;
		}
		// Advance tape
		tapeOffset += PART_SIZE;
	}

	private void patchPartDataIntoBIOS() {
		// Patch BIOS interface area with correct values from stored Header data
		bytes[BIOS_BANK_OFFSET + BIOS_INT_CONTROL_REG_ADDR - 0xf800] = romControlRegister;
		bytes[BIOS_BANK_OFFSET + BIOS_INT_PART_NO_ADDR - 0xf800] = romMultiLoadIndex;
		// TODO This Randomizer is a source of indeterminism. Potential problem in multiplayer sync
		bytes[BIOS_BANK_OFFSET + BIOS_INT_RANDOM_SEED_ADDR - 0xf800] = (byte) Randomizer.instance.nextInt();
		bytes[BIOS_BANK_OFFSET + BIOS_INT_START_ADDR - 0xf800] = (byte) (romStartupAddress & 0xff);
		bytes[BIOS_BANK_OFFSET + BIOS_INT_START_ADDR + 1 - 0xf800] = (byte) ((romStartupAddress >> 8) & 0xff);
		signalPartLoadedOK(true);
	}

	private void signalPartLoadedOK(boolean ok) {
		bytes[BIOS_BANK_OFFSET + BIOS_INT_PART_LOADED_OK - 0xf800] = (byte) (ok ? 1 : 0);
	}
	
	private void loadBIOSFromFile() {
		try {
			InputStream stream = getClass().getResourceAsStream("StarpathMockBios.bin");
			try {
				int totalRead = 0;
				do {
					int read;
					read = stream.read(bytes, BIOS_BANK_OFFSET + totalRead, BANK_SIZE - totalRead);
					if (read == -1) break;	// End of Stream
					totalRead += read;
				} while(totalRead < BANK_SIZE);
				if (totalRead != BANK_SIZE)
					throw new IllegalStateException("Unexpected EOF");
			} finally {
				try { stream.close(); } catch (IOException e1) {}
			}
		} catch (Exception e) {
			throw new IllegalStateException("Could not load Starpath BIOS file");
		}
	}


	private int bank0AddressOffset = 0;
	private int bank1AddressOffset = 0;
	private byte[] header = new byte[HEADER_SIZE];
	private byte valueToWrite = 0;
	private boolean writeEnabled = false;
	private int lastAddress = -1;
	private int addressChangeCountdown = 0;
	@SuppressWarnings("unused")
	private boolean biosRomPower = false;

	private int romStartupAddress = 0;
	private byte romControlRegister = 0;
	private byte romPageCount = 0;
	@SuppressWarnings("unused")
	private byte romChecksum = 0;
	private byte romMultiLoadIndex = 0;
	@SuppressWarnings("unused")
	private int romProgressBarSpeed = 0;
	private byte[] romPageOffsets;
	
	private int tapeOffset = 0;

	private transient BUS bus;

	
	private static final int BIOS_INT_PART_NO_ADDR 		= 0xfb00;
	private static final int BIOS_INT_CONTROL_REG_ADDR	= 0xfb01;
	private static final int BIOS_INT_START_ADDR 		= 0xfb02;
	private static final int BIOS_INT_RANDOM_SEED_ADDR	= 0xfb04;
	private static final int BIOS_INT_PART_LOADED_OK	= 0xfb05;
	private static final int BIOS_INT_EMUL_LOAD_HOTSPOT	= 0x0c00;
	
	private static final int HEADER_SIZE = 256;
	private static final int PAGE_SIZE = 256;
	private static final int BANK_SIZE = 8 * PAGE_SIZE;
	private static final int BIOS_BANK_OFFSET = 3 * BANK_SIZE;
	private static final int PART_SIZE = 4 * BANK_SIZE + HEADER_SIZE;	// 4 * 2048 banks + header

	private static int peekPartNoOnTape(byte[] tapeContent, int tapeOffset) {
		return tapeContent[tapeOffset + 4*BANK_SIZE + 5];
	}
	
	public static final CartridgeFormat FORMAT = new CartridgeFormat("AR", "8K-64K Arcadia/Starpath/Supercharger") {
		@Override
		public Cartridge createCartridge(ROM rom) {
			return new Cartridge8K_64K_AR(rom);
		}
		@Override
		public CartridgeFormatOption getOption(ROM rom) {
			// Any number of parts between 1 and 8
			if (rom.content.length % PART_SIZE != 0 || rom.content.length / PART_SIZE < 1 || rom.content.length / PART_SIZE > 8) return null;
			// Check if the content starts with Part 0
			if (peekPartNoOnTape(rom.content, 0) != 0)
				throw new ROMFormatDenialDetailException("Wrong Supercharger Tape ROM file. This is NOT a Tape Start ROM!\nTry loading a First Part ROM or a Full Tape ROM.");
			return new CartridgeFormatOption(101, this, rom);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

}
