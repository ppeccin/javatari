// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.network;

import java.io.Serializable;

import atari.controls.ConsoleControls.Control;

public class ControlChange implements Serializable {

	// TODO Bug dos paddles em multiplayer
	
	public ControlChange(Control control, boolean state) {
		this.control = control;
		this.state = state;
	}

	public Control control;
	public boolean state;

	public static final long serialVersionUID = 1L;

}