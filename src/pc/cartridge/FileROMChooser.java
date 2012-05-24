// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.cartridge;

import java.io.File;
import java.security.AccessControlException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import parameters.Parameters;
import atari.cartridge.Cartridge;

public class FileROMChooser {

	public static Cartridge chooseFile() {
		if (lastFileChosen == null) {
			String path = Parameters.readPreference(LAST_FILE_CHOSEN_PREF);
			if (path != null) lastFileChosen = new File(path);
		}
		try {
			if (chooser == null) {
				chooser = new JFileChooser();
				chooser.setFileFilter(new FileNameExtensionFilter(ROMLoader.VALID_FILES_DESC, ROMLoader.VALID_FILE_EXTENSIONS));
			}
			chooser.setSelectedFile(lastFileChosen);
			int res = chooser.showOpenDialog(null);
			if (res != 0) return null;
		} catch (AccessControlException ex) {
			// Automatically tries FileServiceChooser if access is denied
			return FileServiceROMChooser.chooseFile();
		}
		lastFileChosen = chooser.getSelectedFile();
		Parameters.storePreference(LAST_FILE_CHOSEN_PREF, lastFileChosen.toString());
		return ROMLoader.load(lastFileChosen);
	}
	
	private static JFileChooser chooser;
	private static File lastFileChosen;

	private static final String LAST_FILE_CHOSEN_PREF = "lastROMFileChosen";

}
