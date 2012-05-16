// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package main;

import java.net.MalformedURLException;
import java.net.URL;

import parameters.Parameters;
import pc.file.CartridgeLoader;
import pc.file.FileSaveStateMedia;
import pc.screen.Screen;
import pc.speaker.Speaker;
import atari.cartridge.Cartridge;
import atari.console.Console;

public class TestStandalone {

	public static void main(String[] args) throws MalformedURLException {

		Parameters.load();
		
		// Load cartridge
		final Cartridge cart = CartridgeLoader.load(new URL("file:///C:/cartridges/hero.bin"));

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
