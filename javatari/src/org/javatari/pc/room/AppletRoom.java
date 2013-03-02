// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.room;

import org.javatari.pc.screen.PanelScreen;
import org.javatari.pc.screen.Screen;

public final class AppletRoom extends Room {

	@Override
	protected Screen buildScreenPeripheral() {
		return new PanelScreen(true);
	}
	
}
