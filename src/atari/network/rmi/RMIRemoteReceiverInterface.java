// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.network.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import atari.network.ControlChange;
import atari.network.ServerUpdate;

public interface RMIRemoteReceiverInterface extends Remote {
	
	public List<ControlChange> receiveServerUpdate(ServerUpdate update) throws RemoteException;
	
}


