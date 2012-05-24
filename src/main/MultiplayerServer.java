// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package main;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import parameters.Parameters;
import pc.cartridge.ROMLoader;
import pc.savestate.FileSaveStateMedia;
import pc.screen.Screen;
import pc.speaker.Speaker;
import utils.Terminator;
import atari.cartridge.Cartridge;
import atari.network.ServerConsole;
import atari.network.socket.SocketRemoteTransmitter;

public class MultiplayerServer {

	public static void main(String[] args) throws RemoteException, AlreadyBoundException, NotBoundException {

		// Load Parameters from properties file and process arguments
		Parameters.init(args);
		
		// Use Socket implementation
		final SocketRemoteTransmitter remoteTransmitter = new SocketRemoteTransmitter();

		// Create the Console
		final ServerConsole console = new ServerConsole(remoteTransmitter);
		
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
	 	
	 	// Listen for client connection
		boolean success = remoteTransmitter.listen();
		if (!success) System.exit(1);
		 
	}
				
}
