// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package main;

import parameters.Parameters;
import pc.cartridge.ROMLoader;
import pc.savestate.FileSaveStateMedia;
import pc.screen.Screen;
import pc.speaker.Speaker;
import utils.Terminator;
import atari.cartridge.Cartridge;
import atari.console.Console;

public class Standalone {

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

	 	// If a Cartridge is provided, insert it
		if (Parameters.mainArg != null) {
			Cartridge cart = ROMLoader.load(Parameters.mainArg);
			if (cart == null) Terminator.terminate();
			console.cartridgeSocket().insert(cart, true);
		}

	}
				
}
