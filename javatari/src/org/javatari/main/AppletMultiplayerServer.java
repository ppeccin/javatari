package org.javatari.main;

import org.javatari.pc.room.EmbeddedRoom;
import org.javatari.pc.room.Room;


public final class AppletMultiplayerServer extends AbstractApplet {

	@Override
	protected Room buildRoom() {
		return EmbeddedRoom.buildServerRoom(this);
	}

	@Override
	public void start() {
		super.start();
		
		// Start listening for P2 Client connections
		if (room.isServerMode())
			MultiplayerServer.startListening(room);
	}
	

	private static final long serialVersionUID = 1L;

}
