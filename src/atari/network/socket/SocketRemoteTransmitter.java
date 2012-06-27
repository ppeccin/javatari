// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.network.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import parameters.Parameters;
import atari.network.ControlChange;
import atari.network.RemoteTransmitter;
import atari.network.ServerConsole;
import atari.network.ServerUpdate;

public class SocketRemoteTransmitter implements RemoteTransmitter {

	public SocketRemoteTransmitter() {
		updates = new ConcurrentLinkedQueue<ServerUpdate>();
	}

	public void start() throws IOException {
		// Open the serverSocket the first time to get errors early here
		serverSocket = new ServerSocket(SERVICE_PORT);
		updatesSender = new UpdatesSender();
		updatesSender.start();
	}

	public void stop() throws IOException {
		started = false;
		if (updatesSender != null) updatesSender.interrupt();	// Will stop the sender loop
		// Also stop listening serverSocket if needed
		if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
	}

	@Override
	public void serverConsole(ServerConsole console) {
		this.console = console;
	}

	@Override
	public void sendUpdate(ServerUpdate update) {
		if (outputStream == null) return;
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
		return outputStream != null;
	}

	private void listen() throws IOException {
		// Reopen the serverSocked if needed (2nd client connection and so on)
		if (serverSocket == null || serverSocket.isClosed())
			serverSocket = new ServerSocket(SERVICE_PORT);
		Socket conn = serverSocket.accept();
		serverSocket.close();
		connect(conn);
	}

	private void connect(Socket toSocket) throws IOException {
		socket = toSocket;
		socket.setTcpNoDelay(true);
		socketOutputStream = socket.getOutputStream();
		outputStream = new ObjectOutputStream(socketOutputStream);
		socketInputStream = socket.getInputStream();
		inputStream = new ObjectInputStream(socketInputStream);
		resetUpdatesPending();
		console.clientConnected();
	}

	private void disconnect() {
		boolean wasConnected = outputStream != null;
		cleanStreamsSilently();
		resetUpdatesPending();
		if (wasConnected) console.clientDisconnected();
	}

	private void cleanStreamsSilently() {
		try { socket.close(); } catch (Exception e) {}
		try { socketOutputStream.close(); } catch (Exception e) {}
		try { socketInputStream.close(); } catch (Exception e) {}
		socket = null;
		socketOutputStream = null; outputStream = null;
		socketInputStream = null; inputStream = null;
	}

	private void resetUpdatesPending() {
		synchronized (updates) {
			updates.clear();		
			updates.notifyAll();
		}
	}
	

	private boolean started = false;
	private ServerSocket serverSocket;
	private Socket socket;

	private ServerConsole console;

	private ConcurrentLinkedQueue<ServerUpdate> updates;
	private UpdatesSender updatesSender;
	private OutputStream socketOutputStream;
	private InputStream socketInputStream;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	
	private static final int MAX_UPDATES_PENDING = Parameters.SERVER_MAX_UPDATES_PENDING;
	
	public static final int SERVICE_PORT = Parameters.SERVER_SERVICE_PORT;
	
	
	private class UpdatesSender extends Thread {
		@Override
		public void run() {
			started = true;
			try {
				while (started) {
					listen();
					try {
						ServerUpdate update;
						while (started) {
							synchronized (updates) {
								while ((update = updates.poll()) == null)
									updates.wait();
								updates.notifyAll();
							}
							if (started && update != null) {
								synchronized (outputStream) {
									outputStream.writeObject(update);
									outputStream.flush();
									socketOutputStream.flush();
									@SuppressWarnings("unchecked")
									List<ControlChange> clientControlChanges = 
										(List<ControlChange>) inputStream.readObject();
									if (clientControlChanges != null)
										console.receiveClientControlChanges(clientControlChanges);
								}
							}
						}
					} catch (Exception ex) {
					} 
					// Exception while connected or interrupted. Try to disconnect
					disconnect();
				}
			} catch (Exception ex) {
				// Exception while listening or connecting or interrupted. Try to disconnect
				disconnect();
			}
			started = false;
			updatesSender = null;
		}
	}

}
