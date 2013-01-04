// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package main;

import parameters.Parameters;
import pc.room.Room;

public final class Standalone {

	public static void main(String[] args) {

		// Load Parameters from properties file and process arguments
		Parameters.init(args);

		// Build a Room for Standalone play and turn everything on
		Room.buildStandaloneRoom().powerOn();

	}
				
}
