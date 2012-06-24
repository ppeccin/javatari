package main;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JApplet;

import parameters.Parameters;
import pc.cartridge.ROMLoader;
import pc.savestate.FileSaveStateMedia;
import pc.screen.ScreenWithConsolePanel;
import pc.speaker.Speaker;
import atari.cartridge.Cartridge;
import atari.console.Console;

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

		String hidePanelParam = getParameter("HIDE_CONSOLE_PANEL");
		boolean showConsolePanel = hidePanelParam == null || !hidePanelParam.toUpperCase().equals("TRUE");
		String backgroundParam = getParameter("BACKGROUND");
		System.out.println("Background color: " + backgroundParam);
		Integer backColor = backgroundParam != null ? Integer.parseInt(backgroundParam) : null;

		// Create components
		console = new Console();
		screen = new ScreenWithConsolePanel(true, showConsolePanel);
		screen.connect(console.videoOutput(), console.controlsSocket(), console.cartridgeSocket());
		speaker = new Speaker();
		speaker.connect(console.audioOutput());
		FileSaveStateMedia stateMedia = new FileSaveStateMedia();
		stateMedia.connect(console.saveStateSocket());
		
		// Add the screen to the Applet and set the background color
		setContentPane(screen);
		if (backColor != null) setBackground(new Color(backColor));
		
	 	// If a Cartridge is provided, read it
		if (Parameters.mainArg != null)
			cart = ROMLoader.load(Parameters.mainArg);
	}
	
	public void start() {
		// Turn AV monitors on
		screen.powerOn();                
	 	speaker.powerOn();

	 	// If a Cartridge is provided, insert it
		if (cart != null)
			console.cartridgeSocket().insert(cart, true);
		
		screen.requestFocus();
	}
	
	public void stop() {
		// Turn monitors and console off
	 	speaker.powerOff();
		screen.powerOff();                
		console.powerOff();
	}


	private Cartridge cart;
	private Console console;
	private ScreenWithConsolePanel screen;
	private Speaker speaker;
	
	private static final long serialVersionUID = 1L;

}
