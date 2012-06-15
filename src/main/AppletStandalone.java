package main;

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

		// Create components
		console = new Console();
		screen = new ScreenWithConsolePanel(console.videoOutput(), console.controlsSocket(), console.cartridgeSocket(), showConsolePanel);
		speaker = new Speaker(console.audioOutput());
		new FileSaveStateMedia(console.saveStateSocket());
		
		// Add the screen to the Applet
		setContentPane(screen);
		
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
