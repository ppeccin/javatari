// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.utils;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.util.Locale;

import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.javatari.parameters.Parameters;


public class Environment {

	public static void init() {
		System.out.println(Parameters.TITLE + " " + Parameters.VERSION + " on " + vmInfo());
		try {
			// Set Locale
			try {
				Locale.setDefault(Locale.ENGLISH);
			} catch (Exception ex)  {}

			SwingHelper.edtSmartInvokeAndWait(new Runnable() { @Override public void run() {
				// Set Look and Feel
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");	// 215, 220, 221
					NIMBUS_LAF = true;
				} catch (Exception ex) {}

				ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

				// Grab info about installed fonts
				try {
					String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
					for (int i = 0; i < fonts.length; i++) {
						String font = fonts[i].toUpperCase();
						if (font.equals("ARIAL")) ARIAL_FONT = true; 
						if (font.equals("LIBERATION SANS")) LIBERATION_FONT = true; 
					}
				} catch (Exception e) {}

				// Create font used to render Cartridge Labels
				try {
					InputStream fontStream = Environment.class.getClassLoader()
							.getResourceAsStream("org/javatari/pc/screen/images/LiberationSans-Bold.ttf");
					if (fontStream != null) {
						try {
							cartridgeLabelFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 15f);
						} finally {
							fontStream.close();
						}
					}
				} catch (Exception e) {}
			}});
		} catch (Exception e) {}
	}

	public static String vmInfo() {
		try {
			return System.getProperty("java.vm.name") + " ver: " + System.getProperty("java.version") + " (" + System.getProperty("os.arch") + ")";
		} catch (Throwable e) {
			return "VM info unavailable";
		}
	}

	public static boolean NIMBUS_LAF = false;
	public static boolean ARIAL_FONT = false;
	public static boolean LIBERATION_FONT = false;
	
	public static Font cartridgeLabelFont = null;

}
