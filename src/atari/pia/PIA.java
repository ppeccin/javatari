// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.pia;

import general.board.BUS16Bits;
import general.board.ClockDriven;

import java.io.Serializable;
import java.util.Map;

import utils.Randomizer;
import atari.board.BUS;
import atari.controls.ConsoleControls;
import atari.controls.ConsoleControls.Control;
import atari.controls.ConsoleControlsInput;

public final class PIA implements BUS16Bits, ClockDriven, ConsoleControlsInput {

	public PIA() {
	}

	public void connectBus(BUS bus) {
		this.bus = bus;
	}

	public void powerOn() {
		// Nothing
	}

	public void powerOff() {
		// Nothing
	}

	@Override
	public void clockPulse() {
		if (--timerCount <= 0)
			decrementTimer();
	}
		
	private void setTimerInterval(int value, int interval) {
		INTIM = value & 0xff;	
		INSTAT &= 0x3f;				// Reset bit 7 and 6 (Overflow since last INTIM read and since last TIMxx write)
		timerCount = currentTimerInterval = lastSetTimerInterval = interval;
		decrementTimer();			// Timer immediately decrements after setting per specification
	}

	private void decrementTimer() {
		// Also check for overflow
		if (--INTIM < 0) {
			INSTAT |= 0xc0;								// Set bit 7 and 6 (Overflow since last INTIM read and since last TIMxx write)
			INTIM = 0xff;								// Wrap timer
			timerCount = currentTimerInterval = 1;		// If timer underflows, return to 1 cycle interval per specification
		} else
			timerCount = currentTimerInterval;
	}

	private void readFromINTIM() {
		INSTAT &= 0xbf;									// Resets bit 6 (Overflow since last INTIM read)
		// If fastDecrement was active (currentTimerInterval == 1), interval always returns to set value after read per specification
		if (currentTimerInterval == 1 )	
			timerCount = currentTimerInterval = lastSetTimerInterval;
	}

	private void swchbWrite(int val) {
		// Only bits 2, 4 and 5 can be written
		SWCHB = (SWCHB & 0xcb) | (val & 34); 
	}
	
	private void debugInfo(String str) {
		if (debug)
			System.out.println(str);
	}
	
	@Override
	public byte readByte(int address) {
		final int reg = address & READ_ADDRESS_MASK;

		if (reg == 0x00) return (byte) SWCHA;
		if (reg == 0x01) return (byte) SWACNT;
		if (reg == 0x02) return (byte) SWCHB;
		if (reg == 0x03) return (byte) SWBCNT;
		if (reg == 0x04 || reg == 0x06) { readFromINTIM(); return (byte) INTIM; }								
		if (reg == 0x05 || reg == 0x07) return (byte) INSTAT;						// Undocumented
		
		// debugInfo(String.format("Invalid PIA read register address: %04x", address)); 
		return 0;
	}

	@Override
	public void writeByte(int address, byte b) {
		int i = b & 0xff;
		int reg = address & WRITE_ADDRESS_MASK;

		if (reg == 0x00) { /* SWCHA  = i; */ debugInfo(String.format(">>>> Unsupported Write to PIA SWCHA: %02x", i)); return; }	// Output to controllers not supported
		if (reg == 0x01) { /* SWACNT = i; */ debugInfo(String.format(">>>> Unsupported Write to PIA SWACNT: %02x", i)); return; }	// SWACNT configuration not supported
		if (reg == 0x02) { swchbWrite(i); return; }																	
		if (reg == 0x03) { SWBCNT = i; debugInfo(String.format(">>>> Ineffective Write to PIA SWBCNT: %02x", i)); return; }
		if (reg == 0x04) { TIM1T  = i; setTimerInterval(i, 1); return; }
		if (reg == 0x05) { TIM8T  = i; setTimerInterval(i, 8); return; }
		if (reg == 0x06) { TIM64T = i; setTimerInterval(i, 64); return; }
		if (reg == 0x07) { T1024T = i; setTimerInterval(i, 1024); return; }
		
		// debugInfo(String.format("Invalid PIA write register address: %04x value %d", address, b));
	}

	@Override
	public void controlStateChanged(ConsoleControls.Control control, boolean state) {
		switch (control) {
			case JOY0_UP:        if (state) SWCHA &= 0xef; else SWCHA |= 0x10; return;	//  0 = Pressed
			case JOY0_DOWN:      if (state) SWCHA &= 0xdf; else SWCHA |= 0x20; return;
			case PADDLE1_BUTTON:
			case JOY0_LEFT:      if (state) SWCHA &= 0xbf; else SWCHA |= 0x40; return;
			case PADDLE0_BUTTON: 
			case JOY0_RIGHT:     if (state) SWCHA &= 0x7f; else SWCHA |= 0x80; return;
			case JOY1_UP:        if (state) SWCHA &= 0xfe; else SWCHA |= 0x01; return;
			case JOY1_DOWN:      if (state) SWCHA &= 0xfd; else SWCHA |= 0x02; return;
			case JOY1_LEFT:      if (state) SWCHA &= 0xfb; else SWCHA |= 0x04; return;
			case JOY1_RIGHT:     if (state) SWCHA &= 0xf7; else SWCHA |= 0x08; return;
			case RESET:          if (state) SWCHB &= 0xfe; else SWCHB |= 0x01; return;
			case SELECT:         if (state) SWCHB &= 0xfd; else SWCHB |= 0x02; return;
		}
		// Toggles
		if (!state) return;
		switch (control) {
			case BLACK_WHITE: if ((SWCHB & 0x08) == 0) SWCHB |= 0x08; else SWCHB &= 0xf7;		//	0 = B/W, 1 = Color 
				bus.tia.videoOutput().showOSD((SWCHB & 0x08) != 0 ? "COLOR" : "B/W", true); return;
			case DIFFICULTY0: if ((SWCHB & 0x40) == 0) SWCHB |= 0x40; else SWCHB &= 0xbf; 		//  0 = Beginner, 1 = Advanced
				bus.tia.videoOutput().showOSD((SWCHB & 0x40) != 0 ? "P1 Expert" : "P1 Novice", true); return;
			case DIFFICULTY1: if ((SWCHB & 0x80) == 0) SWCHB |= 0x80; else SWCHB &= 0x7f;		//  0 = Beginner, 1 = Advanced
				bus.tia.videoOutput().showOSD((SWCHB & 0x80) != 0 ? "P2 Expert" : "P2 Novice", true); return;
		}
	}

	@Override
	public void controlStateChanged(ConsoleControls.Control control, int position) {
		// No positional controls here
	}

	@Override
	public void controlsStateReport(Map<ConsoleControls.Control, Boolean> report) {
		//  Only Panel Controls are visible from outside
		report.put(Control.BLACK_WHITE, (SWCHB & 0x08) == 0);
		report.put(Control.DIFFICULTY0, (SWCHB & 0x40) != 0);
		report.put(Control.DIFFICULTY1, (SWCHB & 0x80) != 0);
		report.put(Control.SELECT, (SWCHB & 0x02) == 0);
		report.put(Control.RESET, (SWCHB & 0x01) == 0);
	}
	
	public PIAState saveState() {
		PIAState state = new PIAState();
		state.debug                = debug;
		state.timerCount           = timerCount;
		state.currentTimerInterval = currentTimerInterval;
		state.lastSetTimerInterval = lastSetTimerInterval;
		state.SWCHA                = SWCHA;        
		state.SWACNT               = SWACNT;
		state.SWCHB                = SWCHB;
		state.SWBCNT               = SWBCNT;
		state.INTIM                = INTIM;
		state.INSTAT               = INSTAT;
		state.TIM1T                = TIM1T;        
		state.TIM8T                = TIM8T;        
		state.TIM64T               = TIM64T;
		state.T1024T               = T1024T;
		return state;
	}
	
	public void loadState(PIAState state) {
		// debug			 = state.debug;			// Keeps the current debug modes
		timerCount           = state.timerCount;
		currentTimerInterval = state.currentTimerInterval;
		lastSetTimerInterval = state.lastSetTimerInterval;
		// SWCHA           	 = state.SWCHA;			// Do not load controls state
		SWACNT               = state.SWACNT;
		SWCHB                = state.SWCHB;
		SWBCNT               = state.SWBCNT;
		INTIM                = state.INTIM;
		INSTAT               = state.INSTAT;
		TIM1T                = state.TIM1T;
		TIM8T                = state.TIM8T;
		TIM64T               = state.TIM64T;
		T1024T               = state.T1024T;
	}


	private BUS bus;

	// State Variables ----------------------------------------------

	public boolean debug = false;

	private int timerCount = 1024;				// Start with the largest timer interval
	private int currentTimerInterval = 1024;
	private int lastSetTimerInterval = 1024;
		

	// Registers ----------------------------------------------------
	
	private int SWCHA=							// 11111111  Port A; input or output  (read or write)        
					0xff;						// All directions of both controllers OFF				            
	private int SWACNT;							// 11111111  Port A DDR, 0=input, 1=output                 
	private int SWCHB = 						// 11..1.11  Port B; console switches (should be read only but unused bits can be written and read)
					0x0b;  						// Reset OFF; Select OFF; B/W OFF; Difficult A/B OFF (Amateur)				            
	private int SWBCNT; 						// 11111111  Port B DDR (hard wired as input)                
	private int INTIM =   						// 11111111  Timer output (read only)                        
				 	Randomizer.instance.nextInt() & 0xff;	// Some random value. Games use this at startup to seed random number generation
	private int INSTAT;  						// 11......  Timer Status (read only, undocumented)          
	private int TIM1T;  						// 11111111  set 1 clock interval (838 nsec/interval)        
	private int TIM8T;  						// 11111111  set 8 clock interval (6.7 usec/interval)        
	private int TIM64T; 						// 11111111  set 64 clock interval (53.6 usec/interval)     
	private int T1024T; 						// 11111111  set 1024 clock interval (858.2 usec/interval)

	private static final int READ_ADDRESS_MASK = 0x0007;
	private static final int WRITE_ADDRESS_MASK = 0x0007;
	

	// Used to save/load states
	public static class PIAState implements Serializable {
		boolean debug;
		int timerCount;
		int currentTimerInterval;
		int lastSetTimerInterval;
		int SWCHA;        
		int SWACNT;                 
		int SWCHB;
		int SWBCNT;                
		int INTIM;
		int INSTAT;          
		int TIM1T;        
		int TIM8T;        
		int TIM64T;     
		int T1024T;
		
		public static final long serialVersionUID = 2L;
	}

}




