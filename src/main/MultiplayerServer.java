// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package main;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import parameters.Parameters;
import pc.file.CartridgeLoader;
import pc.file.FileSaveStateMedia;
import pc.screen.Screen;
import pc.speaker.Speaker;
import atari.cartridge.Cartridge;
import atari.network.ServerConsole;
import atari.network.socket.SocketRemoteTransmitter;

public class MultiplayerServer {

	public static void main(String[] args) throws RemoteException, MalformedURLException, AlreadyBoundException, NotBoundException {

		Parameters.load();
		
		// Load cartridge passed, if any
		final Cartridge cart = (args.length > 0) ? CartridgeLoader.load(new URL(args[0])) : null;

		// Use Socket implementation
		final SocketRemoteTransmitter remoteTransmitter = new SocketRemoteTransmitter();

		// Create the Console with the available Cartridge
		final ServerConsole console = cart != null ? new ServerConsole(remoteTransmitter, cart): new ServerConsole(remoteTransmitter);
		
		// Plug PC interfaces for Video, Audio, Controls, Cartridge and SaveState
		final Screen screen = new Screen(console.videoOutput(), console.controlsSocket(), console.cartridgeSocket());
		final Speaker speaker = new Speaker(console.audioOutput());
		new FileSaveStateMedia(console.saveStateSocket());
		
		// Turn AV monitors on
		screen.powerOn();                
	 	speaker.powerOn();
 	
	 	// If a Cartridge is inserted, turn the console on!
	 	if (cart != null) console.powerOn();
	 	
	 	// Listen for client connection
		boolean success = remoteTransmitter.listen();
		if (!success) System.exit(1);
		 
	}
				
}
