// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.screen;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;


public class ScreenControlsAdapter implements KeyListener {
	
	public ScreenControlsAdapter(Screen screen, Component... components) {
		this.screen = screen;
		init(components);
	}

	private void init(Component... components) {
		for (int i = 0; i < components.length; i++)
			components[i].addKeyListener(this);
		keyCodeMap.put(KEY_EXIT,         Screen.Control.EXIT);
		keyCodeMap.put(KEY_CART_FILE,    Screen.Control.LOAD_CARTRIDGE_FILE);
		keyCodeMap.put(KEY_CART_URL,     Screen.Control.LOAD_CARTRIDGE_URL);
		keyCodeMap.put(KEY_CART_EMPTY,   Screen.Control.LOAD_CARTRIDGE_EMPTY);

		keyAltCodeMap.put(KEY_HELP,      Screen.Control.HELP);
		keyAltCodeMap.put(KEY_FULL_SCR,  Screen.Control.FULL_SCREEN);
		keyAltCodeMap.put(KEY_QUALITY,   Screen.Control.QUALITY);
		keyAltCodeMap.put(KEY_DEBUG,     Screen.Control.DEBUG);
		keyAltCodeMap.put(KEY_CRT_MODES, Screen.Control.CRT_MODES);
		keyAltCodeMap.put(KEY_CART_FILE, Screen.Control.LOAD_CARTRIDGE_FILE_NO_AUTO_POWER);
		keyAltCodeMap.put(KEY_CART_URL,  Screen.Control.LOAD_CARTRIDGE_URL_NO_AUTO_POWER);

		keyShiftCodeMap.put(KEY_UP,    Screen.Control.SIZE_MINUS); 
		keyShiftCodeMap.put(KEY_DOWN,  Screen.Control.SIZE_PLUS); 
		keyShiftCodeMap.put(KEY_LEFT,  Screen.Control.SIZE_MINUS); 
		keyShiftCodeMap.put(KEY_RIGHT, Screen.Control.SIZE_PLUS);
		
		keyShiftAltCodeMap.put(KEY_UP,    Screen.Control.SCALE_Y_MINUS); 
		keyShiftAltCodeMap.put(KEY_DOWN,  Screen.Control.SCALE_Y_PLUS); 
		keyShiftAltCodeMap.put(KEY_LEFT,  Screen.Control.SCALE_X_MINUS); 
		keyShiftAltCodeMap.put(KEY_RIGHT, Screen.Control.SCALE_X_PLUS); 

		keyControlAltCodeMap.put(KEY_UP,    Screen.Control.ORIGIN_Y_MINUS); 
		keyControlAltCodeMap.put(KEY_DOWN,  Screen.Control.ORIGIN_Y_PLUS); 
		keyControlAltCodeMap.put(KEY_LEFT,  Screen.Control.ORIGIN_X_MINUS); 
		keyControlAltCodeMap.put(KEY_RIGHT, Screen.Control.ORIGIN_X_PLUS); 

		keyShiftControlCodeMap.put(KEY_UP,    Screen.Control.HEIGHT_MINUS); 
		keyShiftControlCodeMap.put(KEY_DOWN,  Screen.Control.HEIGHT_PLUS); 
		keyShiftControlCodeMap.put(KEY_LEFT,  Screen.Control.WIDTH_MINUS); 
		keyShiftControlCodeMap.put(KEY_RIGHT, Screen.Control.WIDTH_PLUS);

		keyShiftCodeMap.put(KEY_CART_PASTE_INS, Screen.Control.LOAD_CARTRIDGE_PASTE);
		keyControlCodeMap.put(KEY_CART_PASTE_V, Screen.Control.LOAD_CARTRIDGE_PASTE);

		keyCodeMap.put(KEY_SIZE_DEFAULT, Screen.Control.SIZE_DEFAULT); 
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// Do nothing
	}

	@Override
	public void keyPressed(KeyEvent e) {
		Screen.Control control = controlForEvent(e);
		if (control == null) return;
		screen.controlStateChanged(control, true);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		Screen.Control control = controlForEvent(e);
		if (control == null) return;
		screen.controlStateChanged(control, false);
	}

	private Screen.Control controlForEvent(KeyEvent e) {
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


	private Screen screen; 
	private final Map<Integer, Screen.Control> keyCodeMap = new HashMap<Integer, Screen.Control>();
	private final Map<Integer, Screen.Control> keyShiftCodeMap = new HashMap<Integer, Screen.Control>();
	private final Map<Integer, Screen.Control> keyAltCodeMap = new HashMap<Integer, Screen.Control>();
	private final Map<Integer, Screen.Control> keyShiftControlCodeMap = new HashMap<Integer, Screen.Control>();
	private final Map<Integer, Screen.Control> keyShiftAltCodeMap = new HashMap<Integer, Screen.Control>();
	private final Map<Integer, Screen.Control> keyControlCodeMap = new HashMap<Integer, Screen.Control>();
	private final Map<Integer, Screen.Control> keyControlAltCodeMap = new HashMap<Integer, Screen.Control>();

	public static final int KEY_UP    = KeyEvent.VK_UP;     
	public static final int KEY_DOWN  = KeyEvent.VK_DOWN;   
	public static final int KEY_LEFT  = KeyEvent.VK_LEFT;   
	public static final int KEY_RIGHT = KeyEvent.VK_RIGHT;  

	public static final int KEY_SIZE_DEFAULT = KeyEvent.VK_BACK_SPACE;  
	
	public static final int KEY_EXIT           = KeyEvent.VK_ESCAPE;
	public static final int KEY_CART_FILE      = KeyEvent.VK_F5;
	public static final int KEY_CART_URL       = KeyEvent.VK_F6;
	public static final int KEY_CART_PASTE_V   = KeyEvent.VK_V;
	public static final int KEY_CART_PASTE_INS = KeyEvent.VK_INSERT;
	public static final int KEY_CART_EMPTY     = KeyEvent.VK_F7;
	public static final int KEY_FULL_SCR       = KeyEvent.VK_ENTER;
	public static final int KEY_QUALITY        = KeyEvent.VK_Q;
	public static final int KEY_CRT_MODES      = KeyEvent.VK_R;
	public static final int KEY_VIDEO_STAND    = KeyEvent.VK_V;

	public static final int KEY_DEBUG = KeyEvent.VK_D;  
	public static final int KEY_HELP  = KeyEvent.VK_H;  

}
