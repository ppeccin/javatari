// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.cartridge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class BuiltInROM {

	public static ArrayList<String> allROMFileNames() {
		ArrayList<String> result = new ArrayList<String>();
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(ROMS_FOLDER + "/" + ROMS_LIST_FILE);
		if (stream == null) return result;
				
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		try {
			try {
				String line;
				while((line = reader.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) continue;
					String[] parts = line.split("@");
					String fileName = parts.length == 1 ? parts[0].trim() : parts[1].trim();
					if (!fileName.isEmpty()) result.add(fileName);
				}
			} finally {
				stream.close();
			}
		} catch (IOException e) {
			// give up
		}
		
		return result;
	}

	public static URL urlForFileName(String fileName) {
		return Thread.currentThread().getContextClassLoader().getResource(ROMS_FOLDER + "/" + fileName);		
	}
	

	private static final String ROMS_FOLDER = "roms";
	private static final String ROMS_LIST_FILE = "roms.txt";

}
