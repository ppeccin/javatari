// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.screen;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatari.pc.screen.Monitor.Control;


public final class MonitorControls implements KeyListener {
	
	public MonitorControls(Monitor monitor) {
		this.monitor = monitor;
		init();
	}

	public void addInputComponents(List<Component> inputs) {
		for (Component component : inputs)
			component.addKeyListener(this);
	}
	
	private void init() {
		keyCodeMap.put(KEY_CART_FILE,    Control.LOAD_CARTRIDGE_FILE);
		keyCodeMap.put(KEY_CART_URL,     Control.LOAD_CARTRIDGE_URL);
		keyCodeMap.put(KEY_CART_EMPTY,   Control.LOAD_CARTRIDGE_EMPTY);

		keyAltCodeMap.put(KEY_QUALITY,   Control.QUALITY);
		keyAltCodeMap.put(KEY_DEBUG,     Control.DEBUG);
		keyAltCodeMap.put(KEY_STATS,     Control.STATS);
		keyAltCodeMap.put(KEY_CRT_MODES, Control.CRT_MODES);
		keyAltCodeMap.put(KEY_CART_FILE, Control.LOAD_CARTRIDGE_FILE_NO_AUTO_POWER);
		keyAltCodeMap.put(KEY_CART_URL,  Control.LOAD_CARTRIDGE_URL_NO_AUTO_POWER);

		keyShiftCodeMap.put(KEY_UP,    Control.SIZE_MINUS); 
		keyShiftCodeMap.put(KEY_DOWN,  Control.SIZE_PLUS); 
		keyShiftCodeMap.put(KEY_LEFT,  Control.SIZE_MINUS); 
		keyShiftCodeMap.put(KEY_RIGHT, Control.SIZE_PLUS);
		
		keyShiftAltCodeMap.put(KEY_UP,    Control.SCALE_Y_MINUS); 
		keyShiftAltCodeMap.put(KEY_DOWN,  Control.SCALE_Y_PLUS); 
		keyShiftAltCodeMap.put(KEY_LEFT,  Control.SCALE_X_MINUS); 
		keyShiftAltCodeMap.put(KEY_RIGHT, Control.SCALE_X_PLUS); 

		keyControlAltCodeMap.put(KEY_UP,    Control.ORIGIN_Y_MINUS); 
		keyControlAltCodeMap.put(KEY_DOWN,  Control.ORIGIN_Y_PLUS); 
		keyControlAltCodeMap.put(KEY_LEFT,  Control.ORIGIN_X_MINUS); 
		keyControlAltCodeMap.put(KEY_RIGHT, Control.ORIGIN_X_PLUS); 

		keyShiftControlCodeMap.put(KEY_UP,    Control.HEIGHT_MINUS); 
		keyShiftControlCodeMap.put(KEY_DOWN,  Control.HEIGHT_PLUS); 
		keyShiftControlCodeMap.put(KEY_LEFT,  Control.WIDTH_MINUS); 
		keyShiftControlCodeMap.put(KEY_RIGHT, Control.WIDTH_PLUS);

		keyShiftCodeMap.put(KEY_CART_PASTE_INS, Control.LOAD_CARTRIDGE_PASTE);
		keyControlCodeMap.put(KEY_CART_PASTE_V, Control.LOAD_CARTRIDGE_PASTE);

		keyCodeMap.put(KEY_SIZE_DEFAULT, Control.SIZE_DEFAULT); 
	}

	@Override
	public void keyPressed(KeyEvent e) {
		Control control = controlForEvent(e);
		if (control == null) return;
		monitor.controlActivated(control);
	}


	@Override
	public void keyTyped(KeyEvent e) {
		// Do nothing
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// Do nothing
	}

	private Control controlForEvent(KeyEvent e) {
		switch (e.getModifiersEx()) {
			case 0:
				return keyCodeMap.get(e.getKeyCode());
			case KeyEvent.ALT_DOWN_MASK:
				return keyAltCodeMap.get(e.getKeyCode());
			case KeyEvent.SHIFT_DOWN_MASK:
				return keyShiftCodeMap.get(e.getKeyCode());
			case KeyEvent.CTRL_DOWN_MASK:
				return keyControlCodeMap.get(e.getKeyCode());
			case KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK:
				return keyControlAltCodeMap.get(e.getKeyCode());
			case KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK:
				return keyShiftControlCodeMap.get(e.getKeyCode());
			case KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK:
				return keyShiftAltCodeMap.get(e.getKeyCode());
		}
		return null;
	}


	private Monitor monitor; 
	
	private final Map<Integer, Control> keyCodeMap = new HashMap<Integer, Control>();
	private final Map<Integer, Control> keyShiftCodeMap = new HashMap<Integer, Control>();
	private final Map<Integer, Control> keyAltCodeMap = new HashMap<Integer, Control>();
	private final Map<Integer, Control> keyShiftControlCodeMap = new HashMap<Integer, Control>();
	private final Map<Integer, Control> keyShiftAltCodeMap = new HashMap<Integer, Control>();
	private final Map<Integer, Control> keyControlCodeMap = new HashMap<Integer, Control>();
	private final Map<Integer, Control> keyControlAltCodeMap = new HashMap<Integer, Control>();

	public static final int KEY_UP    = KeyEvent.VK_UP;     
	public static final int KEY_DOWN  = KeyEvent.VK_DOWN;   
	public static final int KEY_LEFT  = KeyEvent.VK_LEFT;   
	public static final int KEY_RIGHT = KeyEvent.VK_RIGHT;  

	public static final int KEY_SIZE_DEFAULT = KeyEvent.VK_BACK_SPACE;  
	
	public static final int KEY_CART_FILE      = KeyEvent.VK_F5;
	public static final int KEY_CART_URL       = KeyEvent.VK_F6;
	public static final int KEY_CART_PASTE_V   = KeyEvent.VK_V;
	public static final int KEY_CART_PASTE_INS = KeyEvent.VK_INSERT;
	public static final int KEY_CART_EMPTY     = KeyEvent.VK_F7;

	public static final int KEY_QUALITY        = KeyEvent.VK_Q;
	public static final int KEY_CRT_MODES      = KeyEvent.VK_R;
	public static final int KEY_VIDEO_STAND    = KeyEvent.VK_V;
	
	public static final int KEY_DEBUG = KeyEvent.VK_D;  
	public static final int KEY_STATS = KeyEvent.VK_G;  

}
