// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package main;

import java.rmi.RemoteException;

import parameters.Parameters;
import pc.screen.DesktopScreenWindow;
import pc.speaker.Speaker;
import atari.network.ClientConsole;
import atari.network.socket.SocketRemoteReceiver;

public class MultiplayerClient {

	public static void main(String[] args) throws RemoteException {

		Parameters.init(args);
		
		// Use Socket implementation
		final SocketRemoteReceiver remoteReceiver = new SocketRemoteReceiver();

		// Create the Console
		final ClientConsole console = new ClientConsole(remoteReceiver);
		
		// Plug PC interfaces for Video, Audio, Controls, Cartridge and SaveState
		final DesktopScreenWindow screen = new DesktopScreenWindow(console.videoOutput(), console.controlsSocket(), console.cartridgeSocket());
		final Speaker speaker = new Speaker(console.audioOutput());
		
		// Automatically adjust interface for Multiplayer Client operation
		screen.consoleControls.p1ControlsMode(true);
		screen.screen.setCartridgeChangeEnabled(false);

		// Turn AV monitors on
		screen.powerOn();                
	 	speaker.powerOn();

 		// Try connection
 		boolean success = remoteReceiver.askUserForConnection(Parameters.mainArg);
 		if (!success) System.exit(1);
 		
	}

}
