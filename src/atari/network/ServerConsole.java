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
		if (remoteTransmitter != null && remoteTransmitter.isClientConnected()) {
			ServerUpdate update = new ServerUpdate();
			update.controlChanges = controlChanges;
			update.isClockPulse = powerOn;
			remoteTransmitter.sendUpdate(update);
		}
	}

	@Override
	protected synchronized void powerFry() {
		super.powerFry();
		sendStateUpdate();
	}


	public synchronized void clientConnected() {
		showOSD("Player 2 Client Connected");
		sendStateUpdate();
	}

	public void clientDisconnected() {
		showOSD("Player 2 Client Disconnected");
	}

	public void receiveClientControlChanges(List<ControlChange> clientControlChages) {
		for (ControlChange change : clientControlChages)
			if (change instanceof ControlChangeForPaddle)
				controlsSocket.controlStateChanged(change.control, ((ControlChangeForPaddle)change).position);
			else
				controlsSocket.controlStateChanged(change.control, change.state);
	}

	private void sendStateUpdate() {
		if (remoteTransmitter != null && remoteTransmitter.isClientConnected()) {
			ServerUpdate update = new ServerUpdate();
			update.powerOn = powerOn;
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
			// Send some controls directly and locally only
			if (control == Control.FAST_SPEED || control == Control.POWER_FRY || control.isStateControl()) {
				super.controlStateChanged(control, state);
				return;
			}
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
		private List<ControlChange> commitAndGetChangesToSend() {
			List<ControlChange> changesToSend;
			synchronized (queuedChanges) {
				if (queuedChanges.isEmpty())
					return null;
				else {
					changesToSend = new ArrayList<ControlChange>(queuedChanges);
					queuedChanges.clear();
				}
			}
			// Effectively process the control changes 
			for (ControlChange change : changesToSend)
				if (change instanceof ControlChangeForPaddle)
					super.controlStateChanged(change.control, ((ControlChangeForPaddle)change).position);
				else
					super.controlStateChanged(change.control, change.state);
			return changesToSend;
		}
		private List<ControlChange> queuedChanges = new ArrayList<ControlChange>();
	}

	private class ServerConsoleCartridgeSocketAdapter extends CartridgeSocketAdapter {}

	private class ServerConsoleSaveStateSourceAdapter extends SaveStateSocketAdapter {}

}
