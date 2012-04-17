// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.tia.audio;

public final class ChannelStream {

	public float nextSample() {
		if (--dividerCountdown == 0) {
			dividerCountdown = divider;
			currentSample = (nextSampleForControl() * 2 - 1) * volume;
		}
		return currentSample;
	}
	
	public void setVolume(int volume) {
		this.volume = (float)Math.pow(((float)volume / MAX_VOLUME), NON_LINEAR_CONVERSION);
	}

	public void setDivider(int divider) {
		this.divider = divider;
		if (dividerCountdown > divider)
			dividerCountdown = divider;
	}

	public void setControl(int control) {
		this.control = control;
	}
	
	private int nextSampleForControl() {
		switch (control) {
			case 0x00:						// Silence  ("set to 1" per specification)
			case 0x0b:						// Set last 4 bits to 1	(same as silence)
				return 0;	
			case 0x01:						// 4 bit poly
				return nextPoly4();
			case 0x02:						// div 15 > 4 bit poly
				return currentTone31() != nextTone31()
						? nextPoly4()
						: currentPoly4();
			case 0x03:						// 5 bit poly > 4 bit poly
				return nextPoly5() == 1
						? nextPoly4()
						: currentPoly4();
			case 0x04:						// div 2 pure tone
			case 0x05:						// div 2 pure tone
				return nextTone2();
			case 0x06:						// div 31 pure tone (18 high, 13, low)
			case 0x0a:						// div 31 pure tone (18 high, 13, low)
				return nextTone31();
			case 0x07:						// 5 bit poly > div 2 (same as 5 bit poly) 	
			case 0x09:						// 5 bit poly
				return nextPoly5();
			case 0x08:						// 9 bit poly
				return nextPoly9();
			case 0x0c:						// div 6 pure tone (3 high, 3 low)
			case 0x0d:						// div 6 pure tone (3 high, 3 low)
				return nextTone6();
			case 0x0e:						// div 93 pure tone	(31 tone each 3)
				return currentTone6() != nextTone6()
						? nextTone31()
						: currentTone31();
			case 0x0f:						// 5 bit poly div 6 (poly 5 each 3)		
				return currentTone6() != nextTone6()
						? nextPoly5()
						: currentPoly5();
			default:	
				throw new IllegalStateException("Invalid TIA Audio Channel Control: " + control);
		}
	}

	private int currentPoly4() {
		return poly4 & 0x01;
	}

	private int nextPoly4() {
		int carry = poly4 & 0x01;					// bit 0
		int push = ((poly4 >> 1) ^ carry) & 0x01;	// bit 1 XOR bit 0
		poly4 = poly4 >>> 1;						// shift right
		if (push == 0)								// set bit 3 = push
			poly4 &= 0x07;
		else
			poly4 |= 0x08;
		return carry;
	}

	private int currentPoly5() {
		return poly5 & 0x01;
	}

	private int nextPoly5() {
		int carry = poly5 & 0x01;					// bit 0
		int push = ((poly5 >> 2) ^ carry) & 0x01;	// bit 2 XOR bit 0
		poly5 = poly5 >>> 1;						// shift right
		if (push == 0)								// set bit 4 = push
			poly5 &= 0x0f;
		else
			poly5 |= 0x10;
		return carry;
	}

	private int nextPoly9() {
		int carry = poly9 & 0x01;					// bit 0
		int push = ((poly9 >> 4) ^ carry) & 0x01;	// bit 4 XOR bit 0
		poly9 = poly9 >>> 1;						// shift right
		if (push == 0)								// set bit 8 = push
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
		return tone31Stream[tone31Count];
	}

	private int nextTone31() {
		if (++tone31Count == 31)
			tone31Count = 0;
		return tone31Stream[tone31Count];
	}

	private float volume = 0;					// 0 - 1
	private int control = 0;					// 0-f
	private int divider = 1;					// Changes to dividers will only be reflected at the next countdown cycle
	private int dividerCountdown = 1;
	
	private float currentSample = 0;
	
	private int poly4 = 0x0f;
	private int poly5 = 0x1f;
	private int poly9 = 0x1ff;
	
	private int tone2 = 1;
	
	private int tone6 = 1;
	private int tone6Countdown = 3;

	private int tone31Count = 30;
	private static final int[] tone31Stream = new int[] { 
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
	};
	
	private static final int   MAX_VOLUME = 15;
	private static final float NON_LINEAR_CONVERSION = 1.15f;

}
