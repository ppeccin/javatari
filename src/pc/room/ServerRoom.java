// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.room;

import java.io.IOException;

import atari.network.ServerConsole;
import atari.network.socket.SocketRemoteTransmitter;

public class ServerRoom extends Room {
	
	public void startServer() throws IOException {
		remoteTransmitter.start();
	}

	public void stopServer() throws IOException {
		remoteTransmitter.stop();
	}

	@Override
	protected void buildConsole() {
		remoteTransmitter = new SocketRemoteTransmitter();
		console = new ServerConsole(remoteTransmitter);
	}


	private SocketRemoteTransmitter remoteTransmitter;

}
