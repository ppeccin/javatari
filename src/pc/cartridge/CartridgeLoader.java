// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.cartridge;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import atari.cartridge.Cartridge;

public class CartridgeLoader {

	public static Cartridge load(String url) {
		try {
			return load(new URL(url));
		} catch (MalformedURLException ex) {
			System.out.println("Could not parse Cartridge URL: " + url);
			System.out.println(ex);
		}
		return null;
	}

	public static Cartridge load(URL url) {
		InputStream stream = null;
		try {
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(5000);
			stream = conn.getInputStream();
			System.out.println("Loading Cartridge from: " + url);
			return load(stream, url.toString());
		} catch (Exception ex) {
			System.out.println("Unable to load Cartridge from: " + url);
			System.out.println(ex);
		} finally {
			if (stream != null) try { 
				stream.close(); 
			} catch (IOException e) {}
		}
		return null;
	}

	public static Cartridge load(InputStream stream, String name) throws IOException {
		byte[] buffer = new byte[MAX_ROM_SIZE];
		int read = stream.read(buffer);
		byte[] content = (read > 0) ? Arrays.copyOf(buffer, read) : new byte[0];
		return CartridgeCreator.create(content, name);
	}


	private static final int MAX_ROM_SIZE = 32768;

}
