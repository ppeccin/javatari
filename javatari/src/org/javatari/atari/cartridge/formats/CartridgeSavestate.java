// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.cartridge.ROM;
import org.javatari.atari.console.savestate.ConsoleState;

/**
 * Special Cartridge that represents an entire Savestate
 */
public final class CartridgeSavestate extends Cartridge {

	private CartridgeSavestate(ROM rom) {
		super(rom, FORMAT);
	}

	public ConsoleState getConsoleState() {
		try{ 
			ByteArrayInputStream byteStream = new ByteArrayInputStream(rom.content);
			byteStream.skip(contentIdentifier.length);
			ObjectInputStream objStream = new ObjectInputStream(byteStream);
			return (ConsoleState) objStream.readObject();
		} catch (Exception ex) {
			// Cast or IO errors
			ex.printStackTrace();
			return null;
		}
	}
	
	
	public static CartridgeSavestate createFromConsoleState(ConsoleState state) {
		try{
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			byteStream.write(contentIdentifier);
			byteStream.flush();
			ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
			objStream.writeObject(state);
			objStream.flush();
			byte[] content = byteStream.toByteArray();
			ROM rom = new ROM("CreatedSavestate", content);
			return new CartridgeSavestate(rom);
		} catch (Exception ex) {
			// IO errors
			ex.printStackTrace();
			return null;
		}
	}

	public static boolean checkIdentifier(byte[] content) {
		if (content.length < contentIdentifier.length) return false;
		for (int i = 0; i < contentIdentifier.length; i++)
			if (content[i] != contentIdentifier[i]) return false;
		return true;
	}

	public static final CartridgeFormat FORMAT = new CartridgeFormat("JAT", "Javatari Savestate") {
		@Override
		public Cartridge createCartridge(ROM rom) {
			return new CartridgeSavestate(rom);
		}
		@Override
		public CartridgeFormatOption getOption(ROM rom) {
			if (!checkIdentifier(rom.content)) return null;
			return new CartridgeFormatOption(90, this, rom);
		}
		private static final long serialVersionUID = 1L;
	};

	public static final long serialVersionUID = 1L;

	// Defines the Savestate identifier/version bytes found at the beginning of the content. "JavatariSavestateV000" 
	public static final byte[] contentIdentifier = {74, 97, 118, 97, 116, 97, 114, 105, 83, 97, 118, 101, 115, 116, 97, 116, 101, 86, 48, 48, 48};

}

