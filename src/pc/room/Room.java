// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.room;

import parameters.Parameters;
import pc.cartridge.ROMLoader;
import pc.controls.AWTConsoleControls;
import pc.savestate.FileSaveStateMedia;
import pc.screen.DesktopScreenWindow;
import pc.screen.Screen;
import pc.speaker.Speaker;
import utils.Terminator;
import atari.cartridge.Cartridge;
import atari.console.Console;

public abstract class Room {

	public void powerOn() {
		screen.powerOn();
	 	speaker.powerOn();
	 	if (console.cartridgeSocket().inserted() != null) console.powerOn();
	}

	public void powerOff() {
	 	console.powerOff();
	 	speaker.powerOff();
		screen.powerOff();
	}

	public Console console() {
		return console;
	}

	public Screen screen() {
		return screen;
	}

	public Speaker speaker() {
		return speaker;
	}
	
	public AWTConsoleControls controls() {
		return controls;
	}
	
	public FileSaveStateMedia stateMedia() {
		return stateMedia;
	}

	protected abstract void buildConsole();
	
	protected void buildPeripherals() {
		// PC interfaces for Video, Audio, Controls, Cartridge and SaveState
		screen = new DesktopScreenWindow();
		speaker = new Speaker();
		controls = new AWTConsoleControls(screen.monitor());
		controls.addInputComponents(screen.controlsInputComponents());
		stateMedia = new FileSaveStateMedia();
	}

	protected void copyPeripherals(Room room) {
		// Copy peripherals from another Room
		screen = room.screen();
		speaker = room.speaker();
		controls = room.controls();
		stateMedia = room.stateMedia();
	}

	protected void connectConsole() {
		screen.connect(console.videoOutput(), console.controlsSocket(), console.cartridgeSocket());
		speaker.connect(console.audioOutput());
		controls.connect(console.controlsSocket());
		stateMedia.connect(console.saveStateSocket());
	}
	
	protected void insertCartridgeProvided() {
		if (Parameters.mainArg == null) return;
		Cartridge cart = ROMLoader.load(Parameters.mainArg);
		if (cart != null) console.cartridgeSocket().insert(cart, false);
		else Terminator.terminate();	// Error loading Cartridge
	}


	protected Console console;

	protected Screen screen;
	protected Speaker speaker;
	protected AWTConsoleControls controls;
	protected FileSaveStateMedia stateMedia;
		
}
