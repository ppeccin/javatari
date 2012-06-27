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
	protected void mainClockDestroy() {
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
		showOSD("Disconnected from Player 1 Server");
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
	
	private void receiveServerPower(boolean serverPowerOn) {
		if (serverPowerOn && !powerOn) powerOn();
		else if (!serverPowerOn && powerOn) powerOff();
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
		@Override
		public void controlStateChanged(Control control, int position) {
			synchronized (queuedChanges) {
				queuedChanges.add(new ControlChangeForPaddle(control, position));
			}
		}
		public List<ControlChange> getChangesToSend() {
			List<ControlChange> changesToSend;
			synchronized (queuedChanges) {
				if (queuedChanges.isEmpty())
					return null;
				else {
					changesToSend = new ArrayList<ControlChange>(queuedChanges);
					queuedChanges.clear();
					return changesToSend;
				}
			}
		}
		public void serverControlChanges(List<ControlChange> changes) {
			// Effectively accepts the control changes, received from the server 
			for (ControlChange change : changes)
				if (change instanceof ControlChangeForPaddle)
					super.controlStateChanged(change.control, ((ControlChangeForPaddle)change).position);
				else
					super.controlStateChanged(change.control, change.state);
		}
		private List<ControlChange> queuedChanges = new ArrayList<ControlChange>();
	}
	
	// Cartridge insertion is controlled only by the Server
	private class ClientConsoleCartridgeSocketAdapter extends CartridgeSocketAdapter {
		@Override
		public void insert(Cartridge cartridge, boolean autoPower) {
			showOSD("Only the Server can change Cartridges");
		}
	}

	private class ClientConsoleSaveStateSourceAdapter extends SaveStateSocketAdapter {
		@Override
		public void connectMedia(SaveStateMedia media) {
			// Ignore, savestates are controlled by the server
		}
	}

}
