// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package parameters;

import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.security.AccessControlException;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.UIManager;

import utils.Terminator;

public final class Parameters {
	
	static{
		setLookAndFeel();
	}

	// Load Properties file and also process command line options, then load preferences. Order is relevant
	public static void init(String[] args) {
		System.out.println(System.getProperty("java.vm.name") + " ver: " + System.getProperty("java.version"));
		parseMainArg(args);
		loadPropertiesFile();
		parseOptions(args);
		processProperties();
		loadPreferences();
	}

	private static void setLookAndFeel() {
		try {
			 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			// Give up
		}
	}
	
	private static void parseMainArg(String[] args) {
		for (String arg : args)
			if (!arg.startsWith("-")) {
				mainArg = arg;
				return;
			}
	}

	private static void parseOptions(String[] args) {
		for (String arg : args) {
			if (!arg.startsWith("-")) continue;
			String opt = arg.substring(1);
			Pattern p = Pattern.compile("=");
			String[] params = p.split(opt);
			if (params == null || params.length != 2 || params[0].isEmpty() || params[1].isEmpty()) {
				System.out.println("Invalid option format: " + arg);
				Terminator.terminate();
			}
			props.put(params[0].toUpperCase(), params[1]);
		}
	}

	private static void loadPropertiesFile() {
		InputStream is = Thread.currentThread().getContextClassLoader().
				getResourceAsStream("parameters/javatari.properties");
		try {
			try {
				props.load(is);
			} finally {
				is.close();
			}
		} catch (Exception ex) {
			System.out.println("parameters/javatari.properties not found, using defaults");
		}
		// Try to replace properties by ones set via command line -D
		try {
			props.putAll(System.getProperties());
		} catch (AccessControlException ex) {
			// Give up
		}
	}

	private static void loadPreferences() {
		Preferences prefs = getUserPreferences();
		if (prefs == null) return;
		String val;
		try {
			val = prefs.get("keyP0Left", null); if (val != null) KEY_P0_LEFT = Integer.parseInt(val);
			val = prefs.get("keyP0Up", null); if (val != null) KEY_P0_UP = Integer.parseInt(val);
			val = prefs.get("keyP0Right", null); if (val != null) KEY_P0_RIGHT = Integer.parseInt(val);
			val = prefs.get("keyP0Down", null); if (val != null) KEY_P0_DOWN = Integer.parseInt(val);
			val = prefs.get("keyP0Button", null); if (val != null) KEY_P0_BUTTON = Integer.parseInt(val);
			val = prefs.get("keyP0Button2", null); if (val != null) KEY_P0_BUTTON2 = Integer.parseInt(val);
			val = prefs.get("keyP1Left", null); if (val != null) KEY_P1_LEFT = Integer.parseInt(val);
			val = prefs.get("keyP1Up", null); if (val != null) KEY_P1_UP = Integer.parseInt(val);
			val = prefs.get("keyP1Right", null); if (val != null) KEY_P1_RIGHT = Integer.parseInt(val);
			val = prefs.get("keyP1Down", null); if (val != null) KEY_P1_DOWN = Integer.parseInt(val);
			val = prefs.get("keyP1Button", null); if (val != null) KEY_P1_BUTTON = Integer.parseInt(val);
			val = prefs.get("keyP1Button2", null); if (val != null) KEY_P1_BUTTON2 = Integer.parseInt(val);
			val = prefs.get("lastROMFileChosen", null); if (val != null) LAST_ROM_FILE_CHOSEN = val;
			val = prefs.get("lastROMURLChosen", null); if (val != null) LAST_ROM_URL_CHOSEN = val;
		} catch (Exception e) {
			// Give up
		}
	}
	
	public static void savePreferences() {
		Preferences prefs = getUserPreferences();
		if (prefs == null) return;
		try {
			prefs.put("keyP0Left", String.valueOf(KEY_P0_LEFT));
			prefs.put("keyP0Up", String.valueOf(KEY_P0_UP));
			prefs.put("keyP0Right", String.valueOf(KEY_P0_RIGHT));
			prefs.put("keyP0Down", String.valueOf(KEY_P0_DOWN));
			prefs.put("keyP0Button", String.valueOf(KEY_P0_BUTTON));
			prefs.put("keyP0Button2", String.valueOf(KEY_P0_BUTTON2));
			prefs.put("keyP1Left", String.valueOf(KEY_P1_LEFT));
			prefs.put("keyP1Up", String.valueOf(KEY_P1_UP));
			prefs.put("keyP1Right", String.valueOf(KEY_P1_RIGHT));
			prefs.put("keyP1Down", String.valueOf(KEY_P1_DOWN));
			prefs.put("keyP1Button", String.valueOf(KEY_P1_BUTTON));
			prefs.put("keyP1Button2", String.valueOf(KEY_P1_BUTTON2));
		 	prefs.put("lastROMFileChosen", LAST_ROM_FILE_CHOSEN);
			prefs.put("lastROMURLChosen", LAST_ROM_URL_CHOSEN);
		} catch (Exception e) {
			// Give up
		}
	}
	
	private static Preferences getUserPreferences() {
		if (!userPreferencesAsked)
			try{
				userPreferencesAsked = true;
				userPreferences = Preferences.userRoot().node("javatari");
			} catch(AccessControlException ex) {
				// Give up
			}
		return userPreferences;
	}

	private static void processProperties() {
		String val;
		try {
			val = props.getProperty("TIA_FORCED_CLOCK"); if (val != null) TIA_FORCED_CLOCK = Double.valueOf(val);
			val = props.getProperty("TIA_SYNC_WITH_AUDIO_MONITOR"); if (val != null) TIA_SYNC_WITH_AUDIO_MONITOR = Boolean.valueOf(val);
			val = props.getProperty("TIA_SYNC_WITH_VIDEO_MONITOR"); if (val != null) TIA_SYNC_WITH_VIDEO_MONITOR = Boolean.valueOf(val);

			val = props.getProperty("VIDEO_NTSC_FPS"); if (val != null) VIDEO_NTSC_FPS = Double.valueOf(val);
			val = props.getProperty("VIDEO_PAL_FPS"); if (val != null) VIDEO_PAL_FPS = Double.valueOf(val);

			val = props.getProperty("TIA_AUDIO_SAMPLE_RATE"); if (val != null) TIA_AUDIO_SAMPLE_RATE = Integer.valueOf(val);
			val = props.getProperty("TIA_AUDIO_MAX_AMPLITUDE"); if (val != null) TIA_AUDIO_MAX_AMPLITUDE = Float.valueOf(val);

			val = props.getProperty("RAM_FRY_ZERO_BITS"); if (val != null) RAM_FRY_ZERO_BITS = Integer.valueOf(val);
			val = props.getProperty("RAM_FRY_ONE_BITS"); if (val != null) RAM_FRY_ONE_BITS = Integer.valueOf(val);
			val = props.getProperty("RAM_FRY_VARIANCE"); if (val != null) RAM_FRY_VARIANCE = Float.valueOf(val);

			val = props.getProperty("SCREEN_DEFAULT_FPS"); if (val != null) SCREEN_DEFAULT_FPS = Double.valueOf(val);
			val = props.getProperty("SCREEN_DEFAULT_ORIGIN_X"); if (val != null) SCREEN_DEFAULT_ORIGIN_X = Integer.valueOf(val);
			val = props.getProperty("SCREEN_DEFAULT_ORIGIN_Y_PCT"); if (val != null) SCREEN_DEFAULT_ORIGIN_Y_PCT = Double.valueOf(val);
			val = props.getProperty("SCREEN_DEFAULT_WIDTH"); if (val != null) SCREEN_DEFAULT_WIDTH = Integer.valueOf(val);
			val = props.getProperty("SCREEN_DEFAULT_HEIGHT_PCT"); if (val != null) SCREEN_DEFAULT_HEIGHT_PCT = Double.valueOf(val);
			val = props.getProperty("SCREEN_DEFAULT_SCALE_X"); if (val != null) SCREEN_DEFAULT_SCALE_X = Float.valueOf(val);
			val = props.getProperty("SCREEN_DEFAULT_SCALE_Y"); if (val != null) SCREEN_DEFAULT_SCALE_Y = Float.valueOf(val);
			val = props.getProperty("SCREEN_DEFAULT_SCALE_ASPECT_X"); if (val != null) SCREEN_DEFAULT_SCALE_ASPECT_X = Float.valueOf(val);
			val = props.getProperty("SCREEN_BORDER_SIZE"); if (val != null) SCREEN_BORDER_SIZE = Integer.valueOf(val);
			val = props.getProperty("SCREEN_OSD_FRAMES"); if (val != null) SCREEN_OSD_FRAMES = Integer.valueOf(val);
			val = props.getProperty("SCREEN_VSYNC_TOLERANCE"); if (val != null) SCREEN_VSYNC_TOLERANCE = Integer.valueOf(val);
			val = props.getProperty("SCREEN_QUALITY_RENDERING"); if (val != null) SCREEN_QUALITY_RENDERING = Boolean.valueOf(val);
			val = props.getProperty("SCREEN_CRT_MODE"); if (val != null) SCREEN_CRT_MODE = Integer.valueOf(val);
			val = props.getProperty("SCREEN_CRT_RETENTION_ALPHA"); if (val != null) SCREEN_CRT_RETENTION_ALPHA = Float.valueOf(val);
			val = props.getProperty("SCREEN_SCANLINES_STRENGTH"); if (val != null) SCREEN_SCANLINES_STRENGTH = Float.valueOf(val);
			val = props.getProperty("SCREEN_MULTI_BUFFERING"); if (val != null) SCREEN_MULTI_BUFFERING = Integer.valueOf(val);
			val = props.getProperty("SCREEN_PAGE_FLIPPING"); if (val != null) SCREEN_PAGE_FLIPPING = Boolean.valueOf(val);
			val = props.getProperty("SCREEN_BUFFER_VSYNC"); if (val != null) SCREEN_BUFFER_VSYNC = Integer.valueOf(val);
			val = props.getProperty("SCREEN_FRAME_ACCELERATION"); if (val != null) SCREEN_FRAME_ACCELERATION = Float.valueOf(val);
			val = props.getProperty("SCREEN_INTERM_FRAME_ACCELERATION"); if (val != null) SCREEN_INTERM_FRAME_ACCELERATION = Float.valueOf(val);
			val = props.getProperty("SCREEN_SCANLINES_ACCELERATION"); if (val != null) SCREEN_SCANLINES_ACCELERATION = Float.valueOf(val);
			val = props.getProperty("SCREEN_CARTRIDGE_CHANGE"); if (val != null) SCREEN_CARTRIDGE_CHANGE = Boolean.valueOf(val);
			val = props.getProperty("SCREEN_CONSOLE_PANEL"); if (val != null) SCREEN_CONSOLE_PANEL = Boolean.valueOf(val);
			val = props.getProperty("SCREEN_FIXED_SIZE"); if (val != null) SCREEN_FIXED_SIZE = Boolean.valueOf(val);
			val = props.getProperty("SCREEN_FULLSCREEN"); if (val != null) SCREEN_FULLSCREEN = Boolean.valueOf(val);

			val = props.getProperty("SPEAKER_DEFAULT_FPS"); if (val != null) SPEAKER_DEFAULT_FPS = Double.valueOf(val);
			val = props.getProperty("SPEAKER_INPUT_BUFFER_SIZE"); if (val != null) SPEAKER_INPUT_BUFFER_SIZE = Integer.valueOf(val);
			val = props.getProperty("SPEAKER_OUTPUT_BUFFER_SIZE"); if (val != null) SPEAKER_OUTPUT_BUFFER_SIZE = Integer.valueOf(val);
			val = props.getProperty("SPEAKER_OUTPUT_BUFFER_FULL_SLEEP_TIME"); if (val != null) SPEAKER_OUTPUT_BUFFER_FULL_SLEEP_TIME = Integer.valueOf(val);
			val = props.getProperty("SPEAKER_NO_DATA_SLEEP_TIME"); if (val != null) SPEAKER_NO_DATA_SLEEP_TIME = Integer.valueOf(val);
			val = props.getProperty("SPEAKER_ADDED_THREAD_PRIORITY"); if (val != null) SPEAKER_ADDED_THREAD_PRIORITY = Integer.valueOf(val);

			val = props.getProperty("CONSOLE_FAST_SPEED_FACTOR"); if (val != null) CONSOLE_FAST_SPEED_FACTOR = Integer.valueOf(val);

			val = props.getProperty("BUS_DATA_RETENTION"); if (val != null) BUS_DATA_RETENTION = Boolean.valueOf(val);

			val = props.getProperty("SERVER_SERVICE_PORT"); if (val != null) SERVER_SERVICE_PORT = Integer.valueOf(val);
			val = props.getProperty("SERVER_MAX_UPDATES_PENDING"); if (val != null) SERVER_MAX_UPDATES_PENDING = Integer.valueOf(val);
			val = props.getProperty("CLIENT_MAX_UPDATES_PENDING"); if (val != null) CLIENT_MAX_UPDATES_PENDING = Integer.valueOf(val);

			val = props.getProperty("MULTIPLAYER_UI"); if (val != null) MULTIPLAYER_UI = Boolean.valueOf(val);
		} catch(Exception ex) {
			System.out.println("Error processing properties:\n" + ex);
			Terminator.terminate();
		}
	}

	// Main Emulator Version
	public static final String VERSION = "version 3.00";

	
	// Cartridge URL to load passed as argument
	public static String mainArg = null;

	// DEFAULTS

	public static double	TIA_FORCED_CLOCK = 0;							//  0 = No Forced Clock
	public static boolean 	TIA_SYNC_WITH_AUDIO_MONITOR = false;
	public static boolean 	TIA_SYNC_WITH_VIDEO_MONITOR = false;

	public static int 		TIA_AUDIO_SAMPLE_RATE = 31430;					// A little less than TIA Scanline frequency * 2 = 31440
	public static float 	TIA_AUDIO_MAX_AMPLITUDE = 0.5f;

	public static double	VIDEO_NTSC_FPS = 60;
	public static double	VIDEO_PAL_FPS = 50.384615;
	
	public static int 		RAM_FRY_ZERO_BITS = 100;						// Quantity of bits to change
	public static int 		RAM_FRY_ONE_BITS = 25;
	public static float 	RAM_FRY_VARIANCE = 0.3f;

	public static double	SCREEN_DEFAULT_FPS = -1;						// 0 = External Synch, -1 = Auto FPS (On Demand)
	public static int 		SCREEN_DEFAULT_ORIGIN_X = 10;
	public static double 	SCREEN_DEFAULT_ORIGIN_Y_PCT = 12;				// Percentage of height	
	public static int 		SCREEN_DEFAULT_WIDTH = 160;
	public static double 	SCREEN_DEFAULT_HEIGHT_PCT = 81.5;				// Percentage of height
	public static float 	SCREEN_DEFAULT_SCALE_X = 4;
	public static float 	SCREEN_DEFAULT_SCALE_Y = 2;
	public static float 	SCREEN_DEFAULT_SCALE_ASPECT_X = 2;				// X = 2 * Y
	public static int 		SCREEN_BORDER_SIZE = 3;
	public static int 		SCREEN_OSD_FRAMES = 160;
	public static int 		SCREEN_VSYNC_TOLERANCE = 10;
	public static boolean 	SCREEN_QUALITY_RENDERING = false;
	public static int	 	SCREEN_CRT_MODE = 0;
	public static float	 	SCREEN_CRT_RETENTION_ALPHA = 0.75f;
	public static float 	SCREEN_SCANLINES_STRENGTH = 0.5f;
	public static int	 	SCREEN_MULTI_BUFFERING = 2;
	public static boolean 	SCREEN_PAGE_FLIPPING = true;
	public static int	 	SCREEN_BUFFER_VSYNC = -1;
	public static float		SCREEN_FRAME_ACCELERATION = 0;
	public static float		SCREEN_INTERM_FRAME_ACCELERATION = -1;
	public static float		SCREEN_SCANLINES_ACCELERATION = -1;
	public static boolean 	SCREEN_CARTRIDGE_CHANGE = true;
	public static boolean 	SCREEN_CONSOLE_PANEL = true;
	public static boolean 	SCREEN_FIXED_SIZE = false;
	public static boolean 	SCREEN_FULLSCREEN = false;
	
	public static double	SPEAKER_DEFAULT_FPS = -1;						// 0 = External Synch, -1 = Auto FPS (On Demand)
	public static int		SPEAKER_INPUT_BUFFER_SIZE = 1536;				// In frames (samples)
	public static int		SPEAKER_OUTPUT_BUFFER_SIZE = 768;				// In frames (samples)
	public static int		SPEAKER_OUTPUT_BUFFER_FULL_SLEEP_TIME = 5;		// In milliseconds
	public static int		SPEAKER_NO_DATA_SLEEP_TIME = 20;				// In milliseconds
	public static int		SPEAKER_ADDED_THREAD_PRIORITY = 0;

	public static int		CONSOLE_FAST_SPEED_FACTOR = 8;
	
	public static boolean 	BUS_DATA_RETENTION = true;

	public static int 		SERVER_SERVICE_PORT = 9998;
	public static int 		SERVER_MAX_UPDATES_PENDING = 20;
	public static int 		CLIENT_MAX_UPDATES_PENDING = 20;

	public static boolean 	MULTIPLAYER_UI = true;

	public static String 	OFFICIAL_WEBSITE = "http://javatari.org";
	
	private static Properties props = new Properties();
	
	private static Preferences userPreferences;
	private static boolean userPreferencesAsked = false;

	// DEFATULS for the customizable preferences below
	
	public static final int DEFAULT_KEY_P0_LEFT    = KeyEvent.VK_LEFT;
	public static final int DEFAULT_KEY_P0_UP      = KeyEvent.VK_UP;
	public static final int DEFAULT_KEY_P0_RIGHT   = KeyEvent.VK_RIGHT;
	public static final int DEFAULT_KEY_P0_DOWN    = KeyEvent.VK_DOWN;
	public static final int DEFAULT_KEY_P0_BUTTON  = KeyEvent.VK_SPACE;
	public static final int DEFAULT_KEY_P0_BUTTON2 = KeyEvent.VK_DELETE;
	public static final int DEFAULT_KEY_P1_LEFT    = KeyEvent.VK_F;
	public static final int DEFAULT_KEY_P1_UP      = KeyEvent.VK_T;
	public static final int DEFAULT_KEY_P1_RIGHT   = KeyEvent.VK_H;
	public static final int DEFAULT_KEY_P1_DOWN    = KeyEvent.VK_G;
	public static final int DEFAULT_KEY_P1_BUTTON  = KeyEvent.VK_A;
	public static final int DEFAULT_KEY_P1_BUTTON2 = KeyEvent.VK_PERIOD;

	
	// The following parameters can be customized as user preferences, not via properties file or command line

	public static int KEY_P0_LEFT    = DEFAULT_KEY_P0_LEFT;
	public static int KEY_P0_UP      = DEFAULT_KEY_P0_UP;
	public static int KEY_P0_RIGHT   = DEFAULT_KEY_P0_RIGHT;
	public static int KEY_P0_DOWN    = DEFAULT_KEY_P0_DOWN;
	public static int KEY_P0_BUTTON  = DEFAULT_KEY_P0_BUTTON;
	public static int KEY_P0_BUTTON2 = DEFAULT_KEY_P0_BUTTON2;
	public static int KEY_P1_LEFT    = DEFAULT_KEY_P1_LEFT;
	public static int KEY_P1_UP      = DEFAULT_KEY_P1_UP;
	public static int KEY_P1_RIGHT   = DEFAULT_KEY_P1_RIGHT;
	public static int KEY_P1_DOWN    = DEFAULT_KEY_P1_DOWN;
	public static int KEY_P1_BUTTON  = DEFAULT_KEY_P1_BUTTON;
	public static int KEY_P1_BUTTON2 = DEFAULT_KEY_P1_BUTTON2;
 
	public static String LAST_ROM_FILE_CHOSEN = "";
	public static String LAST_ROM_URL_CHOSEN = "";

}
