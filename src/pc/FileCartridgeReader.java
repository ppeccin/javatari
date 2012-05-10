// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc;

import general.av.video.VideoStandard;

import java.io.File;
import java.io.FileInputStream;
import java.security.AccessControlException;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;

import atari.cartridge.Cartridge;
import atari.cartridge.Cartridge12K;
import atari.cartridge.Cartridge16K;
import atari.cartridge.Cartridge28K;
import atari.cartridge.Cartridge32K;
import atari.cartridge.Cartridge4K;
import atari.cartridge.Cartridge8K;
import atari.cartridge.Cartridge8KSliced;
import atari.cartridge.CartridgeDisconnected;

public class FileCartridgeReader {

	// DASM Format 3, no header. Common 2600 cartridge ROM format (.bin)
	
	public static Cartridge chooseFile() {
		if (lastFileOpened == null) readLastFileOpenedPref();
		if (chooser == null) chooser = new JFileChooser();
		chooser.setCurrentDirectory(lastFileOpened);
		int res = chooser.showOpenDialog(null);
		if (res != 0) return null;
		lastFileOpened = chooser.getSelectedFile();
		storeLastFileOpenedPref();
		return read(lastFileOpened);
	}
	
	public static Cartridge readFile(String fileName) {
		File file = new File(fileName);
		return read(file);
	}

	private static void readLastFileOpenedPref() {
		Preferences prefs = getUserPreferences();
		if (prefs != null)
			lastFileOpened = new File(prefs.get(LAST_FILE_OPENED_PREF, ""));
	}

	private static void storeLastFileOpenedPref() {
		Preferences prefs = getUserPreferences();
		if (prefs != null)
			prefs.put(LAST_FILE_OPENED_PREF, lastFileOpened.toString());
	}

	private static Preferences getUserPreferences() {
		if (userPreferences == null)
			try{
				userPreferences = Preferences.userRoot().node("javatari");
			} catch(AccessControlException ex) {
				// Ignore
			}
		return userPreferences;
	}

	private static Cartridge read(File file) {
		try {
			FileInputStream stream;
			stream = new FileInputStream(file);
			int len = (int) file.length();
			byte[] buffer = new byte[len];
			System.out.println("Fetching Cartridge file: " + file.getName());
			stream.read(buffer);
			stream.close();
			return create(buffer, file.getName());
		} catch (Exception e) {
			System.out.println("Unable to load Cartridge file: " + file.getName());
			return null;
		}
	}

	// TODO Find a better way to identify the type of Bankswitching and the VideoStandard of Cartridges
	private static Cartridge create(byte[] content, String fileName) {
		String cartName = fileName.toUpperCase();
		Cartridge cart = null; 
		// Special case for Sliced "E0" format as indicated in filename
		if (cartName.indexOf("[SLICED]") >= 0 || cartName.indexOf("[E0]") >= 0) {
			switch (content.length) {
				case Cartridge8KSliced.SIZE:
					cart = new Cartridge8KSliced(content); break;
				default:
					throw new UnsupportedOperationException("Cartridge [SLICED, E0] size not supported: " + content.length);
			}
		} else {
			// Force SuperChip mode ON or OFF as indicated in filename, otherwise leave it in auto mode (null)
			Boolean sc = null;
			if (cartName.indexOf("[SC]") >= 0)
				sc = true;
			else if (cartName.indexOf("[NOSC]") >= 0)
					sc = false;
			switch (content.length) {
				case CartridgeDisconnected.SIZE:
					cart = new CartridgeDisconnected(); break;
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
					throw new UnsupportedOperationException("Cartridge size not supported: " + content.length);
			}
		}
		// Use VideoStandard specified on the filename. Default is null (auto)
		if (cartName.indexOf("[PAL]") >= 0)
			cart.suggestedVideoStandard(VideoStandard.PAL);
		else
			if (cartName.indexOf("[NTSC]") >= 0)
				cart.suggestedVideoStandard(VideoStandard.NTSC);
		return cart;
	}
	
	private static JFileChooser chooser;
	private static File lastFileOpened;
	private static Preferences userPreferences;

	private static final String LAST_FILE_OPENED_PREF = "lastFileOpened";

}
