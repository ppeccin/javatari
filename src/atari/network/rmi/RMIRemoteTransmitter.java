// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.network.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JOptionPane;

import parameters.Parameters;
import atari.network.ControlChange;
import atari.network.RemoteTransmitter;
import atari.network.ServerConsole;
import atari.network.ServerUpdate;

public class RMIRemoteTransmitter extends UnicastRemoteObject implements RMIRemoteTransmitterInterface, RemoteTransmitter {

	public RMIRemoteTransmitter() throws RemoteException {
		updates = new ConcurrentLinkedQueue<ServerUpdate>();
	}

	@Override
	public void connectReceiver(RMIRemoteReceiverInterface receiver) {
		resetUpdatesPending();
		this.receiver = receiver;
		updatesSender = new UpdatesSender();
		updatesSender.start();
		console.clientConnected();
	}

	@Override
	public void serverConsole(ServerConsole console) {
		this.console = console;
	}

	@Override
	public void sendUpdate(ServerUpdate update) {
		if (receiver == null) return;
		synchronized (updates) {
			while(updates.size() > MAX_UPDATES_PENDING) {
				try {
					updates.wait();
				} catch (InterruptedException e) {}
			}
			updates.add(update);
			updates.notifyAll();
		}
	}

	@Override
	public boolean isClientConnected() {
		return receiver != null;
	}

	public void listen() {
		try {
			Registry reg = LocateRegistry.createRegistry(SERVICE_PORT);
			reg.bind(SERVICE_NAME, this);
		} catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Unnable to start server!", "Atari P1 Server", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void disconnectReceiver() {
		receiver = null;
		resetUpdatesPending();
		console.clientDisconnected();
	}

	private void resetUpdatesPending() {
		synchronized (updates) {
			updates.clear();		
			updates.notifyAll();
		}
	}
	
	private ConcurrentLinkedQueue<ServerUpdate> updates;
	private UpdatesSender updatesSender;
	
	private ServerConsole console;
	private RMIRemoteReceiverInterface receiver;
	
	private static final int MAX_UPDATES_PENDING = Parameters.SERVER_MAX_UPDATES_PENDING;
	
	public static final String SERVICE_NAME = Parameters.SERVER_SERVICE_NAME;
	public static final int SERVICE_PORT = Parameters.SERVER_SERVICE_PORT;
	
	public static final long serialVersionUID = 1L;

	
	private class UpdatesSender extends Thread {
		@Override
		public void run() {
			ServerUpdate update;
			while(true) {
				synchronized(updates) {
					while((update = updates.poll()) == null) {
						try {
							updates.wait();
						} catch (InterruptedException e) {}
					}
					updates.notifyAll();
				}
				try {
					if (receiver != null) { 
						List<ControlChange> clientControlChages = receiver.receiveServerUpdate(update);
						console.receiveClientControlChanges(clientControlChages);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
					disconnectReceiver();
				}
			}
		}
	}

}
