// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package main;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import parameters.Parameters;
import pc.screen.Screen;
import pc.speaker.Speaker;
import atari.network.ClientConsole;
import atari.network.socket.SocketRemoteReceiver;

public class MultiplayerClient {

	public static void main(String[] args) throws RemoteException, MalformedURLException, AlreadyBoundException, NotBoundException {

		Parameters.load();
		
		// Use Socket implementation
		final SocketRemoteReceiver remoteReceiver = new SocketRemoteReceiver();

		// Create the Console
		final ClientConsole console = new ClientConsole(remoteReceiver);
		
		// Plug PC interfaces for Video, Audio, Controls, Cartridge and SaveState
		final Screen screen = new Screen(console.videoOutput(), console.controlsSocket(), console.cartridgeSocket());
		screen.p1ControlsMode(true);
		final Speaker speaker = new Speaker(console.audioOutput());
		
		// Turn AV monitors on
		screen.powerOn();                
	 	speaker.powerOn();

 		// Try connection
 		boolean success = remoteReceiver.askUserForConnection(args.length > 0 ? args[0] : null);
 		if (!success) System.exit(1);
 		
	}

}
