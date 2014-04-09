// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.savestate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.javatari.atari.cartridge.formats.CartridgeSavestate;
import org.javatari.atari.console.savestate.ConsoleState;
import org.javatari.atari.console.savestate.SaveStateMedia;
import org.javatari.atari.console.savestate.SaveStateSocket;
import org.javatari.pc.cartridge.FileROMChooser;
import org.javatari.pc.cartridge.ROMLoader;


public final class FileSaveStateMedia implements SaveStateMedia {

	public void connect(SaveStateSocket socket) {
		socket.connectMedia(this);
	}

	@Override
	public boolean saveStateFile(ConsoleState state) {
		File file = FileROMChooser.chooseFileToSavestate();
		if (file == null) return false;
		return saveToFile(file.toString(), state, true);
	}

	@Override
	public boolean saveState(int slot, ConsoleState state) {
		return saveToFile(insideSavesDirectory("javatarisave" + slot + "." + ROMLoader.VALID_STATE_FILE_EXTENSION), state, true);
	}

	@Override
	public ConsoleState loadState(int slot) {
		try{
			return (ConsoleState)loadFromFile(insideSavesDirectory("javatarisave" + slot + "." + ROMLoader.VALID_STATE_FILE_EXTENSION), true);
		} catch (Exception ex) {
			// ClassCast or any other error
			return null;
		}
	}

	@Override
	public boolean saveToFile(String fileName, Object data, boolean isSavestate) {
		try {
			// Create the savestate directory if needed
			File dir = new File(savesDirectory());
			if (!dir.isDirectory())
				dir.mkdir();
			FileOutputStream file = null;
			try {
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				// Begin the file with the identifier if asked
				if (isSavestate) {
					byteStream.write(CartridgeSavestate.contentIdentifier);
					byteStream.flush();
				}
				// Then the Savestate data
				ObjectOutputStream stream = new ObjectOutputStream(byteStream);
				stream.writeObject(data);
				file = new FileOutputStream(fileName);
				file.write(byteStream.toByteArray());
			} finally {
				if (file != null) file.close();
			}
			return true;
		} catch (Exception ex) {
			// No permissions or any other IO error
			ex.printStackTrace();
			return false;
		}
	}

	@Override
	public Object loadFromFile(String fileName, boolean isSavestate) {
		try{ 
			FileInputStream file = null;
			try{
				file = new FileInputStream(fileName);
				// Load the state
				byte[] data = new byte[file.available()];
				file.read(data);
				ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
				// If asked, check the Savestate identifier that should be present at the beginning of the file
				if (isSavestate) {
					if (!CartridgeSavestate.checkIdentifier(data))
						throw new IllegalStateException("Invalid Javatari Savestate file");
					// Skips the Savestate identifier
					byteStream.skip(CartridgeSavestate.contentIdentifier.length);
				}
				ObjectInputStream objStream = new ObjectInputStream(byteStream);
				return objStream.readObject();
			} finally {
				if (file != null) file.close();
			}
		} catch (Exception ex) {
			// No permissions or any other IO error
			ex.printStackTrace();
			return null;
		}
	}

	public String insideSavesDirectory(String fileName) {
		return savesDirectory() + File.separator + fileName;
	}
	
	private String savesDirectory() {
		if (savesDirectory != null) return savesDirectory;
		try {
			String userHome = System.getProperty("user.home");
			if (userHome != null && !userHome.isEmpty()) 
				return savesDirectory = userHome + File.separator + BASE_DIR;
		} catch(SecurityException ex) {
			// No permissions... Ignore and use default directory
		}
		return savesDirectory = BASE_DIR;
	}
	
	
	private String savesDirectory;
	
	private static final String BASE_DIR = "javatarisaves";

}
