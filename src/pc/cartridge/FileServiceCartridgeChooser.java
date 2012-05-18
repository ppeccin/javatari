// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.cartridge;

import java.io.IOException;
import java.io.InputStream;

import javax.jnlp.FileContents;
import javax.jnlp.FileOpenService;
import javax.jnlp.ServiceManager;


import atari.cartridge.Cartridge;

public class FileServiceCartridgeChooser {

	public static Cartridge chooseFile() {
		try {
			// BasicService bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService"); 
			FileOpenService fos = (FileOpenService)ServiceManager.lookup("javax.jnlp.FileOpenService"); 
			FileContents fileCon = fos.openFileDialog("C:/cartridges", new String[] {"bin", "rom", "a26"});
			return read(fileCon);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	private static Cartridge read(FileContents fileCont) {
		InputStream stream = null;
		String fileName = "<unknown>";
		try {
			fileName = fileCont.getName();
			stream = fileCont.getInputStream();
			System.out.println("Loading Cartridge from: " + fileName);
			stream.close();
			return CartridgeLoader.load(stream, fileName);
		} catch (Exception ex) {
			System.out.println("Unable to load Cartridge from: " + fileName);
			System.out.println(ex);
		} finally {
			if (stream != null) try { 
				stream.close(); 
			} catch (IOException e) {}
		}
		return null;
	}

}
