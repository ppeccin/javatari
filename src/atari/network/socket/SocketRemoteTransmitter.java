// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.network.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JOptionPane;

import parameters.Parameters;
import atari.network.ControlChange;
import atari.network.RemoteTransmitter;
import atari.network.ServerUpdate;
import atari.network.ServerConsole;

public class SocketRemoteTransmitter implements RemoteTransmitter {

	public SocketRemoteTransmitter() throws RemoteException {
		updates = new ConcurrentLinkedQueue<ServerUpdate>();
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

	public boolean listen() {
		try {
			ServerSocket serverSocket = new ServerSocket(SERVICE_PORT);
			connectReceiver(serverSocket.accept());
			serverSocket.close();
		} catch(Exception e) {
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(null, "Unnable to open socket!", "Atari P1 Server", JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
		return true;
	}

	private void connectReceiver(Socket socket) throws IOException {
		socket.setTcpNoDelay(true);
		socketOutputStream = socket.getOutputStream();
		outputStream = new ObjectOutputStream(socketOutputStream);
		socketInputStream = socket.getInputStream();
		inputStream = new ObjectInputStream(socketInputStream);
		resetUpdatesPending();
		updatesSender = new UpdatesSender();
		updatesSender.start();
		console.clientConnected();
	}

	private void disconnectReceiver() {
		outputStream = null;
		inputStream = null;
		resetUpdatesPending();
		console.clientDisconnected();
		listen();
	}

	private void resetUpdatesPending() {
		synchronized (updates) {
			updates.clear();		
			updates.notifyAll();
		}
	}
	

	private ServerConsole console;
	
	private ConcurrentLinkedQueue<ServerUpdate> updates;
	private UpdatesSender updatesSender;
	private OutputStream socketOutputStream;
	private InputStream socketInputStream;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	
	private static final int MAX_UPDATES_PENDING = Parameters.SERVER_MAX_UPDATES_PENDING;
	
	public static final String SERVICE_NAME = Parameters.SERVER_SERVICE_NAME;
	public static final int SERVICE_PORT = Parameters.SERVER_SERVICE_PORT;
	
	
	private class UpdatesSender extends Thread {
		@Override
		public void run() {
			ServerUpdate update;
			try {
				while(true) {
					synchronized(updates) {
						while((update = updates.poll()) == null) {
							try {
								updates.wait();
							} catch (InterruptedException e) {}
						}
						updates.notifyAll();
					}
					if (outputStream != null) {
						outputStream.writeObject(update);
						outputStream.flush();
						socketOutputStream.flush();
						@SuppressWarnings("unchecked")
						List<ControlChange> clientControlChanges = (List<ControlChange>) inputStream.readObject();
						console.receiveClientControlChanges(clientControlChanges);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				disconnectReceiver();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				disconnectReceiver();
			}
		}
	}

}
