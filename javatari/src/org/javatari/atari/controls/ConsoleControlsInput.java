// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.controls;

import java.util.Map;

public interface ConsoleControlsInput {

	public void controlStateChanged(ConsoleControls.Control control, boolean state);
	public void controlStateChanged(ConsoleControls.Control control, int position);

	public void controlsStateReport(Map<ConsoleControls.Control, Boolean> report);
	
}