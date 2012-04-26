// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package main;

import parameters.Parameters;
import pc.FileCartridgeReader;
import pc.FileSaveStateMedia;
import pc.Speaker;
import pc.screen.Screen;
import atari.cartridge.Cartridge;
import atari.console.Console;

public class TestStandalone {

	public static void main(String[] args) {

		Parameters.load();
		
		// Load cartridge passed, if any
		final Cartridge cart = FileCartridgeReader.read("c:/cartridges/ThrustDemo.bin");

		// Create the Console with the available Cartridge
		final Console console = cart != null ? new Console(cart) : new Console();
		
		// Plug PC interfaces for Video, Audio, Controls, Cartridge and SaveState
		final Screen screen = new Screen(console.videoOutput(), console.controlsSocket(), console.cartridgeSocket());
		final Speaker speaker = new Speaker(console.audioOutput());	
		new FileSaveStateMedia(console.saveStateSocket());  
		
		// Turn AV monitors on
	 	screen.powerOn();                
	 	speaker.powerOn();

	 	// If a Cartridge is inserted, turn the console on!
	 	if (cart != null) console.powerOn();

	 	// Keep logging info about clocks speeds achieved 
	 	(new Thread() { @Override public void run() {
		 	while(true) {
				System.out.println(console.mainClock() + ", " + screen.clock + ", " + speaker.clock);
				try { Thread.sleep(1000); } catch (InterruptedException e) {}
			}
	 	}}).start();
	 	
	}
				
}
