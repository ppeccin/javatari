// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.network;

import general.board.ClockDriven;

import java.util.ArrayList;
import java.util.List;

import atari.cartridge.Cartridge;
import atari.console.Console;
import atari.console.savestate.ConsoleState;
import atari.console.savestate.SaveStateMedia;
import atari.controls.ConsoleControls.Control;
import atari.controls.ConsoleControlsSocket;

public class ClientConsole extends Console implements ClockDriven {

	public ClientConsole(RemoteReceiver receiver) {
		super();
		setupReceiver(receiver);
	}

	public ClientConsole(RemoteReceiver receiver, Cartridge cartridge) {
		super(cartridge);
		setupReceiver(receiver);
	}

	@Override
	protected void mainClockCreate() {
		// Ignore, the clock is controlled remotely 
	}

	@Override
	protected void mainClockAdjustToNormal() {
		// Ignore, the clock is controlled remotely 
	}

	@Override
	protected void mainClockAdjustToFast() {
		// Ignore, the clock is controlled remotely 
	}

	@Override
	protected void mainClockGo() {
		// Ignore, the clock is controlled remotely 
	}
	
	@Override
	protected void mainClockPause() {
		// Ignore, the clock is controlled remotely 
	}
	
	@Override
	protected void socketsCreate() {
		controlsSocket = new ClientConsoleControlsSocketAdapter();
		controlsSocket.addForwardedInput(new ConsoleControlsInputAdapter());
		controlsSocket.addForwardedInput(tia);
		controlsSocket.addForwardedInput(pia);
		cartridgeSocket = new ClientConsoleCartridgeSocketAdapter();
		saveStateSocket = new ClientConsoleSaveStateSourceAdapter();
	}

	@Override
	public void clockPulse() {
		tia.clockPulse();
	}

	public void connected() {
		showOSD("Connected to Player 1 Server");
	}

	public void disconnected(){
		showOSD("Connection to Player 1 Server lost");
	}

	public void receiveServerUpdate(ServerUpdate update) {
		if (update.powerOn != null)
			receiveServerPower(update.powerOn);
		if (update.consoleState != null)
			receiveServerState(update.consoleState );
		if (update.controlChanges != null)
			((ClientConsoleControlsSocketAdapter) controlsSocket).serverControlChanges(update.controlChanges);
		if (update.isClockPulse)
			clockPulse();
	}

	public List<ControlChange> controlChangesToSend() {
		return ((ClientConsoleControlsSocketAdapter) controlsSocket).getChangesToSend();
	}
	
	private void receiveServerPower(boolean powerOn) {
		if (powerOn) powerOn();
		else powerOff();
	}

	private void receiveServerState(ConsoleState state) {
		loadState(state);
	}
	
	private void setupReceiver(RemoteReceiver receiver) {
		remoteReceiver = receiver;
		remoteReceiver.clientConsole(this);
	}


	private RemoteReceiver remoteReceiver;

	
	private class ClientConsoleControlsSocketAdapter extends ConsoleControlsSocket {
		@Override
		public void controlStateChanged(Control control, boolean state) {
			synchronized (queuedChanges) {
				queuedChanges.add(new ControlChange(control, state));
			}
		}
		public List<ControlChange> getChangesToSend() {
			List<ControlChange> changesToSend;
			synchronized (queuedChanges) {
				if (queuedChanges.isEmpty())
					changesToSend = emptyChanges;
				else {
					changesToSend = new ArrayList<ControlChange>(queuedChanges);
					queuedChanges.clear();
				}
			}
			return changesToSend;
		}
		public void serverControlChanges(List<ControlChange> changes) {
			// Effectively accepts the control changes, received from the server 
			for (ControlChange change : changes)
				super.controlStateChanged(change.control, change.state);
		}
		private List<ControlChange> queuedChanges = new ArrayList<ControlChange>();
		private List<ControlChange> emptyChanges = new ArrayList<ControlChange>();
	}
	
	private class ClientConsoleCartridgeSocketAdapter extends CartridgeSocketAdapter {
		@Override
		public void insert(Cartridge cartridge) {
			// Cartridge insertion is controlled by the server
			showOSD("Only the Server can switch Cartridges");
		}
	}

	private class ClientConsoleSaveStateSourceAdapter extends SaveStateSocketAdapter {
		@Override
		public void connectMedia(SaveStateMedia media) {
			// Ignore, savestates are controlled by the server
		}
	}

}
