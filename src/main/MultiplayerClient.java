// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package main;

import java.io.IOException;

import javax.swing.JOptionPane;

import atari.network.RemoteReceiver;

import parameters.Parameters;
import pc.room.Room;
import utils.Terminator;

public class MultiplayerClient {

	public static void main(String[] args) throws IOException, InterruptedException {

		// Load Parameters from properties file and process arguments
		Parameters.init(args);

		// Build a ClientRoom for P2 Client play and turn everything on
		Room clientRoom = Room.buildClientRoom();
		clientRoom.powerOn();
		
		// Start connection to P1 Server
		boolean success = askUserForConnection(clientRoom.clientCurrentConsole().remoteReceiver(), Parameters.mainArg);
		if (!success) Terminator.terminate();
		
	}

	private static boolean askUserForConnection(RemoteReceiver remoteReceiver, String defaultServer) {
		String server = defaultServer;
 		while(true) {
			// Try connecting to server
 			if (server != null && !server.isEmpty()) {
 				try {
 					remoteReceiver.connect(server);
 					return true;
 				} catch(Exception ex) {
 					JOptionPane.showMessageDialog(null, "Unnable to connect to: " + server + "\n" + ex, "Atari Player 2 Client", JOptionPane.ERROR_MESSAGE);
 				}
 			}
			// If unsuccessful, ask for another address
	 		do {
				server = (String) JOptionPane.showInputDialog(
					null, "Atari Player 1 Server address:", "Atari Player 2 Client",
					JOptionPane.PLAIN_MESSAGE, null, null,
					server
				);
		 		if (server == null) return false;	// User chose cancel
		 		server = server.trim();
	 		} while(server.isEmpty());
 		}
	}

}
