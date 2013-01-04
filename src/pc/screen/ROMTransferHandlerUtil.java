// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.screen;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FilePermission;
import java.util.List;

import pc.cartridge.ROMLoader;
import atari.cartridge.Cartridge;

public final class ROMTransferHandlerUtil {

	public static boolean canAccept(Transferable transf) {
		// General URLs
		if (transf.isDataFlavorSupported(DATA_FLAVOR_TEXT)) return true;
		// Files
		if (!transf.isDataFlavorSupported(DATA_FLAVOR_FILE_LIST)) return false;
		try { // Files Permission
			new FilePermission(".", "read").checkGuard("Ignored");
			return true;
		} catch (SecurityException ex) {
			return false;
		}
	}

	public static Cartridge importCartridgeData(Transferable transf) {
		// First try String URL
		String url = getSingleURL(transf);
		if (url != null) return ROMLoader.load(url);
		// Then try File
		File file = getSingleFile(transf);
		if (file != null) return ROMLoader.load(file);
		// Give up
		return null;
	}

	private static String getSingleURL(Transferable transf) {
		if (!transf.isDataFlavorSupported(DATA_FLAVOR_TEXT)) return null;
		try {
			String data = (String) transf.getTransferData(DATA_FLAVOR_TEXT);
			data = data.trim();
			if (!data.isEmpty())
				return data;
		} catch (Exception ex) {
			// Ignore
		}
		return null;
	}

	private static File getSingleFile(Transferable transf) {
		if (!transf.isDataFlavorSupported(DATA_FLAVOR_FILE_LIST)) return null;
		try {
			@SuppressWarnings("unchecked")
			List<File> data = (List<File>) transf.getTransferData(DATA_FLAVOR_FILE_LIST);
			if (data.isEmpty()) return null;
			return data.get(0);
		} catch (Exception ex) {
			// Ignore
		}
		return null;
	}

	private static DataFlavor DATA_FLAVOR_TEXT;
	private static DataFlavor DATA_FLAVOR_FILE_LIST;

	static {
		try {
			DATA_FLAVOR_TEXT = new DataFlavor("text/plain; class=java.lang.String");
			DATA_FLAVOR_FILE_LIST = new DataFlavor("application/x-java-file-list; class=java.util.List");
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
	}
	
}
