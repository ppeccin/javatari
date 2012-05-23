// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.cartridge;


import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import parameters.Parameters;
import atari.cartridge.Cartridge;

public class FileCartridgeChooser {

	public static Cartridge chooseFile() {
		if (lastFileChosen == null) lastFileChosen = new File(Parameters.readPreference(LAST_FILE_OPENED_PREF));
		if (chooser == null) chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("ROM files", "bin", "rom", "a26"));
		chooser.setSelectedFile(lastFileChosen);
		int res = chooser.showOpenDialog(null);
		if (res != 0) return null;
		lastFileChosen = chooser.getSelectedFile();
		Parameters.storePreference(LAST_FILE_OPENED_PREF, lastFileChosen.toString());
		return CartridgeLoader.load(lastFileChosen);
	}
	
	private static JFileChooser chooser;
	private static File lastFileChosen;

	private static final String LAST_FILE_OPENED_PREF = "lastROMFileChosen";

}
