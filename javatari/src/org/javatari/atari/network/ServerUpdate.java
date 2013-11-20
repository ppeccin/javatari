// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.network;

import java.io.Serializable;
import java.util.List;

import org.javatari.atari.console.savestate.ConsoleState;


public final class ServerUpdate implements Serializable {

	public Boolean powerChange = null;
	public List<ControlChange> controlChanges = null;
	public ConsoleState consoleState = null;
	
	public static final long serialVersionUID = 2L;

}
