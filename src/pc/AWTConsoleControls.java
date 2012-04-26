// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc;

import general.av.video.VideoMonitor;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

import atari.controls.ConsoleControls;
import atari.controls.ConsoleControlsInput;

public class AWTConsoleControls implements ConsoleControls, KeyListener {
	
	public AWTConsoleControls(ConsoleControlsInput input, VideoMonitor monitor, Component... sourceComponents) {
		videoMonitor = monitor;
		consoleControlsInput = input;
		for (int i = 0; i < sourceComponents.length; i++) {
			sourceComponents[i].setFocusTraversalKeysEnabled(false);
			sourceComponents[i].addKeyListener(this);
		}
		init();
	}

	public void p1ControlsMode(boolean state) {
		p1ControlsMode = state;
	}
	
	public void paddleMode(boolean state) {
		if (paddleMode != state) paddleModeToggle();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// Do nothing
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		int modifiers = e.getModifiersEx();
		if (p1ControlsMode)	keyCode = translateP1PriorityKeyCode(keyCode);
		if (checkLocalControlKeyPressed(keyCode, modifiers)) return;
		Control control = controlForEvent(keyCode, modifiers);
		if (control == null) return;
		Boolean state = controlStateMap.get(control);
		if (state == null || !state) {
			controlStateMap.put(control, true);
			consoleControlsInput.controlStateChanged(control, true);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		int modifiers = e.getModifiersEx();
		if (p1ControlsMode)	keyCode = translateP1PriorityKeyCode(keyCode);
		if (checkLocalControlKeyReleased(keyCode, modifiers)) return;
		Control control = controlForEvent(keyCode, modifiers);
		if (control == null) return;
		Boolean state = controlStateMap.get(control);
		if (state == null || state) {
			controlStateMap.put(control, false);
			consoleControlsInput.controlStateChanged(control, false);
		}
	}

	private boolean checkLocalControlKeyPressed(int keyCode, int modif) {
		if (modif == KeyEvent.ALT_DOWN_MASK)
			switch(keyCode) {
				case KEY_LOCAL_P1_MODE:
					p1ControlsMode = !p1ControlsMode; 
					videoMonitor.showOSD((p1ControlsMode ? "P2" : "P1") + " controls priority");
					return true;
				case KEY_LOCAL_PADDLE_MODE:
					paddleModeToggle(); return true;
			}
		if (paddleMode) {
			switch(keyCode) {
				case KEY_P0_LEFT:
					paddle0MovingLeft = true; return true;
				case KEY_P0_RIGHT:
					paddle0MovingRight = true; return true;
				case KEY_P0_UP:
					if (paddle0Speed < 10) paddle0Speed++;
					videoMonitor.showOSD("Paddle 1 speed: " + paddle0Speed);
					return true;
				case KEY_P0_DOWN:
					if (paddle0Speed > 1) paddle0Speed--;
					videoMonitor.showOSD("Paddle 1 speed: " + paddle0Speed);
					return true;
				case KEY_P1_LEFT:
					paddle1MovingLeft = true; return true;
				case KEY_P1_RIGHT:
					paddle1MovingRight = true; return true;
				case KEY_P1_UP:
					if (paddle1Speed < 10) paddle1Speed++;
					videoMonitor.showOSD("Paddle 2 speed: " + paddle1Speed);
					return true;
				case KEY_P1_DOWN:
					if (paddle1Speed > 1) paddle1Speed--;
					videoMonitor.showOSD("Paddle 2 speed: " + paddle1Speed);
					return true;
			}			
		}
		return false;
	}

	private boolean checkLocalControlKeyReleased(int keyCode, int modif) {
		if (paddleMode) {
			switch(keyCode) {
				case KEY_P0_LEFT:
					paddle0MovingLeft = false; return true;
				case KEY_P0_RIGHT:
					paddle0MovingRight = false; return true;
				case KEY_P1_LEFT:
					paddle1MovingLeft = false; return true;
				case KEY_P1_RIGHT:
					paddle1MovingRight = false; return true;
			}			
		}
		return false;
	}

	private Control controlForEvent(int keyCode, int modif) {
		Control control = null;
		switch (modif) {
			case 0:
				control = keyCodeMap.get(keyCode); break;
			case KeyEvent.CTRL_DOWN_MASK:
				control = keyControlCodeMap.get(keyCode); break;
			case KeyEvent.ALT_DOWN_MASK:
				control = keyAltCodeMap.get(keyCode); break;
		}
		if (control == null) return null; 
		if (paddleMode)	return translatePaddleModeControl(control);
		else return control;
	}

	private int translateP1PriorityKeyCode(int keyCode) {
		switch (keyCode) {
			case KEY_P0_UP: return KEY_P1_UP;
			case KEY_P0_DOWN: return KEY_P1_DOWN;
			case KEY_P0_LEFT: return KEY_P1_LEFT;
			case KEY_P0_RIGHT: return KEY_P1_RIGHT;
			case KEY_P0_BUTTON: return KEY_P1_BUTTON;
			case KEY_P0_BUTTON2: return KEY_P1_BUTTON;
			case KEY_P0_BUTTON3: return KEY_P1_BUTTON;
			case KEY_P1_UP: return KEY_P0_UP;
			case KEY_P1_DOWN: return KEY_P0_DOWN;
			case KEY_P1_LEFT: return KEY_P0_LEFT;
			case KEY_P1_RIGHT: return KEY_P0_RIGHT;
			case KEY_P1_BUTTON: return KEY_P0_BUTTON;
		}
		return keyCode;
	}
	
	private Control translatePaddleModeControl(Control control) {
		switch (control) {
			case JOY0_BUTTON: return Control.PADDLE0_BUTTON;
			case JOY1_BUTTON: return Control.PADDLE1_BUTTON;
			case JOY0_UP:
			case JOY0_DOWN:
			case JOY0_LEFT:
			case JOY0_RIGHT:
			case JOY1_UP:
			case JOY1_DOWN:
			case JOY1_LEFT:
			case JOY1_RIGHT:
				return null;
		}
		return control;
	}

	private void paddleModeToggle() {
		paddleMode = !paddleMode;
		if (paddleMode) {
			if (paddlePositionUpdater != null && paddlePositionUpdater.isAlive()) return;	// All set
			paddlePositionUpdater = new PaddlesPositionUpdater();
			paddlePositionUpdater.start();
		} else {
			if (paddlePositionUpdater == null || !paddlePositionUpdater.isAlive()) return;	// All set
			try { paddlePositionUpdater.join(); } catch (InterruptedException e) {}
			paddlePositionUpdater = null;
		}
		paddle0Position = paddle1Position = (paddleMode ? 190 : 0);
		paddle0MovingLeft = paddle0MovingRight = paddle1MovingLeft = paddle1MovingRight = false;
		paddle0Speed = paddle1Speed = 2;
		// Reset all controls to default state
		for (Control control : playerDigitalControls)
			consoleControlsInput.controlStateChanged(control, false);
		consoleControlsInput.controlStateChanged(Control.PADDLE0_POSITION, paddle0Position);
		consoleControlsInput.controlStateChanged(Control.PADDLE1_POSITION, paddle1Position);
		videoMonitor.showOSD((paddleMode ? "Paddles" : "Joysticks") + " connected");
	}

	private void paddlesUpdatePosition() {
		if (paddle0MovingLeft) {
			paddle0Position += paddle0Speed;
			if (paddle0Position > 380) paddle0Position = 380;
			consoleControlsInput.controlStateChanged(Control.PADDLE0_POSITION, paddle0Position);
		}
		if (paddle0MovingRight) {
			paddle0Position -= paddle0Speed;
			if (paddle0Position < 0) paddle0Position = 0;
			consoleControlsInput.controlStateChanged(Control.PADDLE0_POSITION, paddle0Position);
		}
		if (paddle1MovingLeft) {
			paddle1Position += paddle1Speed;
			if (paddle1Position > 380) paddle1Position = 380;
			consoleControlsInput.controlStateChanged(Control.PADDLE1_POSITION, paddle1Position);
		}
		if (paddle1MovingRight) {
			paddle1Position -= paddle1Speed;
			if (paddle1Position < 0) paddle1Position = 0;
			consoleControlsInput.controlStateChanged(Control.PADDLE1_POSITION, paddle1Position);
		}
	}

	private void init() {
		keyCodeMap.put(KEY_P0_LEFT,      Control.JOY0_LEFT); 
		keyCodeMap.put(KEY_P0_UP,        Control.JOY0_UP); 
		keyCodeMap.put(KEY_P0_RIGHT,     Control.JOY0_RIGHT); 
		keyCodeMap.put(KEY_P0_DOWN,      Control.JOY0_DOWN); 
		keyCodeMap.put(KEY_P0_BUTTON,    Control.JOY0_BUTTON); 
		keyCodeMap.put(KEY_P0_BUTTON2,   Control.JOY0_BUTTON); 
		keyCodeMap.put(KEY_P0_BUTTON3,   Control.JOY0_BUTTON); 
		keyCodeMap.put(KEY_P1_LEFT,      Control.JOY1_LEFT); 
		keyCodeMap.put(KEY_P1_UP,        Control.JOY1_UP); 
		keyCodeMap.put(KEY_P1_RIGHT,     Control.JOY1_RIGHT); 
		keyCodeMap.put(KEY_P1_DOWN,      Control.JOY1_DOWN); 
		keyCodeMap.put(KEY_P1_BUTTON,    Control.JOY1_BUTTON); 
		keyCodeMap.put(KEY_POWER,        Control.POWER); 
		keyCodeMap.put(KEY_BLACK_WHITE,  Control.BLACK_WHITE); 
		keyCodeMap.put(KEY_DIFFICULTY0,  Control.DIFFICULTY0); 
		keyCodeMap.put(KEY_DIFFICULTY1,  Control.DIFFICULTY1); 
		keyCodeMap.put(KEY_SELECT,       Control.SELECT); 
		keyCodeMap.put(KEY_RESET,        Control.RESET); 
		keyCodeMap.put(KEY_FAST_SPEED,   Control.FAST_SPEED); 
		
		keyAltCodeMap.put(KEY_PAUSE,         Control.PAUSE); 
		keyAltCodeMap.put(KEY_FRAME,         Control.FRAME); 
		keyAltCodeMap.put(KEY_TRACE,         Control.TRACE); 
		keyAltCodeMap.put(KEY_DEBUG,         Control.DEBUG); 
		keyAltCodeMap.put(KEY_DEBUG_NO_COL,  Control.DEBUG_NO_COLLISIONS); 
	
		keyControlCodeMap.put(KEY_STATE_0,   Control.SAVE_STATE_0); 
		keyControlCodeMap.put(KEY_STATE_1,   Control.SAVE_STATE_1); 
		keyControlCodeMap.put(KEY_STATE_2,   Control.SAVE_STATE_2); 
		keyControlCodeMap.put(KEY_STATE_3,   Control.SAVE_STATE_3); 
		keyControlCodeMap.put(KEY_STATE_4,   Control.SAVE_STATE_4); 
		keyControlCodeMap.put(KEY_STATE_5,   Control.SAVE_STATE_5); 
		keyControlCodeMap.put(KEY_STATE_6,   Control.SAVE_STATE_6); 
		keyControlCodeMap.put(KEY_STATE_7,   Control.SAVE_STATE_7); 
		keyControlCodeMap.put(KEY_STATE_8,   Control.SAVE_STATE_8); 
		keyControlCodeMap.put(KEY_STATE_9,   Control.SAVE_STATE_9); 
		keyControlCodeMap.put(KEY_STATE_10,  Control.SAVE_STATE_10); 
		keyControlCodeMap.put(KEY_STATE_11,  Control.SAVE_STATE_11); 
		keyControlCodeMap.put(KEY_STATE_12,  Control.SAVE_STATE_12); 

		keyAltCodeMap.put(KEY_STATE_0,  Control.LOAD_STATE_0); 
		keyAltCodeMap.put(KEY_STATE_1,  Control.LOAD_STATE_1); 
		keyAltCodeMap.put(KEY_STATE_2,  Control.LOAD_STATE_2); 
		keyAltCodeMap.put(KEY_STATE_3,  Control.LOAD_STATE_3); 
		keyAltCodeMap.put(KEY_STATE_4,  Control.LOAD_STATE_4); 
		keyAltCodeMap.put(KEY_STATE_5,  Control.LOAD_STATE_5); 
		keyAltCodeMap.put(KEY_STATE_6,  Control.LOAD_STATE_6); 
		keyAltCodeMap.put(KEY_STATE_7,  Control.LOAD_STATE_7); 
		keyAltCodeMap.put(KEY_STATE_8,  Control.LOAD_STATE_8); 
		keyAltCodeMap.put(KEY_STATE_9,  Control.LOAD_STATE_9); 
		keyAltCodeMap.put(KEY_STATE_10, Control.LOAD_STATE_10); 
		keyAltCodeMap.put(KEY_STATE_11, Control.LOAD_STATE_11); 
		keyAltCodeMap.put(KEY_STATE_12, Control.LOAD_STATE_12); 
	}


	private boolean p1ControlsMode = false;

	private boolean paddleMode = false;
	private int paddle0Position = 0;			// 380 = LEFT, 190 = MIDDLE, 0 = RIGHT
	private int paddle0Speed = 2;				// 1 to 10
	private boolean paddle0MovingLeft = false;
	private boolean paddle0MovingRight = false;
	private int paddle1Position = 0;
	private int paddle1Speed = 2;				
	private boolean paddle1MovingLeft = false;
	private boolean paddle1MovingRight = false;
	private PaddlesPositionUpdater paddlePositionUpdater;

	private final VideoMonitor videoMonitor;

	private ConsoleControlsInput consoleControlsInput; 
	private final Map<Integer, ConsoleControls.Control> keyCodeMap = new HashMap<Integer, ConsoleControls.Control>();
	private final Map<Integer, ConsoleControls.Control> keyControlCodeMap = new HashMap<Integer, ConsoleControls.Control>();
	private final Map<Integer, ConsoleControls.Control> keyAltCodeMap = new HashMap<Integer, ConsoleControls.Control>();
	
	private final Map<ConsoleControls.Control, Boolean> controlStateMap = new HashMap<ConsoleControls.Control, Boolean>();


	private static final int KEY_P0_UP          = KeyEvent.VK_UP;       
	private static final int KEY_P0_DOWN        = KeyEvent.VK_DOWN;   
	private static final int KEY_P0_LEFT        = KeyEvent.VK_LEFT;   
	private static final int KEY_P0_RIGHT       = KeyEvent.VK_RIGHT;
	private static final int KEY_P0_BUTTON      = KeyEvent.VK_SPACE;  
	private static final int KEY_P0_BUTTON2     = KeyEvent.VK_INSERT;  
	private static final int KEY_P0_BUTTON3     = KeyEvent.VK_DELETE;  
                                               
	private static final int KEY_P1_UP          = KeyEvent.VK_T;     
	private static final int KEY_P1_DOWN        = KeyEvent.VK_G;   
	private static final int KEY_P1_LEFT        = KeyEvent.VK_F;   
	private static final int KEY_P1_RIGHT       = KeyEvent.VK_H;  
	private static final int KEY_P1_BUTTON      = KeyEvent.VK_A;  
                                               
	private static final int KEY_POWER          = KeyEvent.VK_F1;     
	private static final int KEY_BLACK_WHITE    = KeyEvent.VK_F2;     
	private static final int KEY_DIFFICULTY0    = KeyEvent.VK_F4;  
	private static final int KEY_DIFFICULTY1    = KeyEvent.VK_F9;  
	private static final int KEY_SELECT         = KeyEvent.VK_F11;    
	private static final int KEY_RESET          = KeyEvent.VK_F12;    
                                               
	private static final int KEY_PAUSE          = KeyEvent.VK_P;
	private static final int KEY_FRAME          = KeyEvent.VK_F;
	private static final int KEY_TRACE          = KeyEvent.VK_T;
	private static final int KEY_FAST_SPEED     = KeyEvent.VK_TAB;
	private static final int KEY_DEBUG          = KeyEvent.VK_D;
	private static final int KEY_DEBUG_NO_COL   = KeyEvent.VK_C;
	
	private static final int KEY_STATE_0        = KeyEvent.VK_QUOTE;
	private static final int KEY_STATE_1        = KeyEvent.VK_1;
	private static final int KEY_STATE_2        = KeyEvent.VK_2;
	private static final int KEY_STATE_3        = KeyEvent.VK_3;
	private static final int KEY_STATE_4        = KeyEvent.VK_4;
	private static final int KEY_STATE_5        = KeyEvent.VK_5;
	private static final int KEY_STATE_6        = KeyEvent.VK_6;
	private static final int KEY_STATE_7        = KeyEvent.VK_7;
	private static final int KEY_STATE_8        = KeyEvent.VK_8;
	private static final int KEY_STATE_9        = KeyEvent.VK_9;
	private static final int KEY_STATE_10       = KeyEvent.VK_0;
	private static final int KEY_STATE_11       = KeyEvent.VK_MINUS;
	private static final int KEY_STATE_12       = KeyEvent.VK_EQUALS;
	
	private static final int KEY_LOCAL_P1_MODE     = KeyEvent.VK_J;
	private static final int KEY_LOCAL_PADDLE_MODE = KeyEvent.VK_L;
	
	private static final Control[] playerDigitalControls = new Control[] {
		Control.JOY0_UP, Control.JOY0_DOWN, Control.JOY0_LEFT, Control.JOY0_RIGHT, Control.JOY0_BUTTON,
		Control.JOY1_UP, Control.JOY1_DOWN, Control.JOY1_LEFT, Control.JOY1_RIGHT, Control.JOY1_BUTTON,
		Control.PADDLE0_BUTTON, Control.PADDLE1_BUTTON
	};

	
	private class PaddlesPositionUpdater extends Thread {
		@Override
		public void run() {
			while(paddleMode) {
				paddlesUpdatePosition();
				try { sleep(1000 / 60);	} catch (InterruptedException e) {}   // Runs @ ~60Hz
			}
		}
	}

}
