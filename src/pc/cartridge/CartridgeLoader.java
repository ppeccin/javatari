// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.cartridge;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessControlException;
import java.util.Arrays;

import javax.swing.JOptionPane;

import atari.cartridge.Cartridge;

public class CartridgeLoader {

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
		try{
			byte[] buffer = new byte[MAX_ROM_SIZE];
			int totalRead = 0;
			int read;
			do {
				read = stream.read(buffer, totalRead, MAX_ROM_SIZE - totalRead);
				if (read == -1) break;
				totalRead += read;
			} while(totalRead < MAX_ROM_SIZE);
			byte[] content = (totalRead > 0) ? Arrays.copyOf(buffer, totalRead) : new byte[0];
			return CartridgeCreator.create(content, name);
		} catch (IOException ex) {
			errorMessage(ex, name);
		} catch (UnsupportedROMFormatException ex) {
			errorMessage(ex, name);
		} finally {
			if (stream != null) try { 
				stream.close(); 
			} catch (IOException e) {}
		}
		return null;
	}

	private static void errorMessage(Exception ex, String name) {
		System.out.println("Could not load Cartridge from: " + name);
		System.out.println(ex);
		JOptionPane.showMessageDialog(
			null,
			"Could not load Cartridge from:\n" + name,
			"Error loading Cartridge",
			JOptionPane.ERROR_MESSAGE
		);
	}


	private static final int MAX_ROM_SIZE = 32768;

}
