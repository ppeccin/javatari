// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.main;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.parameters.Parameters;
import org.javatari.pc.cartridge.ROMLoader;
import org.javatari.pc.room.Room;
import org.javatari.utils.Environment;


public final class TestStandalone {

	public static void main(String[] args) {

		// Initialize application environment
		Environment.init();
		
		// Load Parameters from properties file and process arguments
		Parameters.init(args);
		
		// Build a Room for Standalone play
		final Room room = Room.buildStandaloneRoom();

	 	// Insert test Cartridge
		final Cartridge cart = ROMLoader.load("file:///C:/cartridges/Hero.bin");
		if (cart != null) room.currentConsole().cartridgeSocket().insert(cart, false);

		// Turn everything on
		room.powerOn();

	 	// Keep logging info about clocks speeds achieved 
	 	(new Thread() { @Override public void run() {
		 	while(true) {
				System.out.println(room.currentConsole().mainClock() + ", " + room.screen().monitor().clock + ", " + room.speaker().clock);
				try { Thread.sleep(1000); } catch (InterruptedException e) {}
			}
	 	}}).start();

	}

}
