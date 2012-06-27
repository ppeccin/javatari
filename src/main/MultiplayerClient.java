// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package main;

import java.io.IOException;

import parameters.Parameters;
import pc.room.ClientRoom;
import pc.room.RoomManager;
import utils.Terminator;

public class MultiplayerClient {

	public static void main(String[] args) throws IOException, InterruptedException {

		// Load Parameters from properties file and process arguments
		Parameters.init(args);

		// Build a ClientRoom for P2 Client play and turn everything on
		ClientRoom clientRoom = RoomManager.buildClientRoom();
		clientRoom.powerOn();
		
		// Start connection to P1 Server
		boolean success = clientRoom.askUserForConnection(Parameters.mainArg);
		if (!success) Terminator.terminate();
		
	}

}
