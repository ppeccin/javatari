// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package main;

import java.io.IOException;

import javax.swing.JOptionPane;

import parameters.Parameters;
import pc.room.Room;
import utils.Terminator;

public class MultiplayerServer {

	public static void main(String[] args) throws IOException {

		// Load Parameters from properties file and process arguments
		Parameters.init(args);

		// Build a ServerRoom for P1 Server play and turn everything on
		Room serverRoom = Room.buildServerRoom();
		serverRoom.powerOn();
		
		// Start listening for P2 Client connections
		try {
			serverRoom.serverCurrentConsole().remoteTransmitter().start();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Server start failed:\n" + ex, "Atari P1 Server", JOptionPane.ERROR_MESSAGE);
			Terminator.terminate();
		}

	}
				
}
