// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.cartridge;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jnlp.FileContents;
import javax.swing.JOptionPane;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeCreator;
import org.javatari.atari.cartridge.CartridgeDatabase;
import org.javatari.atari.cartridge.ROM;
import org.javatari.atari.cartridge.ROMFormatUnsupportedException;
import org.javatari.parameters.Parameters;


public final class ROMLoader {

	public static Cartridge load(String url, boolean provided) {
		try {
			return load(new URL(url), provided);
		} catch (MalformedURLException ex) {
			generalErrorMessage(ex, url);
		}
		return null;
	}

	public static Cartridge load(File file) {
		try {
			return load(file.toURI().toURL(), false);
		} catch (MalformedURLException ex) {
			generalErrorMessage(ex, file.getPath());
		} catch (AccessControlException ex) {
			generalErrorMessage(ex, file.getPath());
		}
		return null;
	}

	public static Cartridge load(URL url, boolean provided) {
		InputStream stream = null;
		try {
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(5000);
			stream = conn.getInputStream();
			return createFromExternalURL(stream, url.toString(), provided);
		} catch (AccessControlException ex) {
			generalErrorMessage(ex, url.toString());
		} catch (IOException ex) {
			generalErrorMessage(ex, url.toString());
		}
		return null;
	}

	public static Cartridge load(FileContents fileContents) {
		String fileName = "<unknown>";
		try {
			fileName = fileContents.getName();
			InputStream stream = fileContents.getInputStream();
			return createFromExternalURL(stream, fileName, false);
		} catch (IOException ex) {
			generalErrorMessage(ex, fileName);
			return null;
		}
	}
	
	public static Cartridge load(BuiltInROM builtInROM) {
		if (builtInROM.label != null) Parameters.CARTRIDGE_LABEL = builtInROM.label;
		if (builtInROM.labelColors != null) Parameters.setCartridgeLabelColors(builtInROM.labelColors);
		return load(builtInROM.url, true);
	}

	private static Cartridge createFromExternalURL(InputStream stream, String romURL, boolean provided) {
		System.out.println("Loading Cartridge from: " + romURL);
		BufferedInputStream buffer = bufferedStream(stream);
		try {
			try {
				// First try reading and creating directly
				return createCartridge(buffer, romURL, provided);
			} catch (ROMFormatUnsupportedException ex) {
				// If it fails, try assuming its a compressed stream (zip)
				buffer.reset();
				InputStream romFromZIP = getFirstROMFromZIP(buffer);
				if (romFromZIP == null) throw ex;	// Probably not zipped either
				return createCartridge(romFromZIP, romURL, provided);
			}
		} catch (ROMFormatUnsupportedException ex) {
			romFormatUnsupportedErrorMessage(ex, romURL);
		} catch (IOException ex) {
			generalErrorMessage(ex, romURL);
		} finally {
			try { 
				stream.close();
				buffer.close(); 
			} catch (IOException e) {}
		}
		return null;
	}

	private static Cartridge createCartridge(InputStream stream, String romURL, boolean provided) throws IOException, ROMFormatUnsupportedException {
		ROM rom = createROM(stream, romURL);
		if (provided) CartridgeDatabase.adjustInfoOfROMProvided(rom);
		return CartridgeCreator.create(rom);
	}

	private static ROM createROM(InputStream stream, String romURL) throws IOException {
		byte[] buffer = new byte[MAX_STREAM_SIZE];
		int totalRead = 0;
		do {
			int read = stream.read(buffer, totalRead, MAX_STREAM_SIZE - totalRead);
			if (read == -1) break;	// End of Stream
			totalRead += read;
		} while(totalRead < MAX_STREAM_SIZE);
		byte[] content = (totalRead > 0) ? Arrays.copyOf(buffer, totalRead) : new byte[0];
		return new ROM(romURL.trim(), content);
	}

	private static InputStream getFirstROMFromZIP(InputStream stream) throws IOException, ROMFormatUnsupportedException {
		ZipInputStream zipStream = new ZipInputStream(stream);
		while(true) {
			ZipEntry entry = zipStream.getNextEntry();
			if (entry == null) return null;
			String entryName = entry.getName().toUpperCase();
			for (int i = 0; i < VALID_LOAD_FILE_EXTENSIONS.length; i++)
				if (entryName.endsWith(VALID_LOAD_FILE_EXTENSIONS[i].toUpperCase()))
					return zipStream;
		}
	}

	private static BufferedInputStream bufferedStream(InputStream stream) {
		BufferedInputStream buf = new BufferedInputStream(stream, MAX_STREAM_SIZE);
		buf.mark(MAX_STREAM_SIZE);
		return buf;
	}

	private static void generalErrorMessage(Exception ex, String location) {
		System.out.println("Could not load Cartridge from: " + location);
		System.out.println(ex);
		String tLoc = location == null ? "" : location.trim();
		if (tLoc.length() > 80) tLoc = tLoc.substring(0, 79) + "...";
		JOptionPane.showMessageDialog(
			null,
			"Could not load Cartridge from:\n" + tLoc + "\n\n" + ex.getClass().getSimpleName() + ": " + ex.getMessage(),
			"Error loading Cartridge",
			JOptionPane.ERROR_MESSAGE
		);
	}

	private static void romFormatUnsupportedErrorMessage(ROMFormatUnsupportedException ex, String location) {
		System.out.println("Could not load Cartridge from: " + location);
		System.out.println(ex.getMessage());
		String tLoc = location == null ? "" : location.trim();
		if (tLoc.length() > 80) tLoc = tLoc.substring(0, 79) + "...";
		JOptionPane.showMessageDialog(
			null,
			"Could not load Cartridge from:\n" + tLoc + "\n\n" + ex.getMessage(),
			"Error loading Cartridge",
			JOptionPane.ERROR_MESSAGE
		);
	}


	private static final int MAX_ROM_SIZE = 512 * 1024;
	private static final int MAX_STREAM_SIZE = MAX_ROM_SIZE + 1024;

	public static final String   VALID_LOAD_FILES_DESC = "ROM and Savestate files (.bin .rom .a26 .zip .jat)";
	public static final String[] VALID_LOAD_FILE_EXTENSIONS = {"bin", "rom", "a26", "zip", "jat"};
	
	public static final String   VALID_STATE_FILE_DESC = "Javatari Savestate files (.jat)";
	public static final String   VALID_STATE_FILE_EXTENSION = "jat";
	
}