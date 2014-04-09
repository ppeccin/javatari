// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.parameters;

import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.security.AccessControlException;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import org.javatari.utils.Terminator;


public final class Parameters {
	
	// Load Properties file and also process command line options, then load preferences. Order is relevant
	public static void init(String[] args) {
		parseMainArg(args);
		loadPropertiesFile();
		parseOptions(args);
		processProperties();
		loadPreferences();
	}

	public static void setDefaultKeysPreferences() {
		KEY_P0_LEFT    = DEFAULT_KEY_P0_LEFT;
		KEY_P0_UP      = DEFAULT_KEY_P0_UP;
		KEY_P0_RIGHT   = DEFAULT_KEY_P0_RIGHT;
		KEY_P0_DOWN    = DEFAULT_KEY_P0_DOWN;
		KEY_P0_BUTTON  = DEFAULT_KEY_P0_BUTTON;
		KEY_P0_BUTTON2 = DEFAULT_KEY_P0_BUTTON2;
		KEY_P1_LEFT    = DEFAULT_KEY_P1_LEFT;
		KEY_P1_UP      = DEFAULT_KEY_P1_UP;
		KEY_P1_RIGHT   = DEFAULT_KEY_P1_RIGHT;
		KEY_P1_DOWN    = DEFAULT_KEY_P1_DOWN;
		KEY_P1_BUTTON  = DEFAULT_KEY_P1_BUTTON;
		KEY_P1_BUTTON2 = DEFAULT_KEY_P1_BUTTON2;
	}
	
	public static void setDefaultJoystickPreferences() {
		JOY_P0_DEVICE          = DEFAULT_JOY_P0_DEVICE;
		JOY_P0_XAXIS           = DEFAULT_JOY_XAXIS;
		JOY_P0_XAXIS_SIGNAL    = DEFAULT_JOY_AXIS_SIGNAL;
		JOY_P0_YAXIS           = DEFAULT_JOY_YAXIS;
		JOY_P0_YAXIS_SIGNAL    = DEFAULT_JOY_AXIS_SIGNAL;
		JOY_P0_PAD_AXIS        = DEFAULT_JOY_PAD_AXIS;
		JOY_P0_PAD_AXIS_SIGNAL = DEFAULT_JOY_AXIS_SIGNAL;
		JOY_P0_BUTTON          = DEFAULT_JOY_BUTTON;
		JOY_P0_BUTTON2         = DEFAULT_JOY_BUTTON2;
		JOY_P0_SELECT          = DEFAULT_JOY_SELECT;
		JOY_P0_RESET           = DEFAULT_JOY_RESET;
		JOY_P0_PAUSE           = DEFAULT_JOY_PAUSE;
		JOY_P0_FAST_SPPED      = DEFAULT_JOY_FAST_SPPED;
		JOY_P0_DEADZONE        = DEFAULT_JOY_DEADZONE;
		JOY_P0_PADDLE_CENTER   = DEFAULT_JOY_PADDLE_CENTER;
		JOY_P0_PADDLE_SENS     = DEFAULT_JOY_PADDLE_SENS;
		JOY_P1_DEVICE          = DEFAULT_JOY_P1_DEVICE;
		JOY_P1_XAXIS           = DEFAULT_JOY_XAXIS;
		JOY_P1_XAXIS_SIGNAL    = DEFAULT_JOY_AXIS_SIGNAL;
		JOY_P1_YAXIS           = DEFAULT_JOY_YAXIS;
		JOY_P1_YAXIS_SIGNAL    = DEFAULT_JOY_AXIS_SIGNAL;
		JOY_P1_PAD_AXIS        = DEFAULT_JOY_PAD_AXIS;
		JOY_P1_PAD_AXIS_SIGNAL = DEFAULT_JOY_AXIS_SIGNAL;
		JOY_P1_BUTTON          = DEFAULT_JOY_BUTTON;
		JOY_P1_BUTTON2         = DEFAULT_JOY_BUTTON2;
		JOY_P1_SELECT          = DEFAULT_JOY_SELECT;
		JOY_P1_RESET           = DEFAULT_JOY_RESET;
		JOY_P1_PAUSE           = DEFAULT_JOY_PAUSE;
		JOY_P1_FAST_SPPED      = DEFAULT_JOY_FAST_SPPED;
		JOY_P1_DEADZONE        = DEFAULT_JOY_DEADZONE;
		JOY_P1_PADDLE_CENTER   = DEFAULT_JOY_PADDLE_CENTER;
		JOY_P1_PADDLE_SENS     = DEFAULT_JOY_PADDLE_SENS;		
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
				getResourceAsStream("javatari.properties");
		try {
			try {
				props.load(is);
			} finally {
				is.close();
			}
		} catch (Exception ex) {
			System.out.println("javatari.properties not found, using defaults");
		}
		// Try to replace properties by ones set via command line -D
		try {
			props.putAll(System.getProperties());
		} catch (AccessControlException ex) {
			// Give up
		}
	}

	private static void loadPreferences() {
		setDefaultJoystickPreferences();
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
			val = prefs.get("joyP0Device", null); if (val != null) JOY_P0_DEVICE = Integer.parseInt(val);
			val = prefs.get("joyP0XAxis", null); if (val != null) JOY_P0_XAXIS = Integer.parseInt(val);
			val = prefs.get("joyP0XAxisSignal", null); if (val != null) JOY_P0_XAXIS_SIGNAL = Integer.parseInt(val);
			val = prefs.get("joyP0YAxis", null); if (val != null) JOY_P0_YAXIS = Integer.parseInt(val);
			val = prefs.get("joyP0YAxisSignal", null); if (val != null) JOY_P0_YAXIS_SIGNAL = Integer.parseInt(val);
			val = prefs.get("joyP0Button", null); if (val != null) JOY_P0_BUTTON = Integer.parseInt(val);
			val = prefs.get("joyP0Button2", null); if (val != null) JOY_P0_BUTTON2 = Integer.parseInt(val);
			val = prefs.get("joyP0Reset", null); if (val != null) JOY_P0_RESET = Integer.parseInt(val);
			val = prefs.get("joyP0Select", null); if (val != null) JOY_P0_SELECT = Integer.parseInt(val);
			val = prefs.get("joyP0Pause", null); if (val != null) JOY_P0_PAUSE = Integer.parseInt(val);
			val = prefs.get("joyP0FastSpeed", null); if (val != null) JOY_P0_FAST_SPPED = Integer.parseInt(val);
			val = prefs.get("joyP0Deadzone", null); if (val != null) JOY_P0_DEADZONE = Integer.parseInt(val);
			val = prefs.get("joyP0PaddleCenter", null); if (val != null) JOY_P0_PADDLE_CENTER = Integer.parseInt(val);
			val = prefs.get("joyP0PaddleSens", null); if (val != null) JOY_P0_PADDLE_SENS = Integer.parseInt(val);
			val = prefs.get("joyP1Device", null); if (val != null) JOY_P1_DEVICE = Integer.parseInt(val);
			val = prefs.get("joyP1XAxis", null); if (val != null) JOY_P1_XAXIS = Integer.parseInt(val);
			val = prefs.get("joyP1XAxisSignal", null); if (val != null) JOY_P1_XAXIS_SIGNAL = Integer.parseInt(val);
			val = prefs.get("joyP1YAxis", null); if (val != null) JOY_P1_YAXIS = Integer.parseInt(val);
			val = prefs.get("joyP1YAxisSignal", null); if (val != null) JOY_P1_YAXIS_SIGNAL = Integer.parseInt(val);
			val = prefs.get("joyP1Button", null); if (val != null) JOY_P1_BUTTON = Integer.parseInt(val);
			val = prefs.get("joyP1Button2", null); if (val != null) JOY_P1_BUTTON2 = Integer.parseInt(val);
			val = prefs.get("joyP1Reset", null); if (val != null) JOY_P1_RESET = Integer.parseInt(val);
			val = prefs.get("joyP1Select", null); if (val != null) JOY_P1_SELECT = Integer.parseInt(val);
			val = prefs.get("joyP1Pause", null); if (val != null) JOY_P1_PAUSE = Integer.parseInt(val);
			val = prefs.get("joyP1FastSpeed", null); if (val != null) JOY_P1_FAST_SPPED = Integer.parseInt(val);
			val = prefs.get("joyP1Deadzone", null); if (val != null) JOY_P1_DEADZONE = Integer.parseInt(val);
			val = prefs.get("joyP1PaddleCenter", null); if (val != null) JOY_P1_PADDLE_CENTER = Integer.parseInt(val);
			val = prefs.get("joyP1PaddleSens", null); if (val != null) JOY_P1_PADDLE_SENS = Integer.parseInt(val);
			val = prefs.get("lastROMFileChosen", null); if (val != null) LAST_ROM_LOAD_FILE_CHOSEN = val;
			val = prefs.get("lastROMURLChosen", null); if (val != null) LAST_ROM_LOAD_URL_CHOSEN = val;
			val = prefs.get("lastROMSaveFileChosen", null); if (val != null) LAST_ROM_SAVE_FILE_CHOSEN = val;
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
			prefs.put("joyP0Device", String.valueOf(JOY_P0_DEVICE));
			prefs.put("joyP0XAxis", String.valueOf(JOY_P0_XAXIS));
			prefs.put("joyP0XAxisSignal", String.valueOf(JOY_P0_XAXIS_SIGNAL));
			prefs.put("joyP0YAxis", String.valueOf(JOY_P0_YAXIS));
			prefs.put("joyP0YAxisSignal", String.valueOf(JOY_P0_YAXIS_SIGNAL));
			prefs.put("joyP0PaddleAxis", String.valueOf(JOY_P0_PAD_AXIS));
			prefs.put("joyP0PaddleAxisSignal", String.valueOf(JOY_P0_PAD_AXIS_SIGNAL));
			prefs.put("joyP0Button", String.valueOf(JOY_P0_BUTTON));
			prefs.put("joyP0Button2", String.valueOf(JOY_P0_BUTTON2));
			prefs.put("joyP0Reset", String.valueOf(JOY_P0_RESET));
			prefs.put("joyP0Select", String.valueOf(JOY_P0_SELECT));
			prefs.put("joyP0Pause", String.valueOf(JOY_P0_PAUSE));
			prefs.put("joyP0FastSpeed", String.valueOf(JOY_P0_FAST_SPPED));
			prefs.put("joyP0Deadzone", String.valueOf(JOY_P0_DEADZONE));
			prefs.put("joyP0PaddleCenter", String.valueOf(JOY_P0_PADDLE_CENTER));
			prefs.put("joyP0PaddleSens", String.valueOf(JOY_P0_PADDLE_SENS));
			prefs.put("joyP1Device", String.valueOf(JOY_P1_DEVICE));
			prefs.put("joyP1XAxis", String.valueOf(JOY_P1_XAXIS));
			prefs.put("joyP1XAxisSignal", String.valueOf(JOY_P1_XAXIS_SIGNAL));
			prefs.put("joyP1YAxis", String.valueOf(JOY_P1_YAXIS));
			prefs.put("joyP10YAxisSignal", String.valueOf(JOY_P1_YAXIS_SIGNAL));
			prefs.put("joyP1PaddleAxis", String.valueOf(JOY_P1_PAD_AXIS));
			prefs.put("joyP1PaddleAxisSignal", String.valueOf(JOY_P1_PAD_AXIS_SIGNAL));
			prefs.put("joyP1Button", String.valueOf(JOY_P1_BUTTON));
			prefs.put("joyP1Button2", String.valueOf(JOY_P1_BUTTON2));
			prefs.put("joyP1Reset", String.valueOf(JOY_P1_RESET));
			prefs.put("joyP1Select", String.valueOf(JOY_P1_SELECT));
			prefs.put("joyP1Pause", String.valueOf(JOY_P1_PAUSE));
			prefs.put("joyP1FastSpeed", String.valueOf(JOY_P1_FAST_SPPED));
			prefs.put("joyP1Deadzone", String.valueOf(JOY_P1_DEADZONE));
			prefs.put("joyP1PaddleCenter", String.valueOf(JOY_P1_PADDLE_CENTER));
			prefs.put("joyP1PaddleSens", String.valueOf(JOY_P1_PADDLE_SENS));
			prefs.put("lastROMFileChosen", LAST_ROM_LOAD_FILE_CHOSEN);
			prefs.put("lastROMURLChosen", LAST_ROM_LOAD_URL_CHOSEN);
			prefs.put("lastROMSaveFileChosen", LAST_ROM_SAVE_FILE_CHOSEN);
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
			val = props.getProperty("TIA_AUDIO_SEND_CHUNK"); if (val != null) TIA_AUDIO_SEND_CHUNK = Integer.valueOf(val);
			val = props.getProperty("TIA_AUDIO_MIN_MONITOR_BUFFER_CHUNKS"); if (val != null) TIA_AUDIO_MIN_MONITOR_BUFFER_CHUNKS = Integer.valueOf(val);
			val = props.getProperty("TIA_AUDIO_MONITOR_BUFFER_CHUNKS_ADD_FACTOR"); if (val != null) TIA_AUDIO_MONITOR_BUFFER_CHUNKS_ADD_FACTOR = Integer.valueOf(val);

			val = props.getProperty("RAM_FRY_ZERO_BITS"); if (val != null) RAM_FRY_ZERO_BITS = Integer.valueOf(val);
			val = props.getProperty("RAM_FRY_ONE_BITS"); if (val != null) RAM_FRY_ONE_BITS = Integer.valueOf(val);
			val = props.getProperty("RAM_FRY_VARIANCE"); if (val != null) RAM_FRY_VARIANCE = Float.valueOf(val);
			val = props.getProperty("BUS_DATA_RETENTION"); if (val != null) BUS_DATA_RETENTION = Boolean.valueOf(val);
			val = props.getProperty("CONSOLE_ALTERNATE_CLOCK_FACTOR"); if (val != null) CONSOLE_ALTERNATE_CLOCK_FACTOR = Float.valueOf(val);

			val = props.getProperty("SCREEN_DEFAULT_FPS"); if (val != null) SCREEN_DEFAULT_FPS = Double.valueOf(val);
			val = props.getProperty("SCREEN_BUFFER_VSYNC"); if (val != null) SCREEN_BUFFER_VSYNC = Integer.valueOf(val);
			val = props.getProperty("SCREEN_BUFFER_SYNC_WAIT"); if (val != null) SCREEN_BUFFER_SYNC_WAIT = Boolean.valueOf(val);
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
			val = props.getProperty("SCREEN_CRT_FILTER"); if (val != null) SCREEN_CRT_FILTER = Boolean.valueOf(val);
			val = props.getProperty("SCREEN_CRT_MODE"); if (val != null) SCREEN_CRT_MODE = Integer.valueOf(val);
			val = props.getProperty("SCREEN_CRT_RETENTION_ALPHA"); if (val != null) SCREEN_CRT_RETENTION_ALPHA = Float.valueOf(val);
			val = props.getProperty("SCREEN_SCANLINES_STRENGTH"); if (val != null) SCREEN_SCANLINES_STRENGTH = Float.valueOf(val);
			val = props.getProperty("SCREEN_MULTI_BUFFERING"); if (val != null) SCREEN_MULTI_BUFFERING = Integer.valueOf(val);
			val = props.getProperty("SCREEN_PAGE_FLIPPING"); if (val != null) SCREEN_PAGE_FLIPPING = Boolean.valueOf(val);
			val = props.getProperty("SCREEN_CONSOLE_PANEL"); if (val != null) SCREEN_CONSOLE_PANEL = Boolean.valueOf(val);
			val = props.getProperty("SCREEN_FRAME_ACCELERATION"); if (val != null) SCREEN_FRAME_ACCELERATION = Float.valueOf(val);
			val = props.getProperty("SCREEN_INTERM_FRAME_ACCELERATION"); if (val != null) SCREEN_INTERM_FRAME_ACCELERATION = Float.valueOf(val);
			val = props.getProperty("SCREEN_SCANLINES_ACCELERATION"); if (val != null) SCREEN_SCANLINES_ACCELERATION = Float.valueOf(val);
			val = props.getProperty("SCREEN_FIXED_SIZE"); if (val != null) SCREEN_FIXED_SIZE = Boolean.valueOf(val);
			val = props.getProperty("SCREEN_FULLSCREEN"); if (val != null) SCREEN_FULLSCREEN = Boolean.valueOf(val);
			val = props.getProperty("SCREEN_USE_FSEM"); if (val != null) SCREEN_USE_FSEM = Integer.valueOf(val);
			val = props.getProperty("SCREEN_EMBEDDED_POPUP"); if (val != null) SCREEN_EMBEDDED_POPUP = Boolean.valueOf(val);
			val = props.getProperty("SCREEN_CARTRIDGE_CHANGE"); if (val != null) SCREEN_CARTRIDGE_CHANGE = Boolean.valueOf(val);

			val = props.getProperty("SPEAKER_DEFAULT_FPS"); if (val != null) SPEAKER_DEFAULT_FPS = Double.valueOf(val);
			val = props.getProperty("SPEAKER_INPUT_BUFFER_SIZE"); if (val != null) SPEAKER_INPUT_BUFFER_SIZE = Integer.valueOf(val);
			val = props.getProperty("SPEAKER_OUTPUT_BUFFER_SIZE"); if (val != null) SPEAKER_OUTPUT_BUFFER_SIZE = Integer.valueOf(val);
			val = props.getProperty("SPEAKER_OUTPUT_BUFFER_FULL_SLEEP_TIME"); if (val != null) SPEAKER_OUTPUT_BUFFER_FULL_SLEEP_TIME = Integer.valueOf(val);
			val = props.getProperty("SPEAKER_NO_DATA_SLEEP_TIME"); if (val != null) SPEAKER_NO_DATA_SLEEP_TIME = Integer.valueOf(val);
			val = props.getProperty("SPEAKER_ADDED_THREAD_PRIORITY"); if (val != null) SPEAKER_ADDED_THREAD_PRIORITY = Integer.valueOf(val);

			val = props.getProperty("MULTIPLAYER_UI"); if (val != null) MULTIPLAYER_UI = Boolean.valueOf(val);
			val = props.getProperty("SERVER_SERVICE_PORT"); if (val != null) SERVER_SERVICE_PORT = Integer.valueOf(val);
			val = props.getProperty("SERVER_MAX_UPDATES_PENDING"); if (val != null) SERVER_MAX_UPDATES_PENDING = Integer.valueOf(val);
			val = props.getProperty("CLIENT_MAX_UPDATES_PENDING"); if (val != null) CLIENT_MAX_UPDATES_PENDING = Integer.valueOf(val);

			val = props.getProperty("CARTRIDGE_NAME"); if (val != null && !val.isEmpty()) CARTRIDGE_NAME = String.valueOf(val);
			val = props.getProperty("CARTRIDGE_LABEL"); if (val != null && !val.isEmpty()) CARTRIDGE_LABEL = String.valueOf(val);
			val = props.getProperty("CARTRIDGE_LABEL_COLORS"); if (val != null && !val.isEmpty()) CARTRIDGE_LABEL_COLORS = String.valueOf(val);
			val = props.getProperty("CARTRIDGE_PADDLES"); if (val != null && !val.isEmpty()) CARTRIDGE_PADDLES = Integer.valueOf(val);
			val = props.getProperty("CARTRIDGE_CRT_MODE"); if (val != null && !val.isEmpty()) CARTRIDGE_CRT_MODE = Integer.valueOf(val);
			val = props.getProperty("CARTRIDGE_FORMAT"); if (val != null && !val.isEmpty()) CARTRIDGE_FORMAT = String.valueOf(val);

			val = props.getProperty("PADDLES_MODE"); if (val != null && !val.isEmpty()) PADDLES_MODE = Integer.valueOf(val);
			val = props.getProperty("JOYSTICK_UPDATE_RATE"); if (val != null && !val.isEmpty()) JOYSTICK_UPDATE_RATE = Integer.valueOf(val);
			
			if (CARTRIDGE_LABEL_COLORS != null) {
				setCartridgeLabelColors(CARTRIDGE_LABEL_COLORS);
			}
		} catch(Exception ex) {
			System.out.println("Error processing properties:\n" + ex);
			Terminator.terminate();
		}
	}


	public static void setCartridgeLabelColors(String colorsSpec) {
		try {
			String[] colors = colorsSpec.trim().split("\\s");
			CARTRIDGE_LABEL_COLOR = (colors.length >= 1) ? Integer.valueOf(colors[0], 16) : -1;
			CARTRIDGE_BACK_COLOR = (colors.length >= 2) ? Integer.valueOf(colors[1], 16) : -1;
			CARTRIDGE_BORDER_COLOR = (colors.length >= 3) ? Integer.valueOf(colors[2], 16) : -1;
		} catch (Exception e) {
			System.out.println("Error parsing CARTRIDGE_LABEL_COLORS: " + e.getMessage());
		}
	}

	
	// Cartridge URL to load passed as argument
	public static String mainArg = null;

	// Main Version info
	public static final String TITLE = "Javatari";
	public static final String VERSION = "4.0";

	// DEFAULTS

	public static double	TIA_FORCED_CLOCK = 0;							//  0 = No Forced Clock
	public static boolean 	TIA_SYNC_WITH_AUDIO_MONITOR = false;
	public static boolean 	TIA_SYNC_WITH_VIDEO_MONITOR = false;

	public static int 		TIA_AUDIO_SAMPLE_RATE = 31440;					// A little less than TIA Scanline frequency * 2 = 31440
	public static float 	TIA_AUDIO_MAX_AMPLITUDE = 0.5f;
	public static int 		TIA_AUDIO_SEND_CHUNK = 106;
	public static int 		TIA_AUDIO_MIN_MONITOR_BUFFER_CHUNKS = 7;
	public static int 		TIA_AUDIO_MONITOR_BUFFER_CHUNKS_ADD_FACTOR = 10;

	public static double	VIDEO_NTSC_FPS = 60;
	public static double	VIDEO_PAL_FPS = 50.3846153846153847;
	
	public static int 		RAM_FRY_ZERO_BITS = 120;						// Quantity of bits to change
	public static int 		RAM_FRY_ONE_BITS = 25;
	public static float 	RAM_FRY_VARIANCE = 0.3f;
	public static boolean 	BUS_DATA_RETENTION = true;
	public static float		CONSOLE_ALTERNATE_CLOCK_FACTOR = 20;

	public static double	SCREEN_DEFAULT_FPS = -1;						// 0 = External Synch, -1 = Auto FPS (On Demand)
	public static int	 	SCREEN_BUFFER_VSYNC = -1;
	public static boolean	SCREEN_BUFFER_SYNC_WAIT = false;
	public static int 		SCREEN_DEFAULT_ORIGIN_X = 68;
	public static double 	SCREEN_DEFAULT_ORIGIN_Y_PCT = 12.4;				// Percentage of height	
	public static int 		SCREEN_DEFAULT_WIDTH = 160;
	public static double 	SCREEN_DEFAULT_HEIGHT_PCT = 81.5;				// Percentage of height
	public static float 	SCREEN_DEFAULT_SCALE_X = 4;
	public static float 	SCREEN_DEFAULT_SCALE_Y = 2;
	public static float 	SCREEN_DEFAULT_SCALE_ASPECT_X = 2;				// X = 2 * Y
	public static int 		SCREEN_BORDER_SIZE = 3;
	public static int 		SCREEN_OSD_FRAMES = 160;
	public static int 		SCREEN_VSYNC_TOLERANCE = 16;
	public static boolean 	SCREEN_CRT_FILTER = false;
	public static int	 	SCREEN_CRT_MODE = 0;
	public static float	 	SCREEN_CRT_RETENTION_ALPHA = 0.75f;
	public static float 	SCREEN_SCANLINES_STRENGTH = 0.5f;
	public static int	 	SCREEN_MULTI_BUFFERING = 2;
	public static boolean 	SCREEN_PAGE_FLIPPING = true;
	public static boolean 	SCREEN_CONSOLE_PANEL = true;
	public static float		SCREEN_FRAME_ACCELERATION = -1;
	public static float		SCREEN_INTERM_FRAME_ACCELERATION = -1;
	public static float		SCREEN_SCANLINES_ACCELERATION = -1;
	public static boolean 	SCREEN_FIXED_SIZE = false;
	public static boolean 	SCREEN_FULLSCREEN = false;
	public static int	 	SCREEN_USE_FSEM = -1;
	public static boolean 	SCREEN_EMBEDDED_POPUP = true;
	public static boolean 	SCREEN_CARTRIDGE_CHANGE = true;
	
	public static double	SPEAKER_DEFAULT_FPS = -1;						// 0 = External Synch, -1 = Auto FPS (On Demand)
	public static int		SPEAKER_INPUT_BUFFER_SIZE = 1872;				// In frames (samples)
	public static int		SPEAKER_OUTPUT_BUFFER_SIZE = 1248;				// In frames (samples)
	public static int		SPEAKER_OUTPUT_BUFFER_FULL_SLEEP_TIME = 5;		// In milliseconds
	public static int		SPEAKER_NO_DATA_SLEEP_TIME = 5;					// In milliseconds
	public static int		SPEAKER_ADDED_THREAD_PRIORITY = 0;

	public static boolean 	MULTIPLAYER_UI = true;
	public static int 		SERVER_SERVICE_PORT = 9998;
	public static int 		SERVER_MAX_UPDATES_PENDING = 20;
	public static int 		CLIENT_MAX_UPDATES_PENDING = 20;

	public static String 	CARTRIDGE_NAME = null;
	public static String 	CARTRIDGE_LABEL = null;
	public static String	CARTRIDGE_LABEL_COLORS = null;
	public static int		CARTRIDGE_LABEL_COLOR = -1;
	public static int		CARTRIDGE_BACK_COLOR = -1;
	public static int 		CARTRIDGE_BORDER_COLOR = -1;
	public static int	 	CARTRIDGE_PADDLES = -1;
	public static int	 	CARTRIDGE_CRT_MODE = -1;
	public static String 	CARTRIDGE_FORMAT = null;

	public static String 	DEFAULT_CARTRIDGE_LABEL = "JAVATARI 2600";
	public static int	 	DEFAULT_CARTRIDGE_LABEL_COLOR = 0xeb2820;
	public static int	 	DEFAULT_CARTRIDGE_BACK_COLOR = 0x141414;

	public static int		PADDLES_MODE = -1;								// -1 = AUTO, 0 = Force OFF, 1 = Force ON
	public static int		JOYSTICK_UPDATE_RATE = 120;						// In Hz

	public static String 	OFFICIAL_WEBSITE = "http://javatari.org";
	public static String 	TWITTER_WEBPAGE = "http://twitter.com/ppeccin";
	
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

	public static final int CONTROL_UNSET = -1;
	public static final int JOY_DEVICE_NONE = -1;
	public static final int JOY_DEVICE_AUTO = -99;

	public static final int DEFAULT_JOY_P0_DEVICE     = JOY_DEVICE_AUTO;
	public static final int DEFAULT_JOY_P1_DEVICE     = JOY_DEVICE_AUTO;
	public static final int DEFAULT_JOY_XAXIS         = 0;
	public static final int DEFAULT_JOY_YAXIS         = 1;
	public static final int DEFAULT_JOY_PAD_AXIS   	  = 0;
	public static final int DEFAULT_JOY_AXIS_SIGNAL   = 1;
	public static final int DEFAULT_JOY_BUTTON        = 0;
	public static final int DEFAULT_JOY_BUTTON2       = 1;
	public static final int DEFAULT_JOY_SELECT        = 6;
	public static final int DEFAULT_JOY_RESET         = 7;
	public static final int DEFAULT_JOY_FAST_SPPED    = 4;
	public static final int DEFAULT_JOY_PAUSE  		  = 5;
	public static final int DEFAULT_JOY_DEADZONE      = 30;
	public static final int DEFAULT_JOY_PADDLE_CENTER = 0;
	public static final int DEFAULT_JOY_PADDLE_SENS   = 90;

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
 
	public static int JOY_P0_DEVICE          = DEFAULT_JOY_P0_DEVICE;
	public static int JOY_P0_XAXIS           = DEFAULT_JOY_XAXIS;
	public static int JOY_P0_XAXIS_SIGNAL    = DEFAULT_JOY_AXIS_SIGNAL;
	public static int JOY_P0_YAXIS           = DEFAULT_JOY_YAXIS;
	public static int JOY_P0_YAXIS_SIGNAL    = DEFAULT_JOY_AXIS_SIGNAL;
	public static int JOY_P0_PAD_AXIS        = DEFAULT_JOY_PAD_AXIS;
	public static int JOY_P0_PAD_AXIS_SIGNAL = DEFAULT_JOY_AXIS_SIGNAL;
	public static int JOY_P0_BUTTON          = DEFAULT_JOY_BUTTON;
	public static int JOY_P0_BUTTON2         = DEFAULT_JOY_BUTTON2;
	public static int JOY_P0_SELECT          = DEFAULT_JOY_SELECT;
	public static int JOY_P0_RESET           = DEFAULT_JOY_RESET;
	public static int JOY_P0_PAUSE           = DEFAULT_JOY_PAUSE;
	public static int JOY_P0_FAST_SPPED      = DEFAULT_JOY_FAST_SPPED;
	public static int JOY_P0_DEADZONE        = DEFAULT_JOY_DEADZONE;
	public static int JOY_P0_PADDLE_CENTER   = DEFAULT_JOY_PADDLE_CENTER;
	public static int JOY_P0_PADDLE_SENS     = DEFAULT_JOY_PADDLE_SENS;
	public static int JOY_P1_DEVICE          = DEFAULT_JOY_P1_DEVICE;
	public static int JOY_P1_XAXIS           = DEFAULT_JOY_XAXIS;
	public static int JOY_P1_XAXIS_SIGNAL    = DEFAULT_JOY_AXIS_SIGNAL;
	public static int JOY_P1_YAXIS           = DEFAULT_JOY_YAXIS;
	public static int JOY_P1_YAXIS_SIGNAL    = DEFAULT_JOY_AXIS_SIGNAL;
	public static int JOY_P1_PAD_AXIS        = DEFAULT_JOY_PAD_AXIS;
	public static int JOY_P1_PAD_AXIS_SIGNAL = DEFAULT_JOY_AXIS_SIGNAL;
	public static int JOY_P1_BUTTON          = DEFAULT_JOY_BUTTON;
	public static int JOY_P1_BUTTON2         = DEFAULT_JOY_BUTTON2;
	public static int JOY_P1_SELECT          = DEFAULT_JOY_SELECT;
	public static int JOY_P1_RESET           = DEFAULT_JOY_RESET;
	public static int JOY_P1_PAUSE           = DEFAULT_JOY_PAUSE;
	public static int JOY_P1_FAST_SPPED      = DEFAULT_JOY_FAST_SPPED;
	public static int JOY_P1_DEADZONE        = DEFAULT_JOY_DEADZONE;
	public static int JOY_P1_PADDLE_CENTER   = DEFAULT_JOY_PADDLE_CENTER;
	public static int JOY_P1_PADDLE_SENS     = DEFAULT_JOY_PADDLE_SENS;

	public static String LAST_ROM_LOAD_FILE_CHOSEN = "";
	public static String LAST_ROM_LOAD_URL_CHOSEN = "";
	public static String LAST_ROM_SAVE_FILE_CHOSEN = "";

}
