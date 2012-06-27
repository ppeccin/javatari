// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.room;

import atari.console.Console;

public class StandaloneRoom extends Room {

	@Override
	protected void buildConsole() {
		console = new Console();
	}

}
