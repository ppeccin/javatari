// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.cartridge;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;

import javax.swing.JOptionPane;

import atari.cartridge.Cartridge;

public class URLCartridgeChooser {

	public static Cartridge chooseURL() {
		String str = "";
		try {
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			str = (String)clip.getData(DataFlavor.stringFlavor);
		} catch (Exception ex) {
			// Ignore
		}
		String opt = (String)JOptionPane.showInputDialog(
			"Load Cartridge from URL:                                                  ", 
			str
		);
		if (opt == null || opt.trim().isEmpty()) return null;
		return CartridgeLoader.load(opt);
	}

}
