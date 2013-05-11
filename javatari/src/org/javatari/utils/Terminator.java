// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.utils;

import org.javatari.pc.room.Room;

public final class Terminator {

	public static void terminate() {
		if (Room.currentRoom() != null) Room.currentRoom().exit();
		throw new IllegalStateException("Emulator terminated");
	}
	
}
