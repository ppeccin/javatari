// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package parameters;

import java.io.InputStream;
import java.security.AccessControlException;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import utils.Terminator;

public class Parameters {

	// Load Properties file and also process command line options and parameters
	public static void init(String[] args) {
		parseMainArg(args);
		loadPropertiesFile();
		parseOptions(args);
		processProperties();
	}
	
	public static String readPreference(String name) {
		if (getUserPreferences() == null) return null;
		return getUserPreferences().get(name, "");
	}

	public static boolean storePreference(String name, String value) {
		if (getUserPreferences() == null) return false;
		getUserPreferences().put(name, value);
		return true;
	}

	public static Preferences getUserPreferences() {
		if (!userPreferencesAsked)
			try{
				userPreferencesAsked = true;
				userPreferences = Preferences.userRoot().node("javatari");
			} catch(AccessControlException ex) {
				// Ignore
			}
		return userPreferences;
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
			// Ignore
		}
	}

	private static void processProperties() {
		String val;
		try {

			val = props.getProperty("TIA_FORCED_CLOCK"); if (val != null) TIA_FORCED_CLOCK = Double.valueOf(val);
			val = props.getProperty("TIA_DEFAULT_CLOCK_NTSC"); if (val != null) TIA_DEFAULT_CLOCK_NTSC = Double.valueOf(val);
			val = props.getProperty("TIA_DEFAULT_CLOCK_PAL"); if (val != null) TIA_DEFAULT_CLOCK_PAL = Double.valueOf(val);
			val = props.getProperty("TIA_SYNC_WITH_AUDIO_MONITOR"); if (val != null) TIA_SYNC_WITH_AUDIO_MONITOR = Boolean.valueOf(val);
			val = props.getProperty("TIA_SYNC_WITH_VIDEO_MONITOR"); if (val != null) TIA_SYNC_WITH_VIDEO_MONITOR = Boolean.valueOf(val);

			val = props.getProperty("TIA_AUDIO_SAMPLE_RATE"); if (val != null) TIA_AUDIO_SAMPLE_RATE = Integer.valueOf(val);
			val = props.getProperty("TIA_AUDIO_MAX_AMPLITUDE"); if (val != null) TIA_AUDIO_MAX_AMPLITUDE = Float.valueOf(val);
			val = props.getProperty("TIA_AUDIO_MAX_MONO_CHANNEL_AMPLITUDE"); if (val != null) TIA_AUDIO_MAX_MONO_CHANNEL_AMPLITUDE = Float.valueOf(val);
			val = props.getProperty("TIA_AUDIO_MAX_STEREO_CHANNEL_AMPLITUDE"); if (val != null) TIA_AUDIO_MAX_STEREO_CHANNEL_AMPLITUDE = Float.valueOf(val);

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
			val = props.getProperty("SCREEN_FULLSCREEN"); if (val != null) SCREEN_FULLSCREEN = Boolean.valueOf(val);

			val = props.getProperty("SPEAKER_DEFAULT_FPS"); if (val != null) SPEAKER_DEFAULT_FPS = Double.valueOf(val);
			val = props.getProperty("SPEAKER_INPUT_BUFFER_SIZE"); if (val != null) SPEAKER_INPUT_BUFFER_SIZE = Integer.valueOf(val);
			val = props.getProperty("SPEAKER_OUTPUT_BUFFER_SIZE"); if (val != null) SPEAKER_OUTPUT_BUFFER_SIZE = Integer.valueOf(val);
			val = props.getProperty("SPEAKER_OUTPUT_BUFFER_FULL_SLEEP_TIME"); if (val != null) SPEAKER_OUTPUT_BUFFER_FULL_SLEEP_TIME = Integer.valueOf(val);
			val = props.getProperty("SPEAKER_NO_DATA_SLEEP_TIME"); if (val != null) SPEAKER_NO_DATA_SLEEP_TIME = Integer.valueOf(val);
			val = props.getProperty("SPEAKER_ADDED_THREAD_PRIORITY"); if (val != null) SPEAKER_ADDED_THREAD_PRIORITY = Integer.valueOf(val);

			val = props.getProperty("CONSOLE_FAST_SPEED_FACTOR"); if (val != null) CONSOLE_FAST_SPEED_FACTOR = Integer.valueOf(val);

			val = props.getProperty("BUS_DATA_RETENTION"); if (val != null) BUS_DATA_RETENTION = Boolean.valueOf(val);

			val = props.getProperty("SERVER_SERVICE_NAME"); if (val != null) SERVER_SERVICE_NAME = String.valueOf(val);
			val = props.getProperty("SERVER_SERVICE_PORT"); if (val != null) SERVER_SERVICE_PORT = Integer.valueOf(val);
			val = props.getProperty("SERVER_MAX_UPDATES_PENDING"); if (val != null) SERVER_MAX_UPDATES_PENDING = Integer.valueOf(val);
			val = props.getProperty("CLIENT_MAX_UPDATES_PENDING"); if (val != null) CLIENT_MAX_UPDATES_PENDING = Integer.valueOf(val);

		} catch(Exception ex) {
			System.out.println("Error processing properties:\n" + ex);
			Terminator.terminate();
		}
	}



	// Cartridge URL to load passed as argument
	public static String mainArg = null;

	
	// DEFAULTS

	public static double	TIA_FORCED_CLOCK = 0;							//  0 = No Forced Clock
	public static double	TIA_DEFAULT_CLOCK_NTSC = 60;
	public static double	TIA_DEFAULT_CLOCK_PAL = 50.384615;
	public static boolean 	TIA_SYNC_WITH_AUDIO_MONITOR = false;
	public static boolean 	TIA_SYNC_WITH_VIDEO_MONITOR = false;

	public static int 		TIA_AUDIO_SAMPLE_RATE = 31430;					// A little less than TIA Scanline frequency * 2 = 31440
	public static float 	TIA_AUDIO_MAX_AMPLITUDE = 0.5f;
	public static float 	TIA_AUDIO_MAX_MONO_CHANNEL_AMPLITUDE = 0.5f;
	public static float 	TIA_AUDIO_MAX_STEREO_CHANNEL_AMPLITUDE = 0.9f;

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
	public static boolean 	SCREEN_FULLSCREEN = false;
	
	public static double	SPEAKER_DEFAULT_FPS = -1;						// 0 = External Synch, -1 = Auto FPS (On Demand)
	public static int		SPEAKER_INPUT_BUFFER_SIZE = 1536;				// In frames (samples)
	public static int		SPEAKER_OUTPUT_BUFFER_SIZE = 768;				// In frames (samples)
	public static int		SPEAKER_OUTPUT_BUFFER_FULL_SLEEP_TIME = 5;		// In milliseconds
	public static int		SPEAKER_NO_DATA_SLEEP_TIME = 20;				// In milliseconds
	public static int		SPEAKER_ADDED_THREAD_PRIORITY = 0;

	public static int		CONSOLE_FAST_SPEED_FACTOR = 8;

	public static boolean 	BUS_DATA_RETENTION = true;

	public static String	SERVER_SERVICE_NAME = "AtariP1Server";
	public static int 		SERVER_SERVICE_PORT = 9998;
	public static int 		SERVER_MAX_UPDATES_PENDING = 20;
	public static int 		CLIENT_MAX_UPDATES_PENDING = 20;

	private static Properties props = new Properties();

	private static Preferences userPreferences;
	private static boolean userPreferencesAsked = false;

}
