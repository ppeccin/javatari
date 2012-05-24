// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package main;

import parameters.Parameters;
import pc.cartridge.ROMLoader;
import pc.savestate.FileSaveStateMedia;
import pc.screen.Screen;
import pc.speaker.Speaker;
import atari.cartridge.Cartridge;
import atari.console.Console;

public class TestStandalone {

	public static void main(String[] args) {

		// Load Parameters from properties file and process arguments
		Parameters.init(args);
		
		// Create the Console
		final Console console = new Console();
		
		// Plug PC interfaces for Video, Audio, Controls, Cartridge and SaveState
		final Screen screen = new Screen(console.videoOutput(), console.controlsSocket(), console.cartridgeSocket());
		final Speaker speaker = new Speaker(console.audioOutput());	
		new FileSaveStateMedia(console.saveStateSocket());  
		
		// Turn AV monitors on
	 	screen.powerOn();                
	 	speaker.powerOn();

	 	// Insert test Cartridge
		final Cartridge cart = ROMLoader.load("file:///C:/cartridges/hero.bin");
	 	if (cart != null) console.cartridgeSocket().insert(cart, true);

	 	// Keep logging info about clocks speeds achieved 
	 	(new Thread() { @Override public void run() {
		 	while(true) {
				System.out.println(console.mainClock() + ", " + screen.clock + ", " + speaker.clock);
				try { Thread.sleep(1000); } catch (InterruptedException e) {}
			}
	 	}}).start();
	 	
	}
				
}
