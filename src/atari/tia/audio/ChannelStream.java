// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.tia.audio;

public final class ChannelStream {

	public float nextSample() {				// Range 0 - 1
		if (--dividerCountdown <= 0) {
			dividerCountdown = divider;
			currentSample = nextSampleForControl();
		}
		return currentSample == 1 ? volume : 0;
	}
	
	public void setVolume(int newVolume) {
		volume = (float)newVolume / MAX_VOLUME;
	}

	public void setDivider(int newDivider) {
		if (divider == newDivider) return;
		dividerCountdown = (int)(((float)dividerCountdown / divider) * newDivider);
		divider = newDivider;
	}

	public void setControl(int control) {
		if (this.control == control) return;
		this.control = control;
	}
	
	private int nextSampleForControl() {
		if (control == 0x00 || control == 0x0b) 
			return 1;																// Silence  ("set to 1" per specification)
		if (control == 0x01) 
			return nextPoly4();														// 4 bit poly
		if (control == 0x02) 
			return currentTone31() != nextTone31() ? nextPoly4() : currentPoly4();	// div 15 > 4 bit poly
		if (control == 0x03)
			return nextPoly5() == 1 ? nextPoly4(): currentPoly4();					// 5 bit poly > 4 bit poly
		if (control == 0x04 || control == 0x05) 
			return nextTone2();														// div 2 pure tone
		if (control == 0x06 || control == 0x0a)
			return nextTone31();													// div 31 pure tone (18 high, 13, low)
		if (control == 0x07 || control == 0x09)
			return nextPoly5();														// 5 bit poly
		if (control == 0x08)
			return nextPoly9();														// 9 bit poly
		if (control == 0x0c || control == 0x0d)
			return nextTone6();														// div 6 pure tone (3 high, 3 low)
		if (control == 0x0e)
			return currentTone31() != nextTone31() ? nextTone6() : currentTone6();	// div 93 pure tone	(31 tone each 3)
		if (control == 0x0f)
			return nextPoly5() == 1 ? nextTone6() : currentTone6();					// 5 bit poly div 6 (poly 5 each 3)

		throw new IllegalStateException("Invalid TIA Audio Channel Control: " + control);
	}

	private int currentPoly4() {
		return poly4 & 0x01;
	}

	private int nextPoly4() {
		final int carry = poly4 & 0x01;					// bit 0
		final int push = ((poly4 >> 1) ^ carry) & 0x01;	// bit 1 XOR bit 0
		poly4 = poly4 >>> 1;							// shift right
		if (push == 0)									// set bit 3 = push
			poly4 &= 0x07;
		else
			poly4 |= 0x08;
		return carry;
	}

	private int nextPoly5() {
		final int carry = poly5 & 0x01;					// bit 0
		final int push = ((poly5 >> 2) ^ carry) & 0x01;	// bit 2 XOR bit 0
		poly5 = poly5 >>> 1;							// shift right
		if (push == 0)									// set bit 4 = push
			poly5 &= 0x0f;
		else
			poly5 |= 0x10;
		return carry;
	}

	private int nextPoly9() {
		final int carry = poly9 & 0x01;					// bit 0
		final int push = ((poly9 >> 4) ^ carry) & 0x01;	// bit 4 XOR bit 0
		poly9 = poly9 >>> 1;							// shift right
		if (push == 0)									// set bit 8 = push
			poly9 &= 0x0ff;
		else
			poly9 |= 0x100;
		return carry;
	}

	private int nextTone2() {
		return tone2 = tone2 == 0 ? 1 : 0;
	}

	private int currentTone6() {
		return tone6;
	}

	private int nextTone6() {
		if (--tone6Countdown == 0) {
			tone6Countdown = 3;
			tone6 = tone6 == 0 ? 1 : 0;
		}
		return tone6;
	}

	private int currentTone31() {
		return TONE31_STREAM[tone31Count];
	}

	private int nextTone31() {
		if (++tone31Count == 31)
			tone31Count = 0;
		return TONE31_STREAM[tone31Count];
	}

	private float volume = 0;					// 0 - 1
	private int control = 0;					// 0-f
	private int divider = 1;					// Changes to dividers will only be reflected at the next countdown cycle
	private int dividerCountdown = 1;
	
	private int currentSample = 0;
	
	private int poly4 = 0x0f;
	private int poly5 = 0x1f;
	private int poly9 = 0x1ff;
	
	private int tone2 = 1;
	
	private int tone6 = 1;
	private int tone6Countdown = 3;

	private int tone31Count = 30;
	private static final int[] TONE31_STREAM = new int[] { 
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
	};
	
	private static final int   MAX_VOLUME = 15;

}
