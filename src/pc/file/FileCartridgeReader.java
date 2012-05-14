// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.file;


import java.io.File;
import java.io.FileInputStream;
import java.security.AccessControlException;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import atari.cartridge.Cartridge;

public class FileCartridgeReader {

	public static Cartridge chooseFile() {
		if (lastFileOpened == null) readLastFileOpenedPref();
		if (chooser == null) chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("ROM files", "bin", "rom", "a26"));
		chooser.setSelectedFile(lastFileOpened);
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
			return CartridgeCreator.create(buffer, file.getName());
		} catch (Exception e) {
			System.out.println("Unable to load Cartridge file: " + file.getName());
			return null;
		}
	}

	private static JFileChooser chooser;
	private static File lastFileOpened;
	private static Preferences userPreferences;

	private static final String LAST_FILE_OPENED_PREF = "lastFileOpened";

}
