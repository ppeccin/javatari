// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.room;

import javax.swing.RootPaneContainer;

import org.javatari.pc.screen.DesktopScreenWindow;
import org.javatari.pc.screen.PanelScreen;
import org.javatari.pc.screen.Screen;

public class EmbeddedRoom extends Room {

	private EmbeddedRoom(RootPaneContainer rootPaneContainer) {
		super();
		this.parentContainer = rootPaneContainer;
	}

	@Override
	protected Screen buildScreenPeripheral() {
		embeddedScreen = new PanelScreen(true);
		parentContainer.setContentPane(embeddedScreen);
		return embeddedScreen;
	}

	public void popUpScreen(boolean fullScreen) {
		if (screen != embeddedScreen) return;
		if (windowScreen == null) windowScreen = buildDesktopScreenPeripheral();
		currentConsole.pause();
		windowScreen.monitor().setCartridgeChangeEnabled(embeddedScreen.monitor().isCartridgeChangeEnabled());
		windowScreen.connect(currentConsole.videoOutput(), currentConsole.controlsSocket(), currentConsole.cartridgeSocket(), currentConsole.saveStateSocket());
		awtControls.connectScreen(windowScreen);
		windowScreen.powerOn(fullScreen);
		embeddedScreen.powerOff();
		embeddedScreen.setVisible(false);
		currentConsole.go();
		screen = windowScreen;
	}

	public void reembedScreen() {
		if (screen != windowScreen) return;
		currentConsole.pause();
		embeddedScreen.monitor().setCartridgeChangeEnabled(windowScreen.monitor().isCartridgeChangeEnabled());
		embeddedScreen.connect(currentConsole.videoOutput(), currentConsole.controlsSocket(), currentConsole.cartridgeSocket(), currentConsole.saveStateSocket());
		awtControls.connectScreen(embeddedScreen);
		embeddedScreen.powerOn();
		windowScreen.powerOff();
		windowScreen.setVisible(false);
		currentConsole.go();
		screen = embeddedScreen;
	}

	public void exit() {
		powerOff();
		// Does not end the entire VM...
	}
	

	public static Room buildStandaloneRoom(RootPaneContainer rootPaneContainer) {
		if (currentRoom != null) throw new IllegalStateException("Room already built");
		currentRoom = new EmbeddedRoom(rootPaneContainer);
		currentRoom.buildPeripherals();
		currentRoom.adjustPeripheralsToStandaloneOrServerOperation();
		currentRoom.buildAndPlugStandaloneConsole();
		return currentRoom;
	}

	public static Room buildServerRoom(RootPaneContainer rootPaneContainer) {
		if (currentRoom != null) throw new IllegalStateException("Room already built");
		currentRoom = new EmbeddedRoom(rootPaneContainer);
		currentRoom.buildPeripherals();
		currentRoom.adjustPeripheralsToStandaloneOrServerOperation();
		currentRoom.buildAndPlugServerConsole();
		return currentRoom;
	}

	public static Room buildClientRoom(RootPaneContainer rootPaneContainer) {
		if (currentRoom != null) throw new IllegalStateException("Room already built");
		currentRoom = new EmbeddedRoom(rootPaneContainer);
		currentRoom.buildPeripherals();
		currentRoom.adjustPeripheralsToClientOperation();
		currentRoom.buildAndPlugClientConsole();
		return currentRoom;
	}


	private final RootPaneContainer parentContainer;
	private PanelScreen embeddedScreen;
	private DesktopScreenWindow windowScreen;
}
