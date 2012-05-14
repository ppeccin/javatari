// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.file;

import java.io.InputStream;

import javax.jnlp.BasicService;
import javax.jnlp.FileContents;
import javax.jnlp.FileOpenService;
import javax.jnlp.ServiceManager;

import atari.cartridge.Cartridge;

public class FileServiceCartridgeReader {

	public static Cartridge chooseFile() {
		try {
			BasicService bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService"); 
			FileOpenService fos = (FileOpenService)ServiceManager.lookup("javax.jnlp.FileOpenService"); 
			String codeBase = bs.getCodeBase().toString();
			System.out.println(codeBase);
			FileContents fileCon = fos.openFileDialog("C:/cartridges", new String[] {"bin", "rom", "a26"});
			return read(fileCon);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	private static Cartridge read(FileContents fileCont) {
		String fileName = "<unknown>";
		try {
			fileName = fileCont.getName();
			InputStream stream = fileCont.getInputStream();
			int len = (int) fileCont.getLength();
			byte[] buffer = new byte[len];
			System.out.println("Fetching Cartridge file: " + fileName);
			stream.read(buffer);
			stream.close();
			return CartridgeCreator.create(buffer, fileName);
		} catch (Exception e) {
			System.out.println("Unable to load Cartridge file: " + fileName);
			return null;
		}
	}

}
