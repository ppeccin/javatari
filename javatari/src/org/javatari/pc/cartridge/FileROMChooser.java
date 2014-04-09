// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.cartridge;

import java.awt.Dimension;
import java.io.File;
import java.security.AccessControlException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.parameters.Parameters;


public final class FileROMChooser {

	public static Cartridge chooseFileToLoad() {
		if (lastLoadFileChosen == null) lastLoadFileChosen = new File(Parameters.LAST_ROM_LOAD_FILE_CHOSEN);
		try {
			if (chooser == null) createChooser();
			chooser.setFileFilter(new FileNameExtensionFilter(ROMLoader.VALID_LOAD_FILES_DESC, ROMLoader.VALID_LOAD_FILE_EXTENSIONS));
			chooser.setSelectedFile(lastLoadFileChosen);
			int res = chooser.showOpenDialog(null);
			if (res != 0) return null;
		} catch (AccessControlException ex) {
			// Automatically tries FileServiceChooser if access is denied
			return FileServiceROMChooser.chooseFileToLoad();
		}
		lastLoadFileChosen = chooser.getSelectedFile();
		Parameters.LAST_ROM_LOAD_FILE_CHOSEN = lastLoadFileChosen.toString();
		Parameters.savePreferences();
		return ROMLoader.load(lastLoadFileChosen);
	}

	public static File chooseFileToSavestate() {
		if (chooser != null && chooser.isShowing()) return null;
		if (lastSaveFileChosen == null) lastSaveFileChosen = new File(Parameters.LAST_ROM_SAVE_FILE_CHOSEN);
		try {
			if (chooser == null) createChooser();
			chooser.setFileFilter(new FileNameExtensionFilter(ROMLoader.VALID_STATE_FILE_DESC, ROMLoader.VALID_STATE_FILE_EXTENSION));
			chooser.setSelectedFile(lastSaveFileChosen);
			int res = chooser.showSaveDialog(null);
			if (res != 0) return null;
		} catch (AccessControlException ex) {
			// Automatically tries FileServiceChooser if access is denied
			// return FileServiceROMChooser.chooseFileToSavestate();	TODO Implement
			return null;
		}
		lastSaveFileChosen = chooser.getSelectedFile();
		if (!lastSaveFileChosen.toString().toUpperCase().endsWith(ROMLoader.VALID_STATE_FILE_EXTENSION.toUpperCase()))
			lastSaveFileChosen = new File(lastSaveFileChosen + "." + ROMLoader.VALID_STATE_FILE_EXTENSION);
		Parameters.LAST_ROM_SAVE_FILE_CHOSEN = lastSaveFileChosen.toString();
		Parameters.savePreferences();
		return lastSaveFileChosen;
	}

	private static void createChooser() {
		chooser = new JFileChooser();
		chooser.setPreferredSize(new Dimension(580, 400));
	}
	
	
	private static JFileChooser chooser;
	private static File lastLoadFileChosen;
	private static File lastSaveFileChosen;

}
