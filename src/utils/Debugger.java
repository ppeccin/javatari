// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package utils;

import javax.swing.JOptionPane;

public class Debugger {

	public static int show(String message) {
		return show(message, message, new String[] { "OK" } );
	}

	public static int show(String title, String message) {
		return show(title, message, new String[] { "OK" } );
	}

	public static int show(String title, String message, String[] buttons) {
		int res;
		do {
			res = JOptionPane.showOptionDialog(
					null, 
					message, 
					title, 
					JOptionPane.DEFAULT_OPTION, 
					JOptionPane.WARNING_MESSAGE, 
					null, 
					buttons, 
					null
			);
		} while(res < 0);
		return res;
	}

}
