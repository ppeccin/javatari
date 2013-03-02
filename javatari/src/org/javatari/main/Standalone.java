// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.main;

import org.javatari.parameters.Parameters;
import org.javatari.pc.room.Room;
import org.javatari.utils.Environment;


public final class Standalone {

	public static void main(String[] args) {

		// Initialize application environment
		Environment.init();

		// Load Parameters from properties file and process arguments
		Parameters.init(args);

		// Build a Room for Standalone play and turn everything on
		Room.buildStandaloneRoom().powerOn();

	}
				
}
