// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.network;

import org.javatari.atari.controls.ConsoleControls.Control;

public final class ControlChangeForPaddle extends ControlChange {

	public ControlChangeForPaddle(Control control, int position) {
		this.control = control;
		this.position = position;
	}

	public int position;

	public static final long serialVersionUID = 1L;

}