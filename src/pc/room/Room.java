// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.room;

import parameters.Parameters;
import pc.cartridge.ROMLoader;
import pc.controls.AWTConsoleControls;
import pc.room.settings.SettingsDialog;
import pc.savestate.FileSaveStateMedia;
import pc.screen.DesktopScreenWindow;
import pc.screen.Screen;
import pc.speaker.Speaker;
import utils.Terminator;
import atari.cartridge.Cartridge;
import atari.console.Console;
import atari.network.ClientConsole;
import atari.network.RemoteReceiver;
import atari.network.RemoteTransmitter;
import atari.network.ServerConsole;

public class Room {
	
	Room() {
		super();
	}

	public void powerOn() {
		screen.powerOn();
	 	speaker.powerOn();
	 	if (!isClientMode()) insertCartridgeProvidedIfNoneInserted();
	 	if (currentConsole.cartridgeSocket().inserted() != null) currentConsole.powerOn();
	}

	public void powerOff() {
	 	currentConsole.powerOff();
	 	speaker.powerOff();
		screen.powerOff();
	}

	public Console currentConsole() {
		return currentConsole;
	}

	public Console standaloneCurrentConsole() {
		if (currentConsole != standaloneConsole) throw new IllegalStateException();
		return standaloneConsole;
	}

	public ServerConsole serverCurrentConsole() {
		if (currentConsole != serverConsole) throw new IllegalStateException();
		return serverConsole;
	}

	public ClientConsole clientCurrentConsole() {
		if (currentConsole != clientConsole) throw new IllegalStateException();
		return clientConsole;
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
		Cartridge lastCartridge = isClientMode() ? null : currentConsole.cartridgeSocket().inserted();
		if (standaloneConsole == null) buildAndPlugStandaloneConsole();
		else plugConsole(standaloneConsole);
		adjustPeripheralsToStandaloneOrServerOperation();
		if (lastCartridge != null) currentConsole.cartridgeSocket().insert(lastCartridge, false);
		powerOn();
	}

	public void morphToServerMode() {
		if (isServerMode()) return;
		powerOff();
		Cartridge lastCartridge = isClientMode() ? null : currentConsole.cartridgeSocket().inserted();
		if (serverConsole == null) buildAndPlugServerConsole();
		else plugConsole(serverConsole);
		adjustPeripheralsToStandaloneOrServerOperation();
		if (lastCartridge != null) currentConsole.cartridgeSocket().insert(lastCartridge, false);
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

	public void destroy() {
		powerOff();
		if (standaloneConsole != null) standaloneConsole.destroy();
		if (serverConsole != null) serverConsole.destroy();
		if (clientConsole != null) clientConsole.destroy();
		screen.destroy();
		speaker.destroy();
		currentRoom = null;
	}
	
	protected void buildPeripherals() {
		// PC interfaces for Video, Audio, Controls, Cartridge and SaveState
		if (screen != null) throw new IllegalStateException();
		screen = buildScreenPeripheral();
		speaker = new Speaker();
		controls = new AWTConsoleControls(screen.monitor());
		controls.addInputComponents(screen.controlsInputComponents());
		stateMedia = new FileSaveStateMedia();
	}

	protected Screen buildScreenPeripheral() {
		return new DesktopScreenWindow();
	}

	private void plugConsole(Console console) {
		if (currentConsole == console) return;
		currentConsole = console;
		screen.connect(currentConsole.videoOutput(), currentConsole.controlsSocket(), currentConsole.cartridgeSocket());
		speaker.connect(currentConsole.audioOutput());
		controls.connect(currentConsole.controlsSocket());
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
		if (Parameters.mainArg == null) return;
		cartridgeProvided = ROMLoader.load(Parameters.mainArg);
		if (cartridgeProvided == null) Terminator.terminate();		// Error loading Cartridge
	}

	private Console buildAndPlugStandaloneConsole() {
		if (standaloneConsole != null) throw new IllegalStateException();
		standaloneConsole = new Console();
		plugConsole(standaloneConsole);
		return standaloneConsole;
	}

	private ServerConsole buildAndPlugServerConsole() {
		if (serverConsole != null) throw new IllegalStateException();
		RemoteTransmitter remoteTransmitter = new RemoteTransmitter();
		serverConsole = new ServerConsole(remoteTransmitter);
		plugConsole(serverConsole);
		return serverConsole;
	}
	
	private ClientConsole buildAndPlugClientConsole() {
		RemoteReceiver remoteReceiver = new RemoteReceiver();
		clientConsole = new ClientConsole(remoteReceiver);
		plugConsole(clientConsole);
		return clientConsole;
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

	public static Room buildAppletStandaloneRoom() {
		if (currentRoom != null) throw new IllegalStateException("Room already built");
		currentRoom = new AppletRoom();
		currentRoom.buildPeripherals();
		currentRoom.adjustPeripheralsToStandaloneOrServerOperation();
		currentRoom.buildAndPlugStandaloneConsole();
		return currentRoom;
	}

	private void adjustPeripheralsToStandaloneOrServerOperation() {
		currentRoom.controls().p1ControlsMode(false);
		currentRoom.screen().monitor().setCartridgeChangeEnabled(Parameters.SCREEN_CARTRIDGE_CHANGE);
	}

	private void adjustPeripheralsToClientOperation() {
		currentRoom.controls().p1ControlsMode(true);
		currentRoom.screen().monitor().setCartridgeChangeEnabled(false);
	}

	public static void openCurrentRoomSettings() {
		if (settingsDialog == null) settingsDialog = new SettingsDialog(currentRoom);
		settingsDialog.setVisible(true);
	}

	
	private Console currentConsole;
	private Console	standaloneConsole;
	private ServerConsole serverConsole;
	private ClientConsole clientConsole;

	private Screen screen;
	private Speaker speaker;
	private AWTConsoleControls controls;
	private FileSaveStateMedia stateMedia;
	private Cartridge cartridgeProvided;
	private boolean triedToLoadCartridgeProvided = false;
	
	private static Room currentRoom;
	private static SettingsDialog settingsDialog;
		
}
