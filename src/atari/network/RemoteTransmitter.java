// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.network;

public interface RemoteTransmitter {

	public void serverConsole(ServerConsole console);
	
	public void sendUpdate(ServerUpdate update);
	
	public boolean isClientConnected();
	
}
