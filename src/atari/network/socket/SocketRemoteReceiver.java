// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.network.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import parameters.Parameters;
import atari.network.ClientConsole;
import atari.network.RemoteReceiver;
import atari.network.ServerUpdate;

public class SocketRemoteReceiver implements RemoteReceiver {

	public SocketRemoteReceiver() {
		updates = new ConcurrentLinkedQueue<ServerUpdate>();
	}

	public void start(String server) throws IOException {
		connect(server);
	}
	
	public void stop() throws IOException {
		if (socket != null && !socket.isClosed()) socket.close();	// Will stop the receiver loop and disconnect
	}

	@Override
	public void clientConsole(ClientConsole console) {
		this.console = console;
	}
	
	private void connect(String server) throws IOException {
		try {
			socket = new Socket(server, SocketRemoteTransmitter.SERVICE_PORT);
			socket.setTcpNoDelay(true);
			socketOutputStream = socket.getOutputStream();
			outputStream = new ObjectOutputStream(socketOutputStream);
			socketInputStream = socket.getInputStream();
			inputStream = new ObjectInputStream(socketInputStream);
		} catch (IOException ex) {
			disconnect();
			throw ex;
		}
		resetUpdatesPending();
		updatesReceiver = new UpdatesReceiver();
		updatesReceiver.start();
		updatesConsumer = new UpdatesConsumer();
		updatesConsumer.start();
		console.connected();
	}
	
	private void disconnect() {
		boolean wasConnected = inputStream != null;
		cleanStreamsSilently();
		if (updatesConsumer != null) updatesConsumer.interrupt();	// Will stop the consumer loop
		if (wasConnected) console.disconnected();
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
		synchronized (updates) {
			updates.clear();
			updates.notifyAll();
		}
	}

	
	private ClientConsole console;
	
	private Socket socket;

	private ConcurrentLinkedQueue<ServerUpdate> updates;
	private UpdatesConsumer updatesConsumer;
	private UpdatesReceiver updatesReceiver;
	private OutputStream socketOutputStream;
	private InputStream socketInputStream;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	private static final int MAX_UPDATES_PENDING = Parameters.CLIENT_MAX_UPDATES_PENDING;


	private class UpdatesReceiver extends Thread {
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
			disconnect();
			updatesReceiver = null;
		}
	}

	private class UpdatesConsumer extends Thread {
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


