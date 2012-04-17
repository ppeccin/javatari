// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.network;

import general.board.Clock;
import general.board.ClockDriven;

import java.util.ArrayList;
import java.util.List;

import atari.cartridge.Cartridge;
import atari.console.Console;
import atari.console.savestate.ConsoleState;
import atari.controls.ConsoleControls.Control;
import atari.controls.ConsoleControlsSocket;
import atari.tia.TIA;

public class ServerConsole extends Console implements ClockDriven {

	public ServerConsole(RemoteTransmitter transmitter) {
		super();
		setupTransmitter(transmitter);
	}

	public ServerConsole(RemoteTransmitter transmitter, Cartridge cartridge) {
		super(cartridge);
		setupTransmitter(transmitter);
	}
	
	@Override
	public void powerOff() {
		// The server clock is always running
		super.powerOff();
		mainClockGo();
	}

	@Override
	protected void mainClockCreate() {
		// The server clock is always running
		mainClock = new Clock(this, TIA.DEFAUL_CLOCK_NTSC);
		mainClock.go();
	}

	@Override
	protected void socketsCreate() {
		controlsSocket = new ServerConsoleControlsSocketAdapter();
		controlsSocket.addForwardedInput(new ConsoleControlsInputAdapter());
		controlsSocket.addForwardedInput(tia);
		controlsSocket.addForwardedInput(pia);
		cartridgeSocket = new ServerConsoleCartridgeSocketAdapter();
		saveStateSocket = new ServerConsoleSaveStateSourceAdapter();
	}

	@Override
	protected synchronized void cartridge(Cartridge cartridge) {
		super.cartridge(cartridge);
		sendStateUpdate();
	}

	@Override
	protected synchronized void loadState(ConsoleState state) {
		super.loadState(state);
		sendStateUpdate();
	}

	@Override
	public synchronized void clockPulse() {
		List<ControlChange> controlChanges = ((ServerConsoleControlsSocketAdapter) controlsSocket).commitAndGetChangesToSend();
		if (powerOn) tia.clockPulse();
		if (remoteTransmitter.isClientConnected()) {
			ServerUpdate update = new ServerUpdate();
			if (!controlChanges.isEmpty()) update.controlChanges = controlChanges;
			update.isClockPulse = powerOn;
			remoteTransmitter.sendUpdate(update);
		}
	}

	public synchronized void clientConnected() {
		showOSD("Player 2 Client Connected");
		ServerUpdate update = new ServerUpdate();
		update.powerOn = powerOn;
		update.consoleState = saveState();
		remoteTransmitter.sendUpdate(update);
	}

	public void clientDisconnected() {
		showOSD("Player 2 Client Disconnected!");
	}

	public void receiveClientControlChanges(List<ControlChange> clientControlChages) {
		for (ControlChange change : clientControlChages)
			controlsSocket.controlStateChanged(change.control, change.state);
	}

	private void sendStateUpdate() {
		if (remoteTransmitter != null) {
			ServerUpdate update = new ServerUpdate();
			update.consoleState = saveState();
			remoteTransmitter.sendUpdate(update);
		}
	}

	private void setupTransmitter(RemoteTransmitter transmitter) {
		remoteTransmitter = transmitter;
		remoteTransmitter.serverConsole(this);
	}

	
	private RemoteTransmitter remoteTransmitter;
		
	
	private class ServerConsoleControlsSocketAdapter extends ConsoleControlsSocket {
		@Override
		public void controlStateChanged(Control control, boolean state) {
			// Send the speed control directly
			if (control == Control.FAST_SPEED || control.isStateControl()) {
				super.controlStateChanged(control, state);
				return;
			}
			synchronized (queuedChanges) {
				queuedChanges.add(new ControlChange(control, state));
			}
		}
		private List<ControlChange> commitAndGetChangesToSend() {
			List<ControlChange> changesToSend;
			synchronized (queuedChanges) {
				if (queuedChanges.isEmpty())
					changesToSend = emptyChanges;
				else {
					changesToSend = new ArrayList<ControlChange>(queuedChanges);
					queuedChanges.clear();
				}
			}
			// Effectively process the control changes 
			for (ControlChange change : changesToSend)
				super.controlStateChanged(change.control, change.state);
			return changesToSend;
		}
		private List<ControlChange> queuedChanges = new ArrayList<ControlChange>();
		private List<ControlChange> emptyChanges = new ArrayList<ControlChange>();
	}

	private class ServerConsoleCartridgeSocketAdapter extends CartridgeSocketAdapter {}

	private class ServerConsoleSaveStateSourceAdapter extends SaveStateSocketAdapter {}

}
