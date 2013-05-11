package org.javatari.main;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JApplet;

import org.javatari.parameters.Parameters;
import org.javatari.pc.room.Room;
import org.javatari.utils.Environment;


public abstract class AbstractApplet extends JApplet {

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
		if (backgroundParam != null) {
			System.out.println("Background color: " + backgroundParam);
			setBackground(new Color(Integer.parseInt(backgroundParam)));
		}

		// Unless forced, disable FSEM when using Applets (Applet + FSEM bug)
		if (Parameters.SCREEN_USE_FSEM != 1) Parameters.SCREEN_USE_FSEM = 0;

		// Create an Applet Room
		room = buildRoom();

	}

	protected abstract Room buildRoom();
	
	@Override
	public void start() {
		// Turn everything on
		room.powerOn();
	}
	
	@Override
	public void stop() {
		// Turn everything off
		room.powerOff();
	}

	@Override
	public void destroy() {
		// Destroy the Room
		room.destroy();
	}


	protected Room room;
	
	private static final long serialVersionUID = 1L;

}
