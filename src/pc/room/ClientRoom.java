// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.room;

import java.io.IOException;

import javax.swing.JOptionPane;

import atari.network.ClientConsole;
import atari.network.socket.SocketRemoteReceiver;

public class ClientRoom extends Room {
	
	public void startClient(String server) throws IOException {
 		remoteReceiver.start(server);
	}

	public void stopClient() throws IOException {
 		remoteReceiver.stop();
	}

	public boolean askUserForConnection(String defaultServer) {
		String server = defaultServer;
 		while(true) {
			// Try connecting to server
 			if (server != null && !server.isEmpty()) {
 				try {
 					startClient(server);
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
	
	@Override
	protected void buildPeripherals() {
		super.buildPeripherals();
		// Automatically adjust interface for Multiplayer Client operation
		controls.p1ControlsMode(true);
		screen.monitor().setCartridgeChangeEnabled(false);
	}

	@Override
	protected void buildConsole() {
		remoteReceiver = new SocketRemoteReceiver();
		console = new ClientConsole(remoteReceiver);
	}

	@Override
	protected void insertCartridgeProvided() {
		// P2 Clientw never change Cartridge
	}

	private SocketRemoteReceiver remoteReceiver;

}
