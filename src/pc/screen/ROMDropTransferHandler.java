// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.screen;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

import javax.swing.TransferHandler;

import pc.cartridge.ROMLoader;
import atari.cartridge.Cartridge;

public class ROMDropTransferHandler extends TransferHandler {

	public ROMDropTransferHandler(Screen screen) {
		super();
		this.screen = screen;
	}

	@Override
	public boolean canImport(TransferSupport support) {
		if (!screen.isCartridgeChangeEnabled()) return false;
		Transferable transf = support.getTransferable();
		if (transf.isDataFlavorSupported(DATA_FLAVOR_TEXT) || 
			transf.isDataFlavorSupported(DATA_FLAVOR_FILE_LIST)) {
			if (support.isDrop() && support.getUserDropAction() != LINK) support.setDropAction(COPY);
			return true;
		}
		return false;
	}

	@Override
	public boolean importData(TransferSupport support) {
		if (!canImport(support)) return false;

		Transferable transf = support.getTransferable();
		
		// LINK Action means load Cartridge without auto power! :-)
		boolean autoPower = !support.isDrop() || support.getDropAction() != LINK;
		
		// First try String URL
		String url = getSingleURL(transf);
		if (url != null) {
			Cartridge cart = ROMLoader.load(url);
			if (cart != null)
				screen.cartridgeInsert(cart, autoPower);
			return true;
		}

		// Then try File
		File file = getSingleFile(transf);
		if (file != null) {
			Cartridge cart = ROMLoader.load(file);
			if (cart != null)
				screen.cartridgeInsert(cart, autoPower);
			return true;
		}

		// Fail if none worked
		return false;
	}

	private String getSingleURL(Transferable transf) {
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

	private File getSingleFile(Transferable transf) {
		if (!transf.isDataFlavorSupported(DATA_FLAVOR_FILE_LIST)) return null;
		try {
			@SuppressWarnings("unchecked")
			List<File> data = (List<File>) transf.getTransferData(DATA_FLAVOR_FILE_LIST);
			if (data.size() != 1) return null;
			return data.get(0);
		} catch (Exception ex) {
			// Ignore
		}
		return null;
	}


	private final Screen screen;

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
	
	private static final long serialVersionUID = 1L;

}
