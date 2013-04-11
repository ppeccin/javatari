// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.cartridge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.javatari.utils.Terminator;

public class BuiltInROM {

	public BuiltInROM(String label, String labelColors, URL romURL) {
		super();
		this.label = label;
		this.labelColors = labelColors;
		this.url = romURL;
	}

	
	public static ArrayList<BuiltInROM> all() {
		ArrayList<BuiltInROM> result = new ArrayList<BuiltInROM>();
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(ROMS_FOLDER + "/" + ROMS_LIST_FILE);
		if (stream == null) return result;
				
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		try {
			try {
				String line;
				while((line = reader.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) continue;
					String[] specs = line.split("@");
					if (specs.length == 0) continue; 
					String location = specs[specs.length -1].trim();
					if (location.isEmpty()) continue; 
					// First try to find as resource
					URL url = fileNameAsResourceURL(location);
					if (url == null) {
						// If not try as URL directly
						try {
							url = new URL(location);
						} catch (Exception e) {
							errorMessage(location);
							Terminator.terminate();
						}
					}
					String label = null;
					String labelColors = null;
					if (specs.length > 1 && !specs[0].trim().isEmpty()) {
						String[] labelSpecs = specs[0].trim().split("%");
						if (labelSpecs.length != 0) { 
							label = labelSpecs[0].trim();
							if (labelSpecs.length > 1 && !labelSpecs[1].trim().isEmpty()) labelColors = labelSpecs[1].trim();
						}
					}
					result.add(new BuiltInROM(label, labelColors, url));
				}
			} finally {
				stream.close();
			}
		} catch (IOException e) {
			// give up
		}
		
		return result;
	}

	private static URL fileNameAsResourceURL(String fileName) {
		return Thread.currentThread().getContextClassLoader().getResource(ROMS_FOLDER + "/" + fileName);		
	}
	
	private static void errorMessage(String location) {
		System.out.println("Invalid Built-in ROM location (file not found or invalid external URL): " + location);
		String tLoc = location == null ? "" : location.trim();
		if (tLoc.length() > 80) tLoc = tLoc.substring(0, 79) + "...";
		JOptionPane.showMessageDialog(
			null,
			"Invalid Built-in ROM location.\nFile not found or invalid external URL:\n\n" + tLoc,
			"Error in Built-in ROMs",
			JOptionPane.ERROR_MESSAGE
		);
	}


	public final String label;
	public final String labelColors;
	public final URL url;
	

	private static final String ROMS_FOLDER = "roms";
	private static final String ROMS_LIST_FILE = "roms.txt";

}
