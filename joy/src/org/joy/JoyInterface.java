// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.joy;

import org.joy.Joy.Info;
import org.joy.Joy.State;

public class JoyInterface {

	static synchronized native int getMaxDevices();
	static synchronized native boolean isConnected(int id);
	static synchronized native boolean updateInfo(int id, Info info);
	static synchronized native boolean updateState(int id, State state);

}
