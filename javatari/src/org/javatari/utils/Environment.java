// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.utils;

import java.awt.GraphicsEnvironment;
import java.util.Locale;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;


public class Environment {

	public static void init() {
		System.out.println(vmInfo());
		try {
			// Set Locale
			try {
				Locale.setDefault(Locale.ENGLISH);
			} catch (Exception ex)  {}

			SwingUtilities.invokeAndWait( new Runnable() { public void run() {
				// Set Look and Feel
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");	// 215, 220, 221
					NIMBUS_LAF = true;
				} catch (Exception ex) {}

				// Grab info about installed fonts
				try {
					String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
					for (int i = 0; i < fonts.length; i++) {
						String font = fonts[i].toUpperCase();
						if (font.equals("ARIAL")) ARIAL_FONT = true; 
						if (font.equals("LIBERATION SANS")) LIBERATION_FONT = true; 
					}
				} catch (Exception e) {}
			}});
		} catch (Exception e) {}
	}

	public static String vmInfo() {
		try {
			return System.getProperty("java.vm.name") + " ver: " + System.getProperty("java.version");
		} catch (Throwable e) {
			return "VM info unavailable";
		}
	}

	public static boolean NIMBUS_LAF = false;
	public static boolean ARIAL_FONT = false;
	public static boolean LIBERATION_FONT = false;

}
