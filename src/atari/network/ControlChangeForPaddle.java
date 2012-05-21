// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.network;

import atari.controls.ConsoleControls.Control;

public class ControlChangeForPaddle extends ControlChange {

	public ControlChangeForPaddle(Control control, int position) {
		this.control = control;
		this.position = position;
	}

	public int position;

	public static final long serialVersionUID = 1L;

}