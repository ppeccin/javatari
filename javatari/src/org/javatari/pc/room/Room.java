// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.room;

import java.awt.Component;
import java.security.AccessControlException;
import java.util.ArrayList;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.console.Console;
import org.javatari.atari.network.ClientConsole;
import org.javatari.atari.network.RemoteReceiver;
import org.javatari.atari.network.RemoteTransmitter;
import org.javatari.atari.network.ServerConsole;
import org.javatari.parameters.Parameters;
import org.javatari.pc.cartridge.BuiltInROM;
import org.javatari.pc.cartridge.ROMLoader;
import org.javatari.pc.controls.AWTConsoleControls;
import org.javatari.pc.controls.JoystickConsoleControls;
import org.javatari.pc.room.settings.SettingsDialog;
import org.javatari.pc.savestate.FileSaveStateMedia;
import org.javatari.pc.screen.DesktopScreenWindow;
import org.javatari.pc.screen.Screen;
import org.javatari.pc.speaker.Speaker;
import org.javatari.utils.Terminator;


public class Room {
	
	protected Room() {
		super();
	}

	public void powerOn() {
		screen.powerOn();
	 	speaker.powerOn();
	 	awtControls.powerOn();
	 	insertCartridgeProvidedIfNoneInserted();
	 	if (currentConsole.cartridgeSocket().inserted() != null) currentConsole.powerOn();
	}

	public void powerOff() {
	 	if (currentConsole != null) currentConsole.extendedPowerOff();
	 	awtControls.powerOff();
	 	speaker.powerOff();
		screen.powerOff();
	}

	public Console currentConsole() {
		return currentConsole;
	}

	public Console standaloneCurrentConsole() {
		if (currentConsole != standaloneConsole) throw new IllegalStateException("Not a Standalone Room");
		return standaloneConsole;
	}

	public ServerConsole serverCurrentConsole() {
		if (currentConsole != serverConsole) throw new IllegalStateException("Not a Server Room");
		return serverConsole;
	}

	public ClientConsole clientCurrentConsole() {
		if (currentConsole != clientConsole) throw new IllegalStateException("Not a Client Room");
		return clientConsole;
	}

	public Screen screen() {
		return screen;
	}

	public Speaker speaker() {
		return speaker;
	}
	
	public AWTConsoleControls awtControls() {
		return awtControls;
	}
	
	public JoystickConsoleControls joystickControls() {
		return awtControls.joystickControls();
	}

	public FileSaveStateMedia stateMedia() {
		return stateMedia;
	}

	public boolean isStandaloneMode() {
		return currentConsole == standaloneConsole;
	}

	public boolean isServerMode() {
		return currentConsole == serverConsole;
	}
	
	public boolean isClientMode() {
		return currentConsole == clientConsole;
	}
	
	public void morphToStandaloneMode() {
		if (isStandaloneMode()) return;
		powerOff();
		Cartridge lastCartridge = isClientMode() ? cartridgeProvided : currentConsole.cartridgeSocket().inserted();
		if (standaloneConsole == null) buildAndPlugStandaloneConsole();
		else plugConsole(standaloneConsole);
		adjustPeripheralsToStandaloneOrServerOperation();
		currentConsole.cartridgeSocket().insert(lastCartridge, false);
		powerOn();
	}

	public void morphToServerMode() {
		if (isServerMode()) return;
		powerOff();
		Cartridge lastCartridge = isClientMode() ? cartridgeProvided : currentConsole.cartridgeSocket().inserted();
		if (serverConsole == null) buildAndPlugServerConsole();
		else plugConsole(serverConsole);
		adjustPeripheralsToStandaloneOrServerOperation();
		currentConsole.cartridgeSocket().insert(lastCartridge, false);
		powerOn();
	}

	public void morphToClientMode() {
		if (isClientMode()) return;
		powerOff();
		if (clientConsole == null) buildAndPlugClientConsole();
		else plugConsole(clientConsole);
		adjustPeripheralsToClientOperation();
		powerOn();
	}

	public void openSettings(Component parent) {
		if (settingsDialog == null) settingsDialog = new SettingsDialog(this);
		settingsDialog.open(parent);
	}

	public void destroy() {
		powerOff();
		if (standaloneConsole != null) standaloneConsole.destroy();
		if (serverConsole != null) serverConsole.destroy();
		if (clientConsole != null) clientConsole.destroy();
		screen.destroy();
		speaker.destroy();
		awtControls.destroy();
		if (settingsDialog != null) {
			settingsDialog.setVisible(false);
			settingsDialog.dispose();
		}
		currentRoom = null;
	}
	
	protected void buildPeripherals() {
		// PC interfaces for Video, Audio, Controls, Cartridge and SaveState
		if (screen != null) throw new IllegalStateException();
		screen = buildScreenPeripheral();
		speaker = new Speaker();
		awtControls = new AWTConsoleControls();
		awtControls.connectScreen(screen);
		stateMedia = new FileSaveStateMedia();
	}

	protected Screen buildScreenPeripheral() {
		return buildDesktopScreenPeripheral();
	}

	public DesktopScreenWindow buildDesktopScreenPeripheral() {
		return new DesktopScreenWindow();
	}

	private void plugConsole(Console console) {
		if (currentConsole == console) return;
		currentConsole = console;
		screen.connect(currentConsole.videoOutput(), currentConsole.controlsSocket(), currentConsole.cartridgeSocket());
		speaker.connect(currentConsole.audioOutput());
		awtControls.connect(currentConsole.controlsSocket(), currentConsole.cartridgeSocket());
		stateMedia.connect(currentConsole.saveStateSocket());
	}
	
	private void insertCartridgeProvidedIfNoneInserted() {
		if (currentConsole.cartridgeSocket().inserted() != null) return;
		loadCartridgeProvided();
		if (cartridgeProvided != null) currentConsole.cartridgeSocket().insert(cartridgeProvided, false);
	}

	private void loadCartridgeProvided() {
		if (triedToLoadCartridgeProvided) return;
		triedToLoadCartridgeProvided = true;
		if (isClientMode()) return;
		// First try to load the first built-in ROM
		ArrayList<BuiltInROM> builtInROMs = BuiltInROM.all();
		if (builtInROMs.size() > 0) {
			cartridgeProvided = ROMLoader.load(builtInROMs.get(0));
			if (cartridgeProvided == null) Terminator.terminate();		// Error loading Cartridge
			Parameters.SCREEN_CARTRIDGE_CHANGE = false;					// Disable manual Cartridge change
			screen.monitor().setCartridgeChangeEnabled(false);
		} else {
			// If none try to load the Cartridge passed as argument
			if (Parameters.mainArg != null) {
				cartridgeProvided = ROMLoader.load(Parameters.mainArg, true);
				if (cartridgeProvided == null) Terminator.terminate();	// Error loading Cartridge
			}			
		}
	}

	protected Console buildAndPlugStandaloneConsole() {
		if (standaloneConsole != null) throw new IllegalStateException();
		standaloneConsole = new Console();
		plugConsole(standaloneConsole);
		return standaloneConsole;
	}

	protected ServerConsole buildAndPlugServerConsole() {
		if (serverConsole != null) throw new IllegalStateException();
		RemoteTransmitter remoteTransmitter = new RemoteTransmitter();
		serverConsole = new ServerConsole(remoteTransmitter);
		plugConsole(serverConsole);
		return serverConsole;
	}
	
	protected ClientConsole buildAndPlugClientConsole() {
		RemoteReceiver remoteReceiver = new RemoteReceiver();
		clientConsole = new ClientConsole(remoteReceiver);
		plugConsole(clientConsole);
		return clientConsole;
	}	

	protected void adjustPeripheralsToStandaloneOrServerOperation() {
		awtControls.p1ControlsMode(false);
		screen.monitor().setCartridgeChangeEnabled(Parameters.SCREEN_CARTRIDGE_CHANGE);
	}

	protected void adjustPeripheralsToClientOperation() {
		awtControls.p1ControlsMode(true);
		screen.monitor().setCartridgeChangeEnabled(false);
	}


	public static Room currentRoom() {
		return currentRoom;
	}

	public static Room buildStandaloneRoom() {
		if (currentRoom != null) throw new IllegalStateException("Room already built");
		currentRoom = new Room();
		currentRoom.buildPeripherals();
		currentRoom.adjustPeripheralsToStandaloneOrServerOperation();
		currentRoom.buildAndPlugStandaloneConsole();
		return currentRoom;
	}

	public static Room buildServerRoom() {
		if (currentRoom != null) throw new IllegalStateException("Room already built");
		currentRoom = new Room();
		currentRoom.buildPeripherals();
		currentRoom.adjustPeripheralsToStandaloneOrServerOperation();
		currentRoom.buildAndPlugServerConsole();
		return currentRoom;
	}

	public static Room buildClientRoom() {
		if (currentRoom != null) throw new IllegalStateException("Room already built");
		currentRoom = new Room();
		currentRoom.buildPeripherals();
		currentRoom.adjustPeripheralsToClientOperation();
		currentRoom.buildAndPlugClientConsole();
		return currentRoom;
	}

	public void exit() {
		try {
			destroy();
			System.out.println("<<<<<<<<<<<<  EXIT   >>>>>>>>>>>>>");
			System.exit(0);		
		} catch(AccessControlException ex) {
			// Ignore
		}
	}
	
	protected Console currentConsole;
	protected Console	standaloneConsole;
	protected ServerConsole serverConsole;
	protected ClientConsole clientConsole;

	protected Screen screen;
	protected Speaker speaker;
	protected AWTConsoleControls awtControls;
	protected FileSaveStateMedia stateMedia;
	protected Cartridge cartridgeProvided;
	protected boolean triedToLoadCartridgeProvided = false;
	protected SettingsDialog settingsDialog;
	
	
	protected static Room currentRoom;
		
}
