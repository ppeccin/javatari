// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class ROMReader {

	// DASM Format 1, 16bit Origin header
	public static void read(String fileName, byte[] memory) throws IOException {
		FileInputStream stream = new FileInputStream(getFile(fileName));
		int origin;
		int b = stream.read();
		if (b == -1)
			throw new IllegalStateException("Unexpected end of ROM file: + filename");
		origin = b;
		b = stream.read();
		if (b == -1)
			throw new IllegalStateException("Unexpected end of ROM file: + filename");
		origin += origin + (b << 8);
		System.out.printf("Fetching ROM file %s at origin %4x\n", fileName, origin);
		stream.read(memory, origin, memory.length - origin);
		stream.close();	
	}

	private static File getFile(String fileName) throws IOException, FileNotFoundException {
		URL url = ClassLoader.getSystemResource(fileName);
		if (url == null)
			throw new IOException("Could not find ROM file: " + fileName);
		try {
			return new File(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

}
