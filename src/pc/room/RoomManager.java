// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.room;

import pc.room.settings.SettingsDialog;

public class RoomManager {

	public static Room currentRoom() {
		return currentRoom;
	}
	
	public static StandaloneRoom buildStandaloneRoom() {
		if (currentRoom != null) throw new IllegalStateException("Room already built.");
		StandaloneRoom standaloneRoom = new StandaloneRoom();
		currentRoom = standaloneRoom;
		return standaloneRoom;
	}

	public static ServerRoom buildServerRoom() {
		if (currentRoom != null) throw new IllegalStateException("Room already built.");
		ServerRoom serverRoom = new ServerRoom();
		currentRoom = serverRoom;
		return serverRoom;
	}

	public static ClientRoom buildClientRoom() {
		if (currentRoom != null) throw new IllegalStateException("Room already built.");
		ClientRoom clientRoom = new ClientRoom();
		currentRoom = clientRoom;
		return clientRoom;
	}

	public static void openSettings() {
		if (settingsDialog == null) settingsDialog = new SettingsDialog();
		settingsDialog.setVisible(true);
	}

		
	private static Room currentRoom;
	private static SettingsDialog settingsDialog;

}
