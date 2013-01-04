// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.room;

import pc.screen.PanelScreen;
import pc.screen.Screen;

public final class AppletRoom extends Room {

	@Override
	protected Screen buildScreenPeripheral() {
		return new PanelScreen(true);
	}
	
}
