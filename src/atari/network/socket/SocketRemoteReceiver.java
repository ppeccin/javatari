// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.network.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JOptionPane;

import parameters.Parameters;
import atari.network.ClientConsole;
import atari.network.RemoteReceiver;
import atari.network.ServerUpdate;

public class SocketRemoteReceiver implements RemoteReceiver {

	public SocketRemoteReceiver() throws RemoteException {
		serverUpdates = new ConcurrentLinkedQueue<ServerUpdate>();
	}

	@Override
	public void clientConsole(ClientConsole console) {
		this.console = console;
	}
	
	public void connect(String server) throws UnknownHostException, IOException {
		Socket socket = new Socket(server, SocketRemoteTransmitter.SERVICE_PORT);
		socket.setTcpNoDelay(true);
		socketOutputStream = socket.getOutputStream();
		outputStream = new ObjectOutputStream(socketOutputStream);
		socketInputStream = socket.getInputStream();
		inputStream = new ObjectInputStream(socketInputStream);
		updatesReceiver = new UpdatesReceiver();
		updatesReceiver.start();
		updatesConsumer = new UpdatesConsumer();
		updatesConsumer.start();
		console.connected();
	}
	
	private void disconnect() {
		outputStream = null;
		inputStream = null;
		console.disconnected();
	}

	private void receiveServerUpdate(ServerUpdate update) {
		synchronized (serverUpdates) {
			while(serverUpdates.size() > MAX_UPDATES_PENDING) {
				try {
					serverUpdates.wait();
				} catch (InterruptedException e) {}
			}
			serverUpdates.add(update);
			serverUpdates.notifyAll();
		}
	}
	
	public boolean askUserForConnection(String server) {
	 	boolean connected = false;
 		while(!connected) {
	 		while (server == null || server.isEmpty()) {
				server = JOptionPane.showInputDialog(null, "Atari Player 1 Server address:", "Atari Player 2 Client", JOptionPane.PLAIN_MESSAGE);
		 		if(server == null) return false;
			} 
 			// Connect to Server
	 		try {
	 			connect(server);
	 			connected = true; 
	 		} catch(Exception e) {
	 			e.printStackTrace();
	 			JOptionPane.showMessageDialog(null, "Unnable to connect to: " + server, "Atari Player 2 Client", JOptionPane.ERROR_MESSAGE);
	 			server = null;
	 		}
	 	}
 		return true;
	}
	
	
	private ClientConsole console;
	
	private ConcurrentLinkedQueue<ServerUpdate> serverUpdates;
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
			} catch (IOException e) {
				disconnect();
			} catch (ClassNotFoundException e) {
				disconnect();
			}
	
		}
	}

	private class UpdatesConsumer extends Thread {
		@Override
		public void run() {
			ServerUpdate update;
			while(inputStream != null) {
				synchronized(serverUpdates) {
					while((update = serverUpdates.poll()) == null) {
						try {
							serverUpdates.wait();
						} catch (InterruptedException e) {}
					}
					serverUpdates.notifyAll();
				}
				console.receiveServerUpdate(update);
				yield();
			}
		}
	}

}


