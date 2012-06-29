// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.room;

import atari.console.Console;

public class StandaloneRoom extends Room {

	public StandaloneRoom() {
		super();
		buildPeripherals();
		buildConsole();
		connectConsole();
		insertCartridgeProvided();
	}

	StandaloneRoom(Room room) {
		super();
		copyPeripherals(room);
		buildConsole();
		connectConsole();
	}

	@Override
	protected void buildConsole() {
		console = new Console();
	}

}
