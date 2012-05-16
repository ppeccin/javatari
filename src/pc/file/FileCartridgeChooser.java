// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.file;


import java.io.File;
import java.net.MalformedURLException;
import java.security.AccessControlException;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import atari.cartridge.Cartridge;

public class FileCartridgeChooser {

	public static Cartridge chooseFile() {
		if (lastFileChosen == null) readLastFileChosenPref();
		if (chooser == null) chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("ROM files", "bin", "rom", "a26"));
		chooser.setSelectedFile(lastFileChosen);
		int res = chooser.showOpenDialog(null);
		if (res != 0) return null;
		lastFileChosen = chooser.getSelectedFile();
		storeLastFileChosenPref();
		try {
			return CartridgeLoader.load(lastFileChosen.toURI().toURL());
		} catch (MalformedURLException e) {
			System.out.println("Unable to get URL for file: " + lastFileChosen.getPath());
		}
		return null;
	}
	
	private static void readLastFileChosenPref() {
		Preferences prefs = getUserPreferences();
		if (prefs != null)
			lastFileChosen = new File(prefs.get(LAST_FILE_OPENED_PREF, ""));
	}

	private static void storeLastFileChosenPref() {
		Preferences prefs = getUserPreferences();
		if (prefs != null)
			prefs.put(LAST_FILE_OPENED_PREF, lastFileChosen.toString());
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


	private static JFileChooser chooser;
	private static File lastFileChosen;
	private static Preferences userPreferences;

	private static final String LAST_FILE_OPENED_PREF = "lastROMFileChosen";

}
