// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.network;

import java.io.Serializable;
import java.util.List;

import atari.console.savestate.ConsoleState;

public final class ServerUpdate implements Serializable {

	public Boolean powerOn = null;
	public List<ControlChange> controlChanges = null;
	public ConsoleState consoleState = null;
	public boolean isClockPulse = false;
	
	public static final long serialVersionUID = 1L;

}
