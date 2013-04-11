// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.cartridge;

import javax.swing.JOptionPane;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.parameters.Parameters;


public final class URLROMChooser {

	public static Cartridge chooseURL() {
		if (lastURLChosen == null) lastURLChosen = Parameters.LAST_ROM_URL_CHOSEN;
		String opt = (String)JOptionPane.showInputDialog(
			"Load Cartridge from URL:                                                  ", 
			lastURLChosen
		);
		if (opt == null || opt.trim().isEmpty()) return null;
		lastURLChosen = opt.trim();
		Parameters.LAST_ROM_URL_CHOSEN = lastURLChosen;
		Parameters.savePreferences();
		return ROMLoader.load(opt, false);
	}

	private static String lastURLChosen;

}
