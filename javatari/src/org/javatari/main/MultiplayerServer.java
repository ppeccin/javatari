// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.main;

import java.io.IOException;

import javax.swing.JOptionPane;

import org.javatari.parameters.Parameters;
import org.javatari.pc.room.Room;
import org.javatari.utils.Environment;
import org.javatari.utils.SwingHelper;
import org.javatari.utils.Terminator;


public final class MultiplayerServer {

	public static void main(String[] args) throws IOException {

		// Initialize application environment
		Environment.init();

		// Load Parameters from properties file and process arguments
		Parameters.init(args);

		// Build a ServerRoom for P1 Server play and turn everything on
		final Room serverRoom = Room.buildServerRoom();
		serverRoom.powerOn();
		
		// Start listening for P2 Client connections
		startListening(serverRoom);

	}

	public static void startListening(final Room room) {
		SwingHelper.edtInvokeLater(new Runnable() { @Override public void run() {
			try {
				room.serverCurrentConsole().remoteTransmitter().start();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Server start failed:\n" + ex, "Atari P1 Server", JOptionPane.ERROR_MESSAGE);
				Terminator.terminate();
			}
		}});
	}
				
}
