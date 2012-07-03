package main;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JApplet;

import parameters.Parameters;
import pc.room.Room;
import pc.screen.PanelScreen;

public class AppletStandalone extends JApplet {

	public void init() {
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

		// Create an Applet Room
		room = Room.buildAppletStandaloneRoom();
		
		// Add the screen to the Applet and set the background color
		setContentPane((PanelScreen)room.screen());
		if (backColor != null) setBackground(new Color(backColor));
	}
	
	public void start() {
		// Turn everything on
		room.powerOn();
		((PanelScreen)room.screen()).requestFocus();
	}
	
	public void stop() {
		// Turn everything off
		room.powerOff();
	}

	@Override
	public void destroy() {
		// Destroy the Room
		room.destroy();
	}


	private Room room;
	
	private static final long serialVersionUID = 1L;

}
