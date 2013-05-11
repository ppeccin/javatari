// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.javatari.parameters.Parameters;


public final class RemoteReceiver {

	public RemoteReceiver() {
		updates = new ConcurrentLinkedQueue<ServerUpdate>();
	}

	public void connect(String server) throws IOException {
		tryConnection(server);
	}
	
	public void disconnect() throws IOException {
		if (socket == null || socket.isClosed()) return; 
		UpdatesReceiver rec = updatesReceiver;
		UpdatesConsumer cons = updatesConsumer;
		socket.close();	// Will stop the receiver loop and disconnect
		try {
			if (rec != null) rec.join();	// Wait for disconnection to complete
			if (cons != null) cons.join();
		} catch (InterruptedException e) {
			// No problem
		}
	}

	public boolean isConnected() {
		return inputStream != null;
	}
	
	public String serverAddress() {
		return serverAddress;
	}
	
	public void clientConsole(ClientConsole console) {
		this.console = console;
	}
	
	public void addConnectionStatusListener(ConnectionStatusListener lis) {
		if (!connectionListeners.contains(lis)) connectionListeners.add(lis);
	}

	private void tryConnection(String serverAddress) throws IOException, IllegalArgumentException {
		this.serverAddress = serverAddress;
		try {
			String addr = getHost(serverAddress);
			int port = getPort(serverAddress);
			socket = new Socket(addr, port);
			socket.setTcpNoDelay(true);
			socketOutputStream = socket.getOutputStream();
			outputStream = new ObjectOutputStream(socketOutputStream);
			socketInputStream = socket.getInputStream();
			inputStream = new ObjectInputStream(socketInputStream);
		} catch (IOException ex) {
			disconnection();
			throw ex;
		}
		resetUpdatesPending();
		updatesReceiver = new UpdatesReceiver();
		updatesReceiver.start();
		updatesConsumer = new UpdatesConsumer();
		updatesConsumer.start();
		console.connected();
		notifyConnectionStatusListeners();	
	}
	
	private String getHost(String serverAddress) {
		int divider = serverAddress.indexOf(":");
		if (divider < 0) return serverAddress;
		else return serverAddress.substring(0, divider).trim();
	}

	private int getPort(String serverAddress) throws IllegalArgumentException {
		int divider = serverAddress.indexOf(":");
		String p = "";
		try {
			if (divider < 0) return Parameters.SERVER_SERVICE_PORT;
			else {
				p = serverAddress.substring(divider + 1).trim();
				return Integer.valueOf(p);
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid port number: " + p);
		}
	}

	private void disconnection() {
		boolean wasConnected = inputStream != null;
		cleanStreamsSilently();
		if (updatesConsumer != null) updatesConsumer.interrupt();	// Will stop the consumer loop
		if (wasConnected) {
			console.disconnected();
			notifyConnectionStatusListeners();
		}
	}

	private void cleanStreamsSilently() {
		try { socket.close(); } catch (Exception e) {}
		try { socketOutputStream.close(); } catch (Exception e) {}
		try { socketInputStream.close(); } catch (Exception e) {}
		socket = null;
		socketOutputStream = null; outputStream = null;
		socketInputStream = null; inputStream = null;
	}

	private void receiveServerUpdate(ServerUpdate update) {
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
	
	private void resetUpdatesPending() {
		console.controlChangesToSend();		// Will clear any remaining entries
		synchronized (updates) {
			updates.clear();
			updates.notifyAll();
		}
	}

	private void notifyConnectionStatusListeners() {
		for (ConnectionStatusListener lis : connectionListeners)
			lis.connectionStatusChanged();
	}


	
	private ClientConsole console;
	
	private Socket socket;
	private String serverAddress;

	private ConcurrentLinkedQueue<ServerUpdate> updates;
	private UpdatesConsumer updatesConsumer;
	private UpdatesReceiver updatesReceiver;
	private OutputStream socketOutputStream;
	private InputStream socketInputStream;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	
	private List<ConnectionStatusListener> connectionListeners = new ArrayList<ConnectionStatusListener>();

	private static final int MAX_UPDATES_PENDING = Parameters.CLIENT_MAX_UPDATES_PENDING;


	private class UpdatesReceiver extends Thread {
		public UpdatesReceiver() {
			super("RemoteReceiver Receiver");
		}
		@Override
		public void run() {
			ServerUpdate update;
			try {
				while(inputStream != null) {
					update = (ServerUpdate) inputStream.readObject();
					outputStream.writeObject(console.controlChangesToSend());
					outputStream.flush();
					socketOutputStream.flush();
					receiveServerUpdate(update);
				}
			} catch (Exception ex) {
			}
			// Exception while receiving update, or interrupted. Try to disconnect
			disconnection();
			updatesReceiver = null;
		}
	}

	private class UpdatesConsumer extends Thread {
		public UpdatesConsumer() {
			super("RemoteReceiver Consumer");
		}
		@Override
		public void run() {
			ServerUpdate update;
			try {
				while (inputStream != null) {
					synchronized(updates) {
						while((update = updates.poll()) == null) {
							updates.wait();
						}
						updates.notifyAll();
					}
					if (inputStream != null && update != null) {
						console.receiveServerUpdate(update);
						yield();
					}
				}
			} catch (InterruptedException ex) {
				// Someone wants to end the consumer
			}
			updatesConsumer = null;
		}
	}

}


