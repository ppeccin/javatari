// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.network.rmi;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import parameters.Parameters;
import atari.network.ClientConsole;
import atari.network.ControlChange;
import atari.network.RemoteReceiver;
import atari.network.ServerUpdate;

public class RMIRemoteReceiver extends UnicastRemoteObject implements RMIRemoteReceiverInterface, RemoteReceiver {

	public RMIRemoteReceiver() throws RemoteException {
		updates = new ConcurrentLinkedQueue<ServerUpdate>();
	}

	@Override
	public void clientConsole(ClientConsole console) {
		this.console = console;
	}
	
	@Override
	public List<ControlChange> receiveServerUpdate(ServerUpdate update) {
		synchronized (updates) {
			while(updates.size() > MAX_UPDATES_PENDING) {
				try {
					updates.wait();
				} catch (InterruptedException e) {}
			}
			updates.add(update);
			updates.notifyAll();
		}
		return console.controlChangesToSend();
	}
	
	public void connect(String server) throws RemoteException, MalformedURLException, NotBoundException {
		Registry reg = LocateRegistry.getRegistry(server, RMIRemoteTransmitter.SERVICE_PORT);
		transmitter = ((RMIRemoteTransmitterInterface) reg.lookup(RMIRemoteTransmitter.SERVICE_NAME));
		transmitter.connectReceiver(this);
		updatesConsumer = new UpdatesConsumer();
		updatesConsumer.start();
		console.connected();
	}
	

	private ClientConsole console;
	private RMIRemoteTransmitterInterface transmitter;
	
	private ConcurrentLinkedQueue<ServerUpdate> updates;
	private UpdatesConsumer updatesConsumer;

	private static final int MAX_UPDATES_PENDING = Parameters.CLIENT_MAX_UPDATES_PENDING;

	public static final long serialVersionUID = 1L;


	private class UpdatesConsumer extends Thread {
		@Override
		public void run() {
			ServerUpdate update;
			while(true) {
				synchronized(updates) {
					while((update = updates.poll()) == null) {
						try {
							updates.wait(0);
						} catch (InterruptedException e) {}
					}
					updates.notifyAll();
				}
				console.receiveServerUpdate(update);
				yield();
			}
		}
	}

}


