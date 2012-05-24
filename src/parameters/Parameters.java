// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package parameters;

import java.io.InputStream;
import java.security.AccessControlException;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.Terminator;

public class Parameters {

	// Load Properties file and also process command line options and parameters
	public static void init(String[] args) {
		loadPropertiesFile();
		processOptions(args);
		processMainArg(args);
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

	private static void processOptions(String[] args) {
		for (String arg : args) {
			if (!arg.startsWith("-")) continue;
			processOption(arg);
		}
	}

	private static void processOption(String opt) {
		// Disable Cartridge Change
		if (opt.toUpperCase().equals("-DISABLECARTRIDGECHANGE")) {
			SCREEN_CARTRIDGE_CHANGE_DISABLED = true;
			return;
		}
		// CRT Mode
		if (opt.length() == 9 && opt.toUpperCase().startsWith("-CRTMODE")) {
			Pattern p = Pattern.compile("[0-4]");
			Matcher m = p.matcher(opt);
			if (!m.find()) {
				System.out.println("Invalid option: " + opt + ". Only -crtmode0 to -crtmode4 are valid");
				Terminator.terminate();
			}
			SCREEN_CRT_MODE = Integer.parseInt(m.group());
			return;
		}
		// Full Screen
		if (opt.toUpperCase().equals("-FULLSCREEN")) {
			SCREEN_FULLSCREEN = true;
			return;
		}
		System.out.println("Unknown option: " + opt);
		Terminator.terminate();
	}

	private static void processMainArg(String[] args) {
		for (String arg : args)
			if (!arg.startsWith("-")) {
				mainArg = arg;
				return;
			}
	}

	private static void loadPropertiesFile() {
		InputStream is = Thread.currentThread().getContextClassLoader().
				getResourceAsStream("parameters/javatari.properties");
		Properties p = new Properties();
		try {
			try {
				p.clear();
				p.load(is);
			} finally {
				is.close();
			}
		} catch (Exception ex) {
			System.out.println("parameters/javatari.properties not found, using defaults");
			return;
		}
		
		TIA_FORCED_CLOCK = Double.valueOf(p.getProperty("TIA_FORCED_CLOCK", String.valueOf(TIA_FORCED_CLOCK)));
		TIA_DEFAULT_CLOCK_NTSC = Double.valueOf(p.getProperty("TIA_DEFAULT_CLOCK_NTSC", String.valueOf(TIA_DEFAULT_CLOCK_NTSC)));
		TIA_DEFAULT_CLOCK_PAL = Double.valueOf(p.getProperty("TIA_DEFAULT_CLOCK_PAL", String.valueOf(TIA_DEFAULT_CLOCK_PAL)));
		TIA_SYNC_WITH_AUDIO_MONITOR = Boolean.valueOf(p.getProperty("TIA_SYNC_WITH_AUDIO_MONITOR", String.valueOf(TIA_SYNC_WITH_AUDIO_MONITOR))); 
		TIA_SYNC_WITH_VIDEO_MONITOR = Boolean.valueOf(p.getProperty("TIA_SYNC_WITH_VIDEO_MONITOR", String.valueOf(TIA_SYNC_WITH_VIDEO_MONITOR))); 
		
		TIA_AUDIO_SAMPLE_RATE = Integer.valueOf(p.getProperty("TIA_AUDIO_SAMPLE_RATE", String.valueOf(TIA_AUDIO_SAMPLE_RATE)));			
		TIA_AUDIO_MAX_AMPLITUDE = Float.valueOf(p.getProperty("TIA_AUDIO_MAX_AMPLITUDE", String.valueOf(TIA_AUDIO_MAX_AMPLITUDE)));  
		TIA_AUDIO_MAX_MONO_CHANNEL_AMPLITUDE = Float.valueOf(p.getProperty("TIA_AUDIO_MAX_MONO_CHANNEL_AMPLITUDE", String.valueOf(TIA_AUDIO_MAX_MONO_CHANNEL_AMPLITUDE)));
		TIA_AUDIO_MAX_STEREO_CHANNEL_AMPLITUDE= Float.valueOf(p.getProperty("TIA_AUDIO_MAX_STEREO_CHANNEL_AMPLITUDE", String.valueOf(TIA_AUDIO_MAX_STEREO_CHANNEL_AMPLITUDE)));

		RAM_FRY_ZERO_BITS = Integer.valueOf(p.getProperty("RAM_FRY_ZERO_BITS", String.valueOf(RAM_FRY_ZERO_BITS)));
		RAM_FRY_ONE_BITS = Integer.valueOf(p.getProperty("RAM_FRY_ONE_BITS", String.valueOf(RAM_FRY_ONE_BITS)));
		RAM_FRY_VARIANCE = Float.valueOf(p.getProperty("RAM_FRY_VARIANCE", String.valueOf(RAM_FRY_VARIANCE)));

		SCREEN_DEFAULT_FPS = Double.valueOf(p.getProperty("SCREEN_DEFAULT_FPS", String.valueOf(SCREEN_DEFAULT_FPS)));
		SCREEN_DEFAULT_ORIGIN_X = Integer.valueOf(p.getProperty("SCREEN_DEFAULT_ORIGIN_X", String.valueOf(SCREEN_DEFAULT_ORIGIN_X)));
		SCREEN_DEFAULT_ORIGIN_Y_PCT = Double.valueOf(p.getProperty("SCREEN_DEFAULT_ORIGIN_Y_PCT", String.valueOf(SCREEN_DEFAULT_ORIGIN_Y_PCT)));
		SCREEN_DEFAULT_WIDTH = Integer.valueOf(p.getProperty("SCREEN_DEFAULT_WIDTH", String.valueOf(SCREEN_DEFAULT_WIDTH)));
		SCREEN_DEFAULT_HEIGHT_PCT = Double.valueOf(p.getProperty("SCREEN_DEFAULT_HEIGHT_PCT", String.valueOf(SCREEN_DEFAULT_HEIGHT_PCT)));
		SCREEN_DEFAULT_SCALE_X = Float.valueOf(p.getProperty("SCREEN_DEFAULT_SCALE_X", String.valueOf(SCREEN_DEFAULT_SCALE_X)));
		SCREEN_DEFAULT_SCALE_Y = Float.valueOf(p.getProperty("SCREEN_DEFAULT_SCALE_Y", String.valueOf(SCREEN_DEFAULT_SCALE_Y)));
		SCREEN_DEFAULT_SCALE_ASPECT_X = Float.valueOf(p.getProperty("SCREEN_DEFAULT_SCALE_ASPECT_X", String.valueOf(SCREEN_DEFAULT_SCALE_ASPECT_X)));
		SCREEN_FULLSCREEN = Boolean.valueOf(p.getProperty("SCREEN_FULLSCREEN", String.valueOf(SCREEN_FULLSCREEN)));
		SCREEN_BORDER_SIZE = Integer.valueOf(p.getProperty("SCREEN_BORDER_SIZE", String.valueOf(SCREEN_BORDER_SIZE)));
		SCREEN_OSD_FRAMES = Integer.valueOf(p.getProperty("SCREEN_OSD_FRAMES", String.valueOf(SCREEN_OSD_FRAMES)));
		SCREEN_VSYNC_TOLERANCE = Integer.valueOf(p.getProperty("SCREEN_VSYNC_TOLERANCE", String.valueOf(SCREEN_VSYNC_TOLERANCE)));
		SCREEN_QUALITY_RENDERING = Boolean.valueOf(p.getProperty("SCREEN_QUALITY_RENDERING", String.valueOf(SCREEN_QUALITY_RENDERING)));
		SCREEN_CRT_MODE = Integer.valueOf(p.getProperty("SCREEN_CRT_MODE", String.valueOf(SCREEN_CRT_MODE)));
		SCREEN_CRT_RETENTION_ALPHA = Float.valueOf(p.getProperty("SCREEN_CRT_RETENTION_ALPHA", String.valueOf(SCREEN_CRT_RETENTION_ALPHA)));
		SCREEN_SCANLINES_STRENGTH = Float.valueOf(p.getProperty("SCREEN_SCANLINES_STRENGTH", String.valueOf(SCREEN_SCANLINES_STRENGTH)));
		SCREEN_MULTI_BUFFERING = Integer.valueOf(p.getProperty("SCREEN_MULTI_BUFFERING", String.valueOf(SCREEN_MULTI_BUFFERING)));
		SCREEN_PAGE_FLIPPING = Boolean.valueOf(p.getProperty("SCREEN_PAGE_FLIPPING", String.valueOf(SCREEN_PAGE_FLIPPING)));
		SCREEN_BUFFER_VSYNC = Integer.valueOf(p.getProperty("SCREEN_BUFFER_VSYNC", String.valueOf(SCREEN_BUFFER_VSYNC)));
		SCREEN_FRAME_ACCELERATION = Float.valueOf(p.getProperty("SCREEN_FRAME_ACCELERATION", String.valueOf(SCREEN_FRAME_ACCELERATION)));
		SCREEN_INTERM_FRAME_ACCELERATION = Float.valueOf(p.getProperty("SCREEN_INTERM_FRAME_ACCELERATION", String.valueOf(SCREEN_INTERM_FRAME_ACCELERATION)));
		SCREEN_SCANLINES_ACCELERATION = Float.valueOf(p.getProperty("SCREEN_SCANLINES_ACCELERATION", String.valueOf(SCREEN_SCANLINES_ACCELERATION)));
		SCREEN_CARTRIDGE_CHANGE_DISABLED = Boolean.valueOf(p.getProperty("SCREEN_CARTRIDGE_CHANGE_DISABLED", String.valueOf(SCREEN_CARTRIDGE_CHANGE_DISABLED)));
		
		SPEAKER_DEFAULT_FPS = Double.valueOf(p.getProperty("SPEAKER_DEFAULT_FPS", String.valueOf(SPEAKER_DEFAULT_FPS)));
		SPEAKER_INPUT_BUFFER_SIZE = Integer.valueOf(p.getProperty("SPEAKER_INPUT_BUFFER_SIZE", String.valueOf(SPEAKER_INPUT_BUFFER_SIZE)));
		SPEAKER_OUTPUT_BUFFER_SIZE = Integer.valueOf(p.getProperty("SPEAKER_OUTPUT_BUFFER_SIZE", String.valueOf(SPEAKER_OUTPUT_BUFFER_SIZE)));
		SPEAKER_OUTPUT_BUFFER_FULL_SLEEP_TIME = Integer.valueOf(p.getProperty("SPEAKER_OUTPUT_BUFFER_FULL_SLEEP_TIME", String.valueOf(SPEAKER_OUTPUT_BUFFER_FULL_SLEEP_TIME)));
		SPEAKER_NO_DATA_SLEEP_TIME = Integer.valueOf(p.getProperty("SPEAKER_NO_DATA_SLEEP_TIME", String.valueOf(SPEAKER_NO_DATA_SLEEP_TIME)));
		SPEAKER_ADDED_THREAD_PRIORITY = Integer.valueOf(p.getProperty("SPEAKER_ADDED_THREAD_PRIORITY", String.valueOf(SPEAKER_ADDED_THREAD_PRIORITY)));

		CONSOLE_FAST_SPEED_FACTOR = Integer.valueOf(p.getProperty("CONSOLE_FAST_SPEED_FACTOR", String.valueOf(CONSOLE_FAST_SPEED_FACTOR)));

		BUS_DATA_RETENTION = Boolean.valueOf(p.getProperty("BUS_DATA_RETENTION", String.valueOf(BUS_DATA_RETENTION)));

		SERVER_SERVICE_NAME = String.valueOf(p.getProperty("SERVER_SERVICE_NAME", String.valueOf(SERVER_SERVICE_NAME)));
		SERVER_SERVICE_PORT = Integer.valueOf(p.getProperty("SERVER_SERVICE_PORT", String.valueOf(SERVER_SERVICE_PORT)));
		SERVER_MAX_UPDATES_PENDING = Integer.valueOf(p.getProperty("SERVER_MAX_UPDATES_PENDING", String.valueOf(SERVER_MAX_UPDATES_PENDING)));
		CLIENT_MAX_UPDATES_PENDING = Integer.valueOf(p.getProperty("CLIENT_MAX_UPDATES_PENDING", String.valueOf(CLIENT_MAX_UPDATES_PENDING)));

	}


	// Cartridge URL to load passed as argument
	public static String mainArg = null;

	
	// DEFAULTS

	public static double	TIA_FORCED_CLOCK = 0;							//  0 = No Forced Clock
	public static double	TIA_DEFAULT_CLOCK_NTSC = 60;
	public static double	TIA_DEFAULT_CLOCK_PAL = 50.39;	
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
	public static boolean 	SCREEN_FULLSCREEN = false;
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
	public static boolean 	SCREEN_CARTRIDGE_CHANGE_DISABLED = false;
	
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

	private static Preferences userPreferences;
	private static boolean userPreferencesAsked = false;

}
