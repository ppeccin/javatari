// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.controls;

public interface ConsoleControls {

	public static enum Control {
		JOY0_UP, JOY0_DOWN, JOY0_LEFT, JOY0_RIGHT, JOY0_BUTTON,
		JOY1_UP, JOY1_DOWN, JOY1_LEFT, JOY1_RIGHT, JOY1_BUTTON,
		PADDLE0_POSITION, PADDLE1_POSITION,		// Position from 380 (Left) to 190 (Center) to 0 (Right); -1 = disconnected, won't charge POTs
		PADDLE0_BUTTON, PADDLE1_BUTTON,
		POWER, BLACK_WHITE, SELECT, RESET,
		DIFFICULTY0, DIFFICULTY1,
		DEBUG, NO_COLLISIONS, TRACE, PAUSE, FRAME, FAST_SPEED,
		CARTRIDGE_FORMAT, CARTRIDGE_CLOCK_DEC, CARTRIDGE_CLOCK_INC,
		VIDEO_STANDARD, POWER_FRY,
		
		SAVE_STATE_FILE,
		SAVE_STATE_0(0), SAVE_STATE_1(1), SAVE_STATE_2(2), SAVE_STATE_3(3), SAVE_STATE_4(4), SAVE_STATE_5(5),
		SAVE_STATE_6(6), SAVE_STATE_7(7), SAVE_STATE_8(8), SAVE_STATE_9(9), SAVE_STATE_10(10), SAVE_STATE_11(11), SAVE_STATE_12(12),
		LOAD_STATE_0(0), LOAD_STATE_1(1), LOAD_STATE_2(2), LOAD_STATE_3(3), LOAD_STATE_4(4), LOAD_STATE_5(5), 
		LOAD_STATE_6(6), LOAD_STATE_7(7), LOAD_STATE_8(8), LOAD_STATE_9(9), LOAD_STATE_10(10), LOAD_STATE_11(11), LOAD_STATE_12(12); 
		
		Control() {
			this(-1);
		}
		Control(int slot) {
			this.slot = slot;
		}
		public boolean isStateControl() {
			return slot >= 0;
		}
		public final int slot;
	};
	
	
	public static final Control[] playerDigitalControls = new Control[] {
		Control.JOY0_UP, Control.JOY0_DOWN, Control.JOY0_LEFT, Control.JOY0_RIGHT, Control.JOY0_BUTTON,
		Control.JOY1_UP, Control.JOY1_DOWN, Control.JOY1_LEFT, Control.JOY1_RIGHT, Control.JOY1_BUTTON,
		Control.PADDLE0_BUTTON, Control.PADDLE1_BUTTON
	};

}