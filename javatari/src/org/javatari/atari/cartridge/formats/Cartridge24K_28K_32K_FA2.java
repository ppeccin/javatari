// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import java.util.Arrays;

import org.javatari.atari.board.BUS;
import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.cartridge.ROM;
import org.javatari.atari.console.savestate.SaveStateSocket;

/**
 * Implements the 24K/28K/32K "FA2" CBS RAM Plus format
 * Also supports SC RAM Saving on Harmony Flash memory (emulated to a file)
 */
public class Cartridge24K_28K_32K_FA2 extends CartridgeBankedByMaskedRange {

	protected Cartridge24K_28K_32K_FA2(ROM rom, CartridgeFormat format) {
		super(rom, format, BASE_BANKSW_ADDRESS, true, 256);		// SuperChip always ON, 256 RAM
	}

	@Override
	public void connectBus(BUS bus) {
		this.bus = bus;
	}

	@Override
	public void connectSaveStateSocket(SaveStateSocket socket) {
		this.saveStateSocket = socket;
	}

	@Override
	public byte readByte(int address) {
		byte b = super.readByte(address);

		// Normal behavior if not the Flash Operation Hotspot address
		if (maskedAddress != FLASH_OP_HOTSPOT) return b;
		
		// Should trigger new operation?
		if (harmonyFlashOpInProgress == 0) {
			int op = extraRAM[FLASH_OP_CONTROL];
			if (op == 1 || op == 2) {
				performFlashOperation(op);
				return (byte)(b | 0x40);	// In progress, set bit 6
			}
		}

		// Report operation completion
		if (harmonyFlashOpInProgress != 0) detectFlashOperationCompletion();
		else return (byte)(b & 0xbf);	// Not busy, clear bit 6
		
		if (harmonyFlashOpInProgress != 0) return (byte)(b | 0x40);	// Still in progress, set bit 6
		else return (byte)(b & 0xbf);							// Finished, clear bit 6
	};
	
	private void performFlashOperation(int op) {
		harmonyFlashOpInProgress = op;
		harmonyFlashOpStartTime = System.currentTimeMillis();
		// 1 = read, 2 = write
		if (op == 1) readMemoryFromFlash();
		else if (op == 2) saveMemoryToFlash();
	}

	private void readMemoryFromFlash() {
		bus.tia.videoOutput().showOSD("Reading from Cartridge Flash Memory...", true);
		if (saveStateSocket != null)
			try {
				byte[] data = (byte[])saveStateSocket.media().loadResource(flashMemoryResourceName());
				if (data.length == 256) harmonyFlashMemory = data;
			} catch(Exception ex) {
				// Give up
			}
		extraRAM = Arrays.copyOf(harmonyFlashMemory, extraRAM.length);
	}

	private void saveMemoryToFlash() {
		bus.tia.videoOutput().showOSD("Writing to Cartridge Flash Memory...", true);
		harmonyFlashMemory = Arrays.copyOf(extraRAM, extraRAM.length);
		if (saveStateSocket != null)
			saveStateSocket.media().saveResource(flashMemoryResourceName(), harmonyFlashMemory);
	}

	private void detectFlashOperationCompletion() {
		if (System.currentTimeMillis() - harmonyFlashOpStartTime > 1100) {		// 1.1 seconds
			harmonyFlashOpStartTime = Long.MIN_VALUE;
			harmonyFlashOpInProgress = 0;
			extraRAM[FLASH_OP_CONTROL] = 0;			// Set return code for Successful operation	
			bus.tia.videoOutput().showOSD("Done.", true);
			// Signal a external state modification
			// Flash memory may have been loaded from file and changed
			// Also the waitin timer is a source of indeterminism!
			if (saveStateSocket != null) saveStateSocket.externalStateChange();
		}		
	}

	private String flashMemoryResourceName() {
		return rom.info.hash + ".hfm";
	}

	@Override
	public Cartridge24K_28K_32K_FA2 clone() {
		Cartridge24K_28K_32K_FA2 clone = (Cartridge24K_28K_32K_FA2)super.clone();
		clone.harmonyFlashMemory = harmonyFlashMemory.clone();
		return clone;
	}


	private transient BUS bus;
	private transient SaveStateSocket saveStateSocket;
	
	private long    harmonyFlashOpStartTime = Long.MIN_VALUE;
	private int     harmonyFlashOpInProgress = 0;					// 0 = none, 1 = read, 2 = write
	private byte[]  harmonyFlashMemory = new byte[256];
	
	
	private static final int SIZE24K = 32768 - 8192;
	private static final int SIZE28K = 32768 - 4096;
	private static final int SIZE32K = 32768;
	private static final int BASE_BANKSW_ADDRESS = 0x0ff5;
	private static final int FLASH_OP_HOTSPOT = 0x0ff4;
	private static final int FLASH_OP_CONTROL = 0x00ff;

	public static final CartridgeFormat FORMAT = new CartridgeFormat("FA2", "24K/28K/32K CBS RAM Plus") {
		@Override
		public Cartridge createCartridge(ROM rom) {
			return new Cartridge24K_28K_32K_FA2(rom, this);
		}
		@Override
		public CartridgeFormatOption getOption(ROM rom) {
			if (rom.content.length != SIZE24K && rom.content.length != SIZE28K && rom.content.length != SIZE32K) return null;
			return new CartridgeFormatOption(102, this, rom);
		}
		private static final long serialVersionUID = 1L;
	};
	

	public static final long serialVersionUID = 1L;

}
