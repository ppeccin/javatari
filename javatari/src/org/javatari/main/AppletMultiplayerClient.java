package org.javatari.main;

import org.javatari.pc.room.EmbeddedRoom;
import org.javatari.pc.room.Room;


public final class AppletMultiplayerClient extends AbstractApplet {

	@Override
	protected Room buildRoom() {
		return EmbeddedRoom.buildClientRoom(this);
	}

	@Override
	public void start() {
		super.start();
		
		// Start connection to P1 Server
		if (room.isClientMode())
			MultiplayerClient.askUserForConnection(room);
	}
	
	
	private static final long serialVersionUID = 1L;

}
