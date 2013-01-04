// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.room.settings;

import static java.awt.event.KeyEvent.*;

import java.util.HashMap;
import java.util.Map;

public final class KeyNames {

	public static String get(int code) {
		String name = keyNames().get(code);
		return name != null ? name : "     ";
	}

	public static boolean hasName(int code) {
		return keyNames().containsKey(code);
	}

	private static Map<Integer, String> keyNames() {
		if (keyNames != null) return keyNames;
		
		keyNames = new HashMap<Integer, String>();
		
		keyNames.put(VK_ESCAPE, "ESC");
		keyNames.put(VK_F1, "F1");
		keyNames.put(VK_F2, "F2");
		keyNames.put(VK_F3, "F3");
		keyNames.put(VK_F4, "F4");
		keyNames.put(VK_F5, "F5");
		keyNames.put(VK_F6, "F6");
		keyNames.put(VK_F7, "F7");
		keyNames.put(VK_F8, "F8");
		keyNames.put(VK_F9, "F9");
		keyNames.put(VK_F10, "F10");
		keyNames.put(VK_F11, "F11");
		keyNames.put(VK_F12, "F12");
		keyNames.put(VK_PRINTSCREEN, "PSCR");
		keyNames.put(VK_SCROLL_LOCK, "SCLK");
		keyNames.put(VK_PAUSE, "PAUSE");
		
		keyNames.put(VK_QUOTE, "'");
		keyNames.put(VK_1, "1");
		keyNames.put(VK_2, "2");
		keyNames.put(VK_3, "3");
		keyNames.put(VK_4, "4");
		keyNames.put(VK_5, "5");
		keyNames.put(VK_6, "6");
		keyNames.put(VK_7, "7");
		keyNames.put(VK_8, "8");
		keyNames.put(VK_9, "9");
		keyNames.put(VK_0, "0");
		keyNames.put(VK_MINUS, "-");
		keyNames.put(VK_EQUALS, "=");
		keyNames.put(VK_BACK_SPACE, "BKSP");

		keyNames.put(VK_TAB, "TAB");
		keyNames.put(VK_Q, "Q");
		keyNames.put(VK_W, "W");
		keyNames.put(VK_E, "E");
		keyNames.put(VK_R, "R");
		keyNames.put(VK_T, "T");
		keyNames.put(VK_Y, "Y");
		keyNames.put(VK_U, "U");
		keyNames.put(VK_I, "I");
		keyNames.put(VK_O, "O");
		keyNames.put(VK_P, "P");
		keyNames.put(VK_DEAD_ACUTE, "´");
		keyNames.put(VK_OPEN_BRACKET, "[");
		keyNames.put(VK_CLOSE_BRACKET, "]");
	
		keyNames.put(VK_CAPS_LOCK, "CAPS");
		keyNames.put(VK_A, "A");
		keyNames.put(VK_S, "S");
		keyNames.put(VK_D, "D");
		keyNames.put(VK_F, "F");
		keyNames.put(VK_G, "G");
		keyNames.put(VK_H, "H");
		keyNames.put(VK_J, "J");
		keyNames.put(VK_K, "K");
		keyNames.put(VK_L, "L");
		keyNames.put(VK_DEAD_CEDILLA, "Ç");
		keyNames.put(VK_DEAD_TILDE, "~");
		keyNames.put(VK_ENTER, "ENTR");

		keyNames.put(VK_SHIFT, "SHFT");
		keyNames.put(VK_BACK_SLASH, "\\");
		keyNames.put(VK_Z, "Z");
		keyNames.put(VK_X, "X");
		keyNames.put(VK_C, "C");
		keyNames.put(VK_V, "V");
		keyNames.put(VK_B, "B");
		keyNames.put(VK_N, "N");
		keyNames.put(VK_M, "M");
		keyNames.put(VK_COMMA, ",");
		keyNames.put(VK_PERIOD, ".");
		keyNames.put(VK_SEMICOLON, ";");
		keyNames.put(VK_SLASH, "/");
		keyNames.put(VK_CONTROL, "CTRL");
		keyNames.put(VK_ALT, "ALT");
		keyNames.put(VK_WINDOWS, "WIN");
		keyNames.put(VK_SPACE, "SPC");
		keyNames.put(VK_ALT_GRAPH, "ALGR");
		keyNames.put(VK_CONTEXT_MENU, "CNTX");
		
		keyNames.put(VK_INSERT, "INS");
		keyNames.put(VK_DELETE, "DEL");
		keyNames.put(VK_HOME, "HOME");
		keyNames.put(VK_END, "END");
		keyNames.put(VK_PAGE_UP, "PGUP");
		keyNames.put(VK_PAGE_DOWN, "PGDN");
		keyNames.put(VK_UP, "UP");
		keyNames.put(VK_DOWN, "DOWN");
		keyNames.put(VK_LEFT, "LEFT");
		keyNames.put(VK_RIGHT, "RIGHT");
		
		keyNames.put(VK_NUM_LOCK, "NUM");
		keyNames.put(VK_DIVIDE, "NUM/");
		keyNames.put(VK_MULTIPLY, "NUM*");
		keyNames.put(VK_SUBTRACT, "NUM-");
		keyNames.put(VK_ADD, "NUM+");
		keyNames.put(VK_DECIMAL, "NUM.");
		keyNames.put(VK_NUMPAD0, "NUM0");
		keyNames.put(VK_NUMPAD1, "NUM1");
		keyNames.put(VK_NUMPAD2, "NUM2");
		keyNames.put(VK_NUMPAD3, "NUM3");
		keyNames.put(VK_NUMPAD4, "NUM4");
		keyNames.put(VK_NUMPAD5, "NUM5");
		keyNames.put(VK_NUMPAD6, "NUM6");
		keyNames.put(VK_NUMPAD7, "NUM7");
		keyNames.put(VK_NUMPAD8, "NUM8");
		keyNames.put(VK_NUMPAD9, "NUM9");
		keyNames.put(VK_KP_UP, "NUP");
		keyNames.put(VK_KP_DOWN, "NDWN");
		keyNames.put(VK_KP_LEFT, "NLEFT");
		keyNames.put(VK_KP_RIGHT, "NRGHT");
		keyNames.put(VK_CLEAR, "CLR");
		
		return keyNames;
	}
	
	private static Map<Integer, String>keyNames;
	
}
