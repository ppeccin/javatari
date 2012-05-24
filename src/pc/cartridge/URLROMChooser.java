// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.cartridge;

import javax.swing.JOptionPane;

import parameters.Parameters;
import atari.cartridge.Cartridge;

public class URLROMChooser {

	public static Cartridge chooseURL() {
		if (lastURLChosen == null) lastURLChosen = Parameters.readPreference(LAST_URL_CHOSEN_PREF);
		String opt = (String)JOptionPane.showInputDialog(
			"Load Cartridge from URL:                                                  ", 
			lastURLChosen
		);
		if (opt == null || opt.trim().isEmpty()) return null;
		lastURLChosen = opt.trim();
		Parameters.storePreference(LAST_URL_CHOSEN_PREF, lastURLChosen);
		return ROMLoader.load(opt);
	}

	private static String lastURLChosen;

	private static final String LAST_URL_CHOSEN_PREF = "lastROMURLChosen";

}
