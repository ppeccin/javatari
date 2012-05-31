// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.cartridge;

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

import javax.swing.JOptionPane;

import atari.cartridge.Cartridge;

public class ROMLoader {

	public static Cartridge load(String url) {
		try {
			return load(new URL(url));
		} catch (MalformedURLException ex) {
			errorMessage(ex, url);
		}
		return null;
	}

	public static Cartridge load(File file) {
		try {
			return load(file.toURI().toURL());
		} catch (MalformedURLException ex) {
			errorMessage(ex, file.getPath());
		} catch (AccessControlException ex) {
			errorMessage(ex, file.getPath());
		}
		return null;
	}

	public static Cartridge load(URL url) {
		InputStream stream = null;
		try {
			System.out.println("Loading Cartridge from: " + url);
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(5000);
			stream = conn.getInputStream();
			return load(stream, url.toString());
		} catch (AccessControlException ex) {
			errorMessage(ex, url.toString());
		} catch (IOException ex) {
			errorMessage(ex, url.toString());
		}
		return null;
	}

	public static Cartridge load(InputStream stream, String name) {
		BufferedInputStream buffer = bufferedStream(stream);
		try {
			try {
				// First try reading and creating directly
				return tryCreation(buffer, name);
			} catch (UnsupportedROMFormatException ex) {
				// If it fails, try assuming its a compressed stream (zip)
				buffer.reset();
				InputStream romFromZIP = getFirstROMFromZIP(buffer);
				if (romFromZIP == null) throw ex;	// Probably not zipped either
				return tryCreation(romFromZIP, name);
			}
		} catch (IOException ex) {
			errorMessage(ex, name);
		} catch (UnsupportedROMFormatException ex) {
			errorMessage(ex, name);
		} finally {
			try { 
				stream.close();
				buffer.close(); 
			} catch (IOException e) {}
		}
		return null;
	}

	private static BufferedInputStream bufferedStream(InputStream stream) {
		BufferedInputStream buf = new BufferedInputStream(stream, MAX_STREAM_SIZE);
		buf.mark(MAX_STREAM_SIZE);
		return buf;
	}

	private static Cartridge tryCreation(InputStream stream, String name) throws IOException, UnsupportedROMFormatException {
		byte[] buffer = new byte[MAX_STREAM_SIZE];
		int totalRead = 0;
		do {
			int read = stream.read(buffer, totalRead, MAX_STREAM_SIZE - totalRead);
			if (read == -1) break;	// End of Stream
			totalRead += read;
		} while(totalRead < MAX_STREAM_SIZE);
		byte[] content = (totalRead > 0) ? Arrays.copyOf(buffer, totalRead) : new byte[0];
		return CartridgeCreator.create(content, name);
	}

	private static InputStream getFirstROMFromZIP(InputStream stream) throws IOException, UnsupportedROMFormatException {
		ZipInputStream zipStream = new ZipInputStream(stream);
		while(true) {
			ZipEntry entry = zipStream.getNextEntry();
			if (entry == null) return null;
			String entryName = entry.getName().toUpperCase();
			for (int i = 0; i < VALID_FILE_EXTENSIONS.length; i++)
				if (entryName.endsWith(VALID_FILE_EXTENSIONS[i].toUpperCase()))
					return zipStream;
		}
	}

	private static void errorMessage(Exception ex, String name) {
		System.out.println("Could not load Cartridge from: " + name);
		System.out.println(ex);
		String tName = name == null ? "" : name.trim();
		if (tName.length() > 80) tName = tName.substring(0, 79);
		JOptionPane.showMessageDialog(
			null,
			"Could not load Cartridge from:\n" + tName,
			"Error loading Cartridge",
			JOptionPane.ERROR_MESSAGE
		);
	}


	private static final int MAX_ROM_SIZE = 32768;
	private static final int MAX_STREAM_SIZE = MAX_ROM_SIZE + 1000;

	public static final String   VALID_FILES_DESC = "ROM files (.bin .rom .a26 .zip)";
	public static final String[] VALID_FILE_EXTENSIONS = {"bin", "rom", "a26", "zip"};
	
}