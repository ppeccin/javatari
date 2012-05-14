// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.file;

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

	public FileSaveStateMedia(SaveStateSocket socket) {
		socket.connectMedia(this);
	}

	@Override
	public boolean save(int slot, ConsoleState state) {
		try {
			// Creates the savestate directory if needed
			File dir = new File(BASE_DIR);
			if (!dir.isDirectory())
				dir.mkdir();
			FileOutputStream file = null;
			try {
				ByteArrayOutputStream data = new ByteArrayOutputStream();
				ObjectOutputStream stream = new ObjectOutputStream(data);
				stream.writeObject(state);
				file = new FileOutputStream(BASE_DIR + File.separator + "savestate" + slot + ".sav");
				file.write(data.toByteArray());
			} finally {
				if (file != null) file.close();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public ConsoleState load(int slot) {
		try{
			FileInputStream file = null;
			try{
				file = new FileInputStream(BASE_DIR + File.separator + "savestate" + slot + ".sav");
				byte[] data = new byte[file.available()];
				file.read(data);
				ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(data));
				return (ConsoleState) stream.readObject();
			} finally {
				if (file != null) file.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static final String BASE_DIR = "saves";

}
