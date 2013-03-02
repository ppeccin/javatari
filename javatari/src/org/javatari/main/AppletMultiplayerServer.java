package org.javatari.main;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JApplet;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.javatari.parameters.Parameters;
import org.javatari.pc.room.Room;
import org.javatari.pc.screen.PanelScreen;
import org.javatari.utils.Environment;
import org.javatari.utils.Terminator;


public final class AppletMultiplayerServer extends JApplet {

	@Override
	public void init() {
		// Initialize application environment
		Environment.init();

		// Builds an Array of args from the Applet parameters to mimic command line args
		ArrayList<String> args = new ArrayList<String>();
		for (int i = -1; i < 50; i++) {
			String paramName = "ARG" + (i >= 0 ? i : "");
			String paramValue = getParameter(paramName);
			if (paramValue != null) args.add(paramValue);
		}
		
		// Load Parameters from properties file and process arguments
		Parameters.init(args.toArray(new String[0]));

		// Process background color parameter if present
		String backgroundParam = getParameter("BACKGROUND");
		System.out.println("Background color: " + backgroundParam);
		Integer backColor = backgroundParam != null ? Integer.parseInt(backgroundParam) : null;

		// Create an Applet Server Room
		room = Room.buildAppletServerRoom();
		
		// Add the screen to the Applet and set the background color
		setContentPane((PanelScreen)room.screen());
		if (backColor != null) setBackground(new Color(backColor));
	}
	
	@Override
	public void start() {
		// Turn everything on
		room.powerOn();
		((PanelScreen)room.screen()).requestFocus();
		
		SwingUtilities.invokeLater(new Runnable() { @Override public void run() {
			// Start listening for P2 Client connections
			try {
				room.serverCurrentConsole().remoteTransmitter().start();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Server start failed:\n" + ex, "Atari P1 Server", JOptionPane.ERROR_MESSAGE);
				Terminator.terminate();
			}
		}});
	}
	
	@Override
	public void stop() {
		// Turn everything off
		room.powerOff();
		
		// Stop listening for P2 Client connections
		try {
			room.serverCurrentConsole().remoteTransmitter().stop();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Error stopping Server:\n" + ex, "javatari P1 Server", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void destroy() {
		// Destroy the Room
		room.destroy();
	}


	private Room room;
	
	private static final long serialVersionUID = 1L;

}
