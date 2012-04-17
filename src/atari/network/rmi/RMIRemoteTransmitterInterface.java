// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.network.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIRemoteTransmitterInterface extends Remote {

	public void connectReceiver(RMIRemoteReceiverInterface receiver) throws RemoteException;
	
}
