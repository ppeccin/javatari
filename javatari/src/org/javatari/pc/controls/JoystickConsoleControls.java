// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.controls;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.javatari.atari.controls.ConsoleControls;
import org.javatari.atari.controls.ConsoleControlsSocket;
import org.javatari.general.av.video.VideoMonitor;
import org.javatari.parameters.Parameters;
import org.javatari.pc.screen.Screen;
import org.joy.Joy;
import org.joy.Joy.Info;
import org.joy.Joystick;


public final class JoystickConsoleControls implements ConsoleControls {
	
	public JoystickConsoleControls(AWTConsoleControls awtControls) {
		super();
		this.awtControls = awtControls;
	}

	public void connect(ConsoleControlsSocket socket) {
		consoleControlsSocket = socket;
	}

	public void connectScreen(Screen screen) {
		videoMonitor = screen.monitor();
	}

	public void powerOn() {
		swappedMode = false;
		start();
	}
	
	public void powerOff() {
		stop();
	}

	public void paddleMode(boolean state) {
		paddleMode = state;
		joy0State.xPosition = joy1State.xPosition = -1;
	}

	public synchronized void toggleMode() {
		if (!started) {
			swappedMode = false;
			start();
		} else
			swappedMode = !swappedMode;
		if (started) videoMonitor.showOSD("Joystick input " + (swappedMode ? "Swapped" : "Normal"), true);
		else if (devices.isEmpty()) videoMonitor.showOSD("No Joysticks detected!", true);
			else videoMonitor.showOSD("Joysticks are disabled! Open Settings", true);
	}
	
	public boolean isStarted() {
		return started;
	}

	public Joystick getJoystick0() {
		return joystick0;
	}

	public Joystick getJoystick1() {
		return joystick1;
	}

	public Vector<DeviceOption> getJoystick0DeviceOptions() {
		return joystick0DeviceOptions;
	}

	public Vector<DeviceOption> getJoystick1DeviceOptions() {
		return joystick1DeviceOptions;
	}

	public DeviceOption getJoystick0DeviceOption() {
		return joystick0DeviceOption;
	}

	public DeviceOption getJoystick1DeviceOption() {
		return joystick1DeviceOption;
	}

	public synchronized void start() {
		stop();

		if (deviceListFailed) return;
		
		try {
			devices = Joy.listDevices();
		} catch (UnsupportedOperationException e) {
			System.out.println(e.getMessage());
			deviceListFailed = true;
			devices = new ArrayList<Info>();
			joystick0DeviceOptions = joystick1DeviceOptions = new Vector<DeviceOption>();
			joystick0DeviceOption = joystick1DeviceOption = null;
			return;
		}

		initPreferences();
		setupDeviceOptions(joy0Prefs.device, joy1Prefs.device);
	
		if (joystick0 == null && joystick1 == null) return;

		applyPreferences();
		updaterActive();
	}

	private void setupDeviceOptions(int joy0Device, int joy1Device) {
		// Determine auto options
		Info auto0 = devices.size() > 0 ? devices.get(0) : null;
		Info auto1 = devices.size() > 1 ? devices.get(1) : null;
		// List all options for joystick0 and joystick1
		joystick0DeviceOptions = new Vector<DeviceOption>();
		joystick0DeviceOptions.add(new DeviceOption(Parameters.JOY_DEVICE_AUTO, auto0));
		joystick0DeviceOptions.add(new DeviceOption(Parameters.JOY_DEVICE_NONE, null));
		joystick1DeviceOptions = new Vector<DeviceOption>();
		joystick1DeviceOptions.add(new DeviceOption(Parameters.JOY_DEVICE_AUTO, auto1));
		joystick1DeviceOptions.add(new DeviceOption(Parameters.JOY_DEVICE_NONE, null));
		for (Info info : devices) {
			joystick0DeviceOptions.add(new DeviceOption(info.id, info));
			joystick1DeviceOptions.add(new DeviceOption(info.id, info));
		}
		// Set chosen options
		joystick0DeviceOption = null;
		for (DeviceOption opt : joystick0DeviceOptions)
			if (joy0Device == opt.device) joystick0DeviceOption = opt;
		if (joystick0DeviceOption == null) joystick0DeviceOption = joystick0DeviceOptions.get(0);	// Use Auto if no option possible
		joystick1DeviceOption = null;
		for (DeviceOption opt : joystick1DeviceOptions)
			if (joy1Device == opt.device) joystick1DeviceOption = opt;
		if (joystick1DeviceOption == null) joystick1DeviceOption = joystick1DeviceOptions.get(0);	// Use Auto if no option possible
		// Set actual devices according to chosen options
		if (joystick0DeviceOption.info != null) joystick0 = Joystick.create(joystick0DeviceOption.info);
		if (joystick1DeviceOption.info != null) joystick1 = Joystick.create(joystick1DeviceOption.info);
	}

	private synchronized void stop() {
		updaterInactive();
		initControls();
	}

	public void initPreferences() {
		joy0Prefs = new Preferences();
		joy1Prefs = new Preferences();
		joy0Prefs.device           = Parameters.JOY_P0_DEVICE;
		joy0Prefs.xAxis            = Parameters.JOY_P0_XAXIS;
		joy0Prefs.xAxisSignal      = Parameters.JOY_P0_XAXIS_SIGNAL;
		joy0Prefs.yAxis            = Parameters.JOY_P0_YAXIS;
		joy0Prefs.yAxisSignal      = Parameters.JOY_P0_YAXIS_SIGNAL;
		joy0Prefs.paddleAxis       = Parameters.JOY_P0_PAD_AXIS;
		joy0Prefs.paddleAxisSignal = Parameters.JOY_P0_PAD_AXIS_SIGNAL;
		joy0Prefs.button           = Parameters.JOY_P0_BUTTON;
		joy0Prefs.button2          = Parameters.JOY_P0_BUTTON2;
		joy0Prefs.select           = Parameters.JOY_P0_SELECT;
		joy0Prefs.reset            = Parameters.JOY_P0_RESET;
		joy0Prefs.pause            = Parameters.JOY_P0_PAUSE;
		joy0Prefs.fastSpeed        = Parameters.JOY_P0_FAST_SPPED;
		joy0Prefs.paddleCenter     = (int) ((float)Parameters.JOY_P0_PADDLE_CENTER / 100 * -190) + 190 - 5;
		joy0Prefs.paddleSens       = (int) ((float)Parameters.JOY_P0_PADDLE_SENS / 100 * -190); 
		joy0Prefs.deadzone         = Parameters.JOY_P0_DEADZONE;
		joy1Prefs.device           = Parameters.JOY_P1_DEVICE;
		joy1Prefs.xAxis            = Parameters.JOY_P1_XAXIS;
		joy1Prefs.xAxisSignal      = Parameters.JOY_P1_XAXIS_SIGNAL;
		joy1Prefs.yAxis            = Parameters.JOY_P1_YAXIS;
		joy1Prefs.yAxisSignal      = Parameters.JOY_P1_YAXIS_SIGNAL;
		joy1Prefs.paddleAxis       = Parameters.JOY_P1_PAD_AXIS;
		joy1Prefs.paddleAxisSignal = Parameters.JOY_P1_PAD_AXIS_SIGNAL;
		joy1Prefs.button           = Parameters.JOY_P1_BUTTON;
		joy1Prefs.button2          = Parameters.JOY_P1_BUTTON2;
		joy1Prefs.select           = Parameters.JOY_P1_SELECT;
		joy1Prefs.reset            = Parameters.JOY_P1_RESET;
		joy1Prefs.pause            = Parameters.JOY_P1_PAUSE;
		joy1Prefs.fastSpeed        = Parameters.JOY_P1_FAST_SPPED;
		joy1Prefs.deadzone         = Parameters.JOY_P1_DEADZONE;
		joy1Prefs.paddleCenter     = (int) ((float)Parameters.JOY_P1_PADDLE_CENTER / 100 * -190) + 190 - 5;
		joy1Prefs.paddleSens       = (int) ((float)Parameters.JOY_P1_PADDLE_SENS / 100 * -190);
		applyPreferences();
	}

	private void initControls() {
		joystick0 = joystick1 = null;
		joy0State = new State();
		joy1State = new State();
	}

	public void applyPreferences() {
		if (joystick0 != null) joystick0.setDigitalThreshold((float)joy0Prefs.deadzone / 100);
		if (joystick1 != null) joystick1.setDigitalThreshold((float)joy1Prefs.deadzone / 100);
	}
	
	public synchronized boolean startButtonDetection(int joy, JoystickButtonDetectionListener listener) {
		stopButtonDetection();
		if (joy < 0 || joy > 1 || !started) return false;
		Joystick joystick = joy == 0 ? joystick0 : joystick1;
		if (joystick == null) return false;
		joyButtonDetection = joystick;
		joyButtonDetectionListener = listener;
		return true;
	}

	public synchronized void stopButtonDetection() {
		joyButtonDetection = null;
		joyButtonDetectionLastState = -1;
		joyButtonDetectionListener = null;
	}
	
	private synchronized void update() {
		boolean ok;
		if (joystick0 != null) {
			ok = joystick0.update();
			if (ok)	update(joystick0, joy0State, joy0Prefs, !swappedMode);
			else {
				joystick0 = null;
				videoMonitor.showOSD((p1ControlsMode ? "P2" : "P1") + " Joystick disconnected", true);
			}
		}
		if (joystick1 != null) {
			ok = joystick1.update();
			if (ok) update(joystick1, joy1State, joy1Prefs, swappedMode); 
			else {
				joystick1 = null;
				videoMonitor.showOSD((p1ControlsMode ? "P1" : "P2") + " Joystick disconnected", true);
			}
		}
		if (joystick0 == null && joystick1 == null) stop();
	}

	private void update(Joystick joystick, State joyState, Preferences joyPrefs, boolean player0) {
		// Paddle Analog
		if (paddleMode && joyPrefs.paddleSens != 0) {
			float pos = joystick.getAxisNoThreshold(joyPrefs.paddleAxis) * joyPrefs.paddleAxisSignal;
			int newPosition = (int) (pos * joyPrefs.paddleSens + joyPrefs.paddleCenter);
			if (newPosition < 0) newPosition = 0;
			else if (newPosition > 380) newPosition = 380;
			if (newPosition != joyState.xPosition) {
				joyState.xPosition = newPosition;
				consoleControlsSocket.controlStateChanged(player0 ? Control.PADDLE0_POSITION : Control.PADDLE1_POSITION, newPosition);
			}
		}
		// Joystick direction (Analog or POV) and Paddle Digital (Analog or POV)
		int newCardinal = Joystick.CENTER;
		if (!paddleMode || joyPrefs.paddleSens == 0) 
			newCardinal = joystick.getAxisDirectionCardinal(joyPrefs.xAxis, joyPrefs.xAxisSignal, joyPrefs.yAxis, joyPrefs.yAxisSignal);
		if (newCardinal == Joystick.CENTER && joystick.getInfo().pov) 
			newCardinal = joystick.getPOVDirectionCardinal();
		if (newCardinal != joyState.cardinal) {
			boolean newUP = newCardinal == Joystick.NORTHWEST || newCardinal == Joystick.NORTH || newCardinal == Joystick.NORTHEAST;
			boolean newRIGHT = newCardinal == Joystick.NORTHEAST || newCardinal == Joystick.EAST || newCardinal == Joystick.SOUTHEAST;
			boolean newDOWN = newCardinal == Joystick.SOUTHEAST || newCardinal == Joystick.SOUTH || newCardinal == Joystick.SOUTHWEST;
			boolean newLEFT = newCardinal == Joystick.SOUTHWEST || newCardinal == Joystick.WEST || newCardinal == Joystick.NORTHWEST;
			if (player0) {
				awtControls.processKeyEvent(Parameters.KEY_P0_UP, 0, newUP);
				awtControls.processKeyEvent(Parameters.KEY_P0_RIGHT, 0, newRIGHT);
				awtControls.processKeyEvent(Parameters.KEY_P0_DOWN, 0, newDOWN);
				awtControls.processKeyEvent(Parameters.KEY_P0_LEFT, 0, newLEFT);
			} else {
				awtControls.processKeyEvent(Parameters.KEY_P1_UP, 0, newUP);
				awtControls.processKeyEvent(Parameters.KEY_P1_RIGHT, 0, newRIGHT);
				awtControls.processKeyEvent(Parameters.KEY_P1_DOWN, 0, newDOWN);
				awtControls.processKeyEvent(Parameters.KEY_P1_LEFT, 0, newLEFT);
			}
			joyState.cardinal = newCardinal;
		}
		// Joystick button
		if (joyButtonDetection == joystick) 
			detectButton();
		else {
			boolean newButton = joystick.getButton(joyPrefs.button) || joystick.getButton(joyPrefs.button2);
			if (newButton != joyState.button) {
				awtControls.processKeyEvent(player0 ? Parameters.KEY_P0_BUTTON : Parameters.KEY_P1_BUTTON, 0, newButton);
				joyState.button = newButton;
			}
		}
		// Other Console controls
		boolean newSelect = joystick.getButton(joyPrefs.select);
		if (newSelect != joyState.select) {
			awtControls.processKeyEvent(AWTConsoleControls.KEY_SELECT, 0, newSelect);
			joyState.select = newSelect;
		}
		boolean newReset = joystick.getButton(joyPrefs.reset);
		if (newReset != joyState.reset) {
			awtControls.processKeyEvent(AWTConsoleControls.KEY_RESET, 0, newReset);
			joyState.reset = newReset;
		}
		boolean newPause = joystick.getButton(joyPrefs.pause);
		if (newPause != joyState.pause) {
			awtControls.processKeyEvent(AWTConsoleControls.KEY_PAUSE, KeyEvent.ALT_DOWN_MASK, newPause);
			joyState.pause = newPause;
		}
		boolean newFastSpeed = joystick.getButton(joyPrefs.fastSpeed);
		if (newFastSpeed != joyState.fastSpeed) {
			awtControls.processKeyEvent(AWTConsoleControls.KEY_FAST_SPEED, 0, newFastSpeed);
			joyState.fastSpeed = newFastSpeed;
		}
	}
	
	private void detectButton() {
		int buttonsNewState = joyButtonDetection.getButtons();
		if (joyButtonDetectionLastState == -1) {
			joyButtonDetectionLastState = buttonsNewState;
			return;
		}
		if (joyButtonDetectionLastState == buttonsNewState) return;
		for (int b = 0; b < joyButtonDetection.getInfo().buttons; b++) {
			boolean newB = (buttonsNewState & (1 << b)) != 0; 
			if (newB && (joyButtonDetectionLastState & (1 << b)) == 0) {		// Button has been pressed
				int j = joyButtonDetection == joystick0 ? 0 : 1;
				JoystickButtonDetectionListener list = joyButtonDetectionListener;
				stopButtonDetection();
				list.joystickButtonDetected(j, b);
				return;
			}
		}
	}

	private synchronized void updaterActive() {
		started = true;
		if (stateUpdater != null && stateUpdater.isAlive()) return;	// All set
		stateUpdater = new StateUpdater();
		stateUpdater.start();
	}

	private synchronized void updaterInactive() {
		started = false;
		if (stateUpdater == null) return;	// All set
		if (stateUpdater.isAlive())
			try { stateUpdater.join(1000); } catch (InterruptedException e) {}
		stateUpdater = null;
	}


	public boolean deviceListFailed = false;
	
	public boolean p1ControlsMode = false;
	public boolean swappedMode = false;
	public boolean paddleMode = false;
	
	private final AWTConsoleControls awtControls;
	private VideoMonitor videoMonitor;
	private ConsoleControlsSocket consoleControlsSocket; 

	private List<Info> devices = new Vector<Info>();
	private Vector<DeviceOption> joystick0DeviceOptions = new Vector<DeviceOption>();
	private Vector<DeviceOption> joystick1DeviceOptions = new Vector<DeviceOption>();
	private DeviceOption joystick0DeviceOption, joystick1DeviceOption; 

	private Joystick joystick0, joystick1;
	private State joy0State, joy1State;
	private Preferences joy0Prefs, joy1Prefs;

	private boolean started = false;
	private StateUpdater stateUpdater;

	private Joystick joyButtonDetection = null;
	private int joyButtonDetectionLastState = -1;
	private JoystickButtonDetectionListener joyButtonDetectionListener = null;

	private static int JOYSTICK_UPDATE_SLEEP = 1000 / Parameters.JOYSTICK_UPDATE_RATE;

		
	public interface JoystickButtonDetectionListener {
		public void joystickButtonDetected(int joystick, int button);
	}

	public static class DeviceOption {
		public int device;
		public Info info;
		public DeviceOption(int device, Info info) {
			this.device = device;
			this.info = info;
		}
		@Override
		public String toString() {
			return device == Parameters.JOY_DEVICE_NONE ? "None" :
				device == Parameters.JOY_DEVICE_AUTO ? "Auto" + ": " + (info != null ? info : "None" ):	
				info.toString();
		}
	}
	
	private static class State {
		int cardinal = Joystick.CENTER;
		boolean button, select, reset, fastSpeed, pause = false;
		int xPosition = -1;
	}
	
	private static class Preferences {
		public int device, xAxis, xAxisSignal, yAxis, yAxisSignal, paddleAxis, paddleAxisSignal, button, button2;
		public int select, reset, pause, fastSpeed, deadzone, paddleCenter, paddleSens;
	}

	private class StateUpdater extends Thread {
		public StateUpdater() {
			super("JoystickControls Updater");
		}
		@Override
		public void run() {
			while(started) {
				update();
				try { sleep(JOYSTICK_UPDATE_SLEEP); } catch (InterruptedException e) {}
			}
		}
	}

}
