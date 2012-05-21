// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.console;

import general.av.audio.AudioSignal;
import general.av.video.VideoSignal;
import general.av.video.VideoStandard;
import general.board.Clock;
import general.m6502.M6502;

import java.util.Map;

import parameters.Parameters;
import atari.board.BUS;
import atari.cartridge.Cartridge;
import atari.cartridge.CartridgeDisconnected;
import atari.cartridge.CartridgeSocket;
import atari.console.savestate.ConsoleState;
import atari.console.savestate.SaveStateMedia;
import atari.console.savestate.SaveStateSocket;
import atari.controls.ConsoleControls;
import atari.controls.ConsoleControls.Control;
import atari.controls.ConsoleControlsInput;
import atari.controls.ConsoleControlsSocket;
import atari.pia.PIA;
import atari.pia.RAM;
import atari.tia.TIA;

public class Console {

	public Console() {
		mainComponentsCreate();
		socketsCreate();
		mainClockCreate();
		videoStandardAuto();
		cartridge(new CartridgeDisconnected());
	}

	public VideoSignal videoOutput() {
		return tia.videoOutput();
	}

	public AudioSignal audioOutput() {
		return tia.audioOutput();
	}

	public ConsoleControlsSocket controlsSocket() {
		return controlsSocket;
	}
	
	public CartridgeSocket cartridgeSocket() { 
		return cartridgeSocket;
	}
	
	public SaveStateSocket saveStateSocket() { 
		return saveStateSocket;
	}
	
	public void powerOn() {
		if (powerOn) powerOff();
		ram.powerOn();
		cpu.powerOn();
		pia.powerOn();
		tia.powerOn();
		powerOn = true;
		controlsSocket.controlsStatesRedefined();
		mainClockGo();
		videoStandardAutoDetectionStart();
	}

	public void powerOff() {
		mainClockPause();
		tia.powerOff();
		pia.powerOff();
		cpu.powerOff();
		ram.powerOff();
		powerOn = false;
		controlsSocket.controlsStatesRedefined();
	}

	public void showOSD(String message) {
		tia.videoOutput().showOSD(message);
	}
	
	public VideoStandard videoStandard() {
		return videoStandard;
	}
		
	public void videoStandard(VideoStandard videoStandard) {
		if (videoStandard != this.videoStandard) {
			this.videoStandard = videoStandard;
			tia.videoStandard(this.videoStandard);
			mainClockAdjustToNormal();
		}
		showOSD((videoStandardAuto ? "AUTO: " : "") + videoStandard);
	}

	// For debug purposes
	public Clock mainClock() {
		return mainClock;
	}

	protected Cartridge cartridge() {
		return bus.cartridge;
	}

	protected void cartridge(Cartridge cartridge) {
		bus.cartridge(cartridge);
	}

	protected void videoStandardAuto() {
		videoStandardAuto = true;
		if (powerOn) videoStandardAutoDetectionStart();
		else videoStandard(VideoStandard.NTSC);
	}

	protected void videoStandardAutoDetectionStart() {
		if (!videoStandardAuto || videoStandardAutoDetectionInProgress) return;
		// If the Cartridge has suggested a VideoStandard, use it
		VideoStandard suggestedStandard = bus.cartridge != null
				? bus.cartridge.suggestedVideoStandard() : VideoStandard.NTSC;
		if (suggestedStandard != null) {
			videoStandard(suggestedStandard);
			return;
		}
		// Otherwise use the VideoStandard detected by the monitor
		videoStandardAutoDetectionInProgress = true;
		tia.videoOutput().monitor().videoStandardDetectionStart();
		new Thread() { public void run() {
			VideoStandard std;
			int tries = 0;
			do {
				try { Thread.sleep(20); } catch (InterruptedException e) {};
				std = tia.videoOutput().monitor().videoStandardDetected();
			} while (std == null && ++tries < 1000/20);
			if (std != null) videoStandard(std);
			else showOSD("AUTO: FAILED");
			videoStandardAutoDetectionInProgress = false;
		}}.start();
	}

	protected void videoStandardForced(VideoStandard forcedVideoStandard) {
		videoStandardAuto = false;
		videoStandard(forcedVideoStandard);
	}

	protected void mainComponentsCreate() {
		cpu = new M6502();
		pia = new PIA(this);
		ram = new RAM();
		tia = new TIA(cpu, pia);
		bus = new BUS(tia, pia, ram);
		cpu.connectBus(bus);
	}

	protected void mainClockCreate() {
		mainClock = new Clock(tia, 0);
	}

	protected void mainClockAdjustToNormal() {
		mainClock.speed(tia.desiredClockForVideoStandard());
	}

	protected void mainClockAdjustToFast() {
		mainClock.speed(tia.desiredClockForVideoStandard() * FAST_SPEED_FACTOR);
	}

	protected void mainClockGo() {
		mainClock.go();
	}
	
	protected void mainClockPause() {
		mainClock.pause();
	}
	
	protected void socketsCreate() {
		controlsSocket = new ConsoleControlsSocket();
		controlsSocket.addForwardedInput(new ConsoleControlsInputAdapter());
		controlsSocket.addForwardedInput(tia);
		controlsSocket.addForwardedInput(pia);
		cartridgeSocket = new CartridgeSocketAdapter();
		saveStateSocket = new SaveStateSocketAdapter();
	}

	protected void loadState(ConsoleState state) {
		tia.loadState(state.tiaState);
		pia.loadState(state.piaState);
		ram.loadState(state.ramState);
		cpu.loadState(state.cpuState);
		cartridge(state.cartridge);
		videoStandard(state.videoStandard);
		controlsSocket.controlsStatesRedefined();
	}

	protected ConsoleState saveState() {
		return new ConsoleState(
			tia.saveState(),
			pia.saveState(),
			ram.saveState(),
			cpu.saveState(),
			cartridge(),
			videoStandard()
		);
	}

	protected void powerFry() {
		ram.powerFry();
	}


	public boolean powerOn = false;

	protected BUS bus;
	protected M6502 cpu;
	protected TIA tia;
	protected PIA pia;
	protected RAM ram;
	protected VideoStandard videoStandard;

	protected boolean videoStandardAuto = true;
	private boolean videoStandardAutoDetectionInProgress = false;
	
	protected ConsoleControlsSocket controlsSocket;
	protected CartridgeSocketAdapter cartridgeSocket;
	protected SaveStateSocketAdapter saveStateSocket;

	protected Clock mainClock;
	
	public static final int FAST_SPEED_FACTOR = Parameters.CONSOLE_FAST_SPEED_FACTOR;

	
	protected class ConsoleControlsInputAdapter implements ConsoleControlsInput {
		public ConsoleControlsInputAdapter() {
		}
		@Override
		public void controlStateChanged(Control control, boolean state) {
			// Normal state controls
			if (control == Control.FAST_SPEED) {
				if (state)
					mainClockAdjustToFast();
				else
					mainClockAdjustToNormal();
				return;
			} 
			// Toggles
			if (!state) return;
			switch (control) {
				case POWER:
					if (powerOn) powerOff();
					else powerOn();
					break;
				case SAVE_STATE_0: case SAVE_STATE_1: case SAVE_STATE_2: case SAVE_STATE_3: case SAVE_STATE_4: case SAVE_STATE_5: 
				case SAVE_STATE_6: case SAVE_STATE_7: case SAVE_STATE_8: case SAVE_STATE_9: case SAVE_STATE_10: case SAVE_STATE_11: case SAVE_STATE_12:
					saveStateSocket.saveState(control.slot);
					break;
				case LOAD_STATE_0: case LOAD_STATE_1: case LOAD_STATE_2: case LOAD_STATE_3: case LOAD_STATE_4: case LOAD_STATE_5: 
				case LOAD_STATE_6: case LOAD_STATE_7: case LOAD_STATE_8: case LOAD_STATE_9: case LOAD_STATE_10: case LOAD_STATE_11: case LOAD_STATE_12:
					saveStateSocket.loadState(control.slot);
					break;
				case VIDEO_STANDARD:
					if (videoStandardAuto) videoStandardForced(VideoStandard.NTSC);
					else if (videoStandard() == VideoStandard.NTSC) videoStandardForced(VideoStandard.PAL); 
						else videoStandardAuto();
					break;
				case POWER_FRY:
					powerFry();
			}
		}
		@Override
		public void controlStateChanged(ConsoleControls.Control control, int position) {
			// No positional controls here
		}
		@Override
		public void controlsStateReport(Map<ConsoleControls.Control, Boolean> report) {
			//  Only Power Control is visible from outside
			report.put(Control.POWER, powerOn);
		}
	}	
	
	protected class CartridgeSocketAdapter implements CartridgeSocket {
		@Override
		public void insert(Cartridge cartridge, boolean autoPower) {
			if (autoPower && powerOn) powerOff();
			cartridge(cartridge); 
			if (autoPower && !powerOn) controlsSocket.controlStateChanged(Control.POWER, true);
		}
	}	
	
	protected class SaveStateSocketAdapter implements SaveStateSocket {
		@Override
		public void connectMedia(SaveStateMedia media) {
			this.media = media;	
		}
		public void saveState(int slot) {
			if (!powerOn || media == null) return;
			mainClockPause();
			ConsoleState state = Console.this.saveState();
			mainClockGo();
			if (media.save(slot, state))
				showOSD("State " + slot + " saved");
			else 
				showOSD("State " + slot + " save failed");
		}
		public void loadState(int slot) {
			if (!powerOn || media == null) return;
			ConsoleState state = media.load(slot);
			if (state == null) {
				showOSD("State " + slot + " load failed");
				return;
			}
			mainClockPause();
			Console.this.loadState(state);
			mainClockGo();
			showOSD("State " + slot + " loaded");
		}
		private SaveStateMedia media;
	}	
	
}
