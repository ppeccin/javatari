// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.savestate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import atari.console.savestate.ConsoleState;
import atari.console.savestate.SaveStateMedia;
import atari.console.savestate.SaveStateSocket;

public class FileSaveStateMedia implements SaveStateMedia {

	public void connect(SaveStateSocket socket) {
		socket.connectMedia(this);
	}

	@Override
	public boolean save(int slot, ConsoleState state) {
		try {
			// Create the savestate directory if needed
			File dir = new File(savesDirectory());
			if (!dir.isDirectory())
				dir.mkdir();
			FileOutputStream file = null;
			try {
				ByteArrayOutputStream data = new ByteArrayOutputStream();
				ObjectOutputStream stream = new ObjectOutputStream(data);
				stream.writeObject(state);
				file = new FileOutputStream(savesDirectory() + File.separator + "save" + slot + ".sav");
				file.write(data.toByteArray());
			} finally {
				if (file != null) file.close();
			}
			return true;
		} catch (Exception ex) {
			// No permissions or any other IO error
		}
		return false;
	}

	@Override
	public ConsoleState load(int slot) {
		try{
			FileInputStream file = null;
			try{
				file = new FileInputStream(savesDirectory() + File.separator + "save" + slot + ".sav");
				byte[] data = new byte[file.available()];
				file.read(data);
				ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(data));
				return (ConsoleState) stream.readObject();
			} finally {
				if (file != null) file.close();
			}
		} catch (Exception ex) {
			// No permissions or any other IO error
		}
		return null;
	}

	private String savesDirectory() {
		if (savesDirectory != null) return savesDirectory;
		try{
			String userHome = System.getProperty("user.home");
			if (userHome != null && !userHome.isEmpty()) 
				return savesDirectory = userHome + File.separator + BASE_DIR;
		} catch(SecurityException ex) {
			// No permissions... Ignore and use default directory
		}
		return savesDirectory = BASE_DIR;
	}
	
	
	private String savesDirectory;
	
	private static final String BASE_DIR = "javatarisaves";

}
