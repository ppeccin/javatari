// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.cartridge;

import javax.jnlp.FileContents;
import javax.jnlp.FileOpenService;
import javax.jnlp.ServiceManager;

import org.javatari.atari.cartridge.Cartridge;


public final class FileServiceROMChooser {

	public static Cartridge chooseFileToLoad() {
		try {
			FileOpenService fos = (FileOpenService)ServiceManager.lookup("javax.jnlp.FileOpenService"); 
			FileContents fileCon = fos.openFileDialog(null, ROMLoader.VALID_LOAD_FILE_EXTENSIONS);
			if (fileCon == null) return null;
			return ROMLoader.load(fileCon);
		} catch (Exception ex) {
			System.out.println("File Service Cartridge Chooser: unable to open dialog\n" + ex);
			return null;
		}
	}
	
}
