// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.tia;		

import general.av.video.VideoStandard;
import general.board.BUS16Bits;
import general.board.ClockDriven;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import com.sun.corba.se.impl.orbutil.concurrent.DebugMutex;

import parameters.Parameters;
import utils.Array2DCopy;
import atari.board.BUS;
import atari.controls.ConsoleControls;
import atari.controls.ConsoleControlsInput;
import atari.tia.audio.AudioGenerator;
import atari.tia.audio.AudioMonoGenerator;
import atari.tia.video.NTSCPalette;
import atari.tia.video.PALPalette;
import atari.tia.video.VideoGenerator;

@SuppressWarnings("unused")
public final class TIA implements BUS16Bits, ClockDriven, ConsoleControlsInput {

	public TIA() {
		videoOutput = new VideoGenerator();
		audioOutput = new AudioMonoGenerator();
	}

	public void connectBus(BUS bus) {
		this.bus = bus;
	}

	public VideoGenerator videoOutput() {	// VideoSignal
		return videoOutput;
	}

	public AudioGenerator audioOutput() {	// AudioSignal
		return audioOutput;
	}

	public void videoStandard(VideoStandard standard) {
		videoOutput.standard(standard);
		audioOutput.videoStandard(standard);
		palette = standard.equals(VideoStandard.NTSC) ? NTSCPalette.getPalette() : PALPalette.getPalette();
	}
	
	public double desiredClockForVideoStandard() {
		if (FORCED_CLOCK != 0) return FORCED_CLOCK;
		return videoOutput.standard().fps;
	}
	
	public void powerOn() {
		Arrays.fill(linePixels, HBLANK_COLOR);
		Arrays.fill(debugPixels, 0);
		initLatchesAtPowerOn();
		observableChange();
		powerOn = true;
	}

	public void powerOff() {
		powerOn = false;
		videoOutput.newLine(null, false);		// Let the monitor know that the signal is off
		audioOutput.channel0().setVolume(0);
		audioOutput.channel1().setVolume(0);
	}

	@Override
	// To perform better, TIA is using one clock cycle per frame
	public void clockPulse() {
		if (!powerOn || (debugPause && debugPausedNoMoreFrames())) return;
		boolean videoOutputVSynched = false;	
		do {
			// Releases the CPU at the beginning of the line in case a WSYNC has halted it
			bus.cpu.RDY = true;
			// HBLANK period
			for (clock = 3; clock < HBLANK_DURATION; clock += 3) {		// 3 .. 66
				checkRepeatMode();
				// Send clock/3 pulse to the CPU and PIA each 3 TIA cycles, at the end of the 3rd cycle, 
				bus.clockPulse();
			}
			// 67
			// First Audio Sample. 2 samples per scan line ~ 31440 KHz
			audioOutput.clockPulse();
			// Display period
			int subClock3 = 2;	// To control the clock/3 cycles. First at clock 69
			for (clock = 68; clock < LINE_WIDTH; clock++) {			// 68 .. 227
				checkRepeatMode();
				// Clock delay decodes
				if (vBlankDecode.isActive) vBlankDecode.clockPulse();
				// Send clock/3 pulse to the CPU and PIA each 3 TIA cycles, at the end of the 3rd cycle, 
				if (--subClock3 == 0) {
					bus.clockPulse();
					subClock3 = 3;
				}
				objectsClockCounters();
				if (!repeatLastLine && (clock >= 76 || !hMoveHitBlank)) setPixelValue();
				// else linePixels[clock] |= 0x88800080;	// Add a pink dye to show pixels repeated
			}
			// Send the last clock/3 pulse to the CPU and PIA, at the end of the 227th cycle, perceived by the TIA at clock 0 next line
			clock = 0;
			bus.clockPulse();
			// End of scan line
			// Second Audio Sample. 2 samples per scan line ~ 31440 KHz
			audioOutput.clockPulse();
			// Handle Paddles capacitor charging
			if (paddle0Position >= 0 && !paddleCapacitorsGrounded) chargePaddleCapacitors();	// Only if paddles are connected (position >= 0)
			// Send the finished line to the output
			adjustLineAtEnd();
			videoOutputVSynched = videoOutput.newLine(linePixels, vSyncOn);
		} while (!videoOutputVSynched && powerOn);
		if (powerOn) {
			audioOutput.sendSamplesFrameToMonitor();
			// If needed, synch with audio and video output after each frame
			if (SYNC_WITH_AUDIO_MONITOR) audioOutput.monitor().synchOutput();
			if (SYNC_WITH_VIDEO_MONITOR) videoOutput.monitor().synchOutput();
		}
	}

	private void checkRepeatMode() {
		// If one entire line since last observable change has just completed, enter repeatLastLine mode
		if (clock == lastObservableChangeClock) {
			repeatLastLine = true;
			lastObservableChangeClock = -1;
		}
	}

	private void setPixelValue() {
		// Updates the current PlayFiled pixel to draw only each 4 pixels, or at the first calculated pixel after stopped using cached line
		if ((clock & 0x03) == 0 || clock == lastObservableChangeClock)		// clock & 0x03 is the same as clock % 4
			playfieldUpdateCurrentPixel();
		// No need to calculate all possibilities in vBlank. TODO No collisions will be detected during VBLANK
		if (vBlankOn) {
			linePixels[clock] = vSyncOn ? vSyncColor : vBlankColor;
			return;
		}
		// Pixel color
		int color = 1;		// All valid colors are between 0xffffffff ad 0x00000000, therefore <= 0
		// Flags for Collision latches
		boolean P0 = false, P1 = false, M0 = false, M1 = false, FL = false, BL = false;
		// Get the value for the PlayField and Ball first only if PlayField and Ball have higher priority
		if (playfieldPriority) {
			// Get the value for the Ball
			if (ballScanCounter >= 0 && ballScanCounter <= 7) {
				playersPerformDelayedSpriteChanges();		// May trigger Ball delayed enablement
				if (ballEnabled) {
					BL = true;
					color = ballColor;
				}
			}
			if (playfieldCurrentPixel) { 
				FL = true;
				if (color > 0) color = playfieldColor;	// No Score Mode in priority mode
			}
		}
		// Get the value for Player0
		if (player0ScanCounter >= 0 && player0ScanCounter <= 31) {
			playersPerformDelayedSpriteChanges();
			int sprite = player0VerticalDelay ? player0ActiveSprite : player0DelayedSprite;
			if (sprite != 0)
				if (((sprite >> (player0Reflected ? (7 - (player0ScanCounter >>> 2)) : (player0ScanCounter >>> 2))) & 0x01) != 0) {
					P0 = true;
					if (color > 0) color = player0Color;
				}
		}
		if (missile0ScanCounter >= 0 && missile0Enabled && missile0ScanCounter <= 7 && !missile0ResetToPlayer) {
			M0 = true;
			if (color > 0) color = missile0Color;
		}
		// Get the value for Player1
		if (player1ScanCounter >= 0 && player1ScanCounter <= 31) {
			playersPerformDelayedSpriteChanges();
			int sprite = player1VerticalDelay ? player1ActiveSprite : player1DelayedSprite;
			if (sprite != 0)
				if (((sprite >> (player1Reflected ? (7 - (player1ScanCounter >>> 2)) : (player1ScanCounter >>> 2))) & 0x01) != 0) {
					P1 = true;
					if (color > 0) color = player1Color;
				}
		}
		if (missile1ScanCounter >= 0 && missile1Enabled &&  missile1ScanCounter <= 7 && !missile1ResetToPlayer) {
			M1 = true;
			if (color > 0) color = missile1Color;
		}
		if (!playfieldPriority) {
			// Get the value for the Ball (low priority)
			if (ballScanCounter >= 0 && ballScanCounter <= 7) {
				playersPerformDelayedSpriteChanges();		// May trigger Ball delayed enablement
				if (ballEnabled) {
					BL = true;
					if (color > 0) color = ballColor;
				}
			}
			// Get the value for the the PlayField (low priority)
			if (playfieldCurrentPixel) {
				FL = true;
				if (color > 0) color = !playfieldScoreMode ? playfieldColor : (clock < 148 ? player0Color : player1Color);
			}
		}
		// If nothing more is showing, get the PlayField background value (low priority)
		if (color > 0) color = playfieldBackground;
		// Set the correct pixel color
		linePixels[clock] = color;
		// Finish collision latches
		if (debugNoCollisions) return;
		if (P0 && FL) 
			CXP0FB |= 0x80;
		if (P1) {
			if (FL) CXP1FB |= 0x80;
			if (P0) CXPPMM |= 0x80;
		}
		if (BL) {
			if (FL) CXBLPF |= 0x80;
			if (P0) CXP0FB |= 0x40; 
			if (P1) CXP1FB |= 0x40; 
		}
		if (M0) {
			if (P1) CXM0P  |= 0x80;
			if (P0) CXM0P  |= 0x40; 
			if (FL) CXM0FB |= 0x80;
			if (BL) CXM0FB |= 0x40; 
		}
		if (M1) {
			if (P0) CXM1P  |= 0x80;
			if (P1) CXM1P  |= 0x40; 
			if (FL) CXM1FB |= 0x80;
			if (BL) CXM1FB |= 0x40; 
			if (M0) CXPPMM |= 0x40; 
		}
	}

	private void objectsClockCounters() {
		player0ClockCounter();
		player1ClockCounter();
		missile0ClockCounter();
		missile1ClockCounter();
		ballClockCounter();
	}

	private void player0ClockCounter() {
		if (++player0Counter == 160) player0Counter = 0;
		if (player0ScanCounter >= 0) {
			// If missileResetToPlayer is on and the player scan has started the FIRST copy
			if (missile0ResetToPlayer && player0Counter < 12 && player0ScanCounter >= 28 && player0ScanCounter <= 31)
				missile0Counter = 156;
			player0ScanCounter -= player0ScanSpeed;
		}
		// Start scans 4 clocks before each copy. Scan is between 0 and 31, each pixel = 4 scan clocks
		if (player0Counter == 156) {
			if (player0RecentReset) player0RecentReset = false;
			else player0ScanCounter = 31 + player0ScanSpeed * (player0ScanSpeed == 4 ? 5 : 6);	// If Double or Quadruple size, delays 1 additional pixel 
		}
		else if (player0Counter == 12) {
			if (player0CloseCopy) player0ScanCounter = 31 + player0ScanSpeed * 5;
		}
		else if (player0Counter == 28) {
			if (player0MediumCopy) player0ScanCounter = 31 + player0ScanSpeed * 5;
		}
		else if (player0Counter == 60) {
			if (player0WideCopy) player0ScanCounter = 31 + player0ScanSpeed * 5;
		}
	}

	private void player1ClockCounter() {
		if (++player1Counter == 160) player1Counter = 0;
		if (player1ScanCounter >= 0) {
			// If missileResetToPlayer is on and the player scan has started the FIRST copy
			if (missile1ResetToPlayer && player1Counter < 12 && player1ScanCounter >= 28 && player1ScanCounter <= 31)
				missile1Counter = 156;
			player1ScanCounter -= player1ScanSpeed;
		}
		// Start scans 4 clocks before each copy. Scan is between 0 and 31, each pixel = 4 scan clocks
		if (player1Counter == 156) {
			if (player1RecentReset) player1RecentReset = false;
			else player1ScanCounter = 31 + player1ScanSpeed * (player1ScanSpeed == 4 ? 5 : 6);	// If Double or Quadruple size, delays 1 additional pixel 
		}
		else if (player1Counter == 12) {
			if (player1CloseCopy) player1ScanCounter = 31 + player1ScanSpeed * 5;
		}
		else if (player1Counter == 28) {
			if (player1MediumCopy) player1ScanCounter = 31 + player1ScanSpeed * 5;
		}
		else if (player1Counter == 60) {
			if (player1WideCopy) player1ScanCounter = 31 + player1ScanSpeed * 5;
		}
	}

	private void missile0ClockCounter() {
		if (++missile0Counter == 160) missile0Counter = 0;
		if (missile0ScanCounter >= 0) missile0ScanCounter -= missile0ScanSpeed;
		// Start scans 4 clocks before each copy. Scan is between 0 and 7, each pixel = 8 scan clocks
		if (missile0Counter == 156) {
			if (missile0RecentReset) missile0RecentReset = false;
			else missile0ScanCounter = 7 + missile0ScanSpeed * 4; 
		}
		else if (missile0Counter == 12) {
			if (player0CloseCopy) missile0ScanCounter = 7 + missile0ScanSpeed * 4;
		}
		else if (missile0Counter == 28) {
			if (player0MediumCopy) missile0ScanCounter = 7 + missile0ScanSpeed * 4;
		}
		else if (missile0Counter == 60) {
			if (player0WideCopy) missile0ScanCounter = 7 + missile0ScanSpeed * 4;
		}
	}

	private void missile1ClockCounter() {
		if (++missile1Counter == 160) missile1Counter = 0;
		if (missile1ScanCounter >= 0) missile1ScanCounter -= missile1ScanSpeed;
		// Start scans 4 clocks before each copy. Scan is between 0 and 7, each pixel = 8 scan clocks
		if (missile1Counter == 156) {
			if (missile1RecentReset) missile1RecentReset = false;
			else missile1ScanCounter = 7 + missile1ScanSpeed * 4;
		}
		else if (missile1Counter == 12) {
			if (player1CloseCopy) missile1ScanCounter = 7 + missile1ScanSpeed * 4;
		}
		else if (missile1Counter == 28) {
			if (player1MediumCopy) missile1ScanCounter = 7 + missile1ScanSpeed * 4;
		}
		else if (missile1Counter == 60) {
			if (player1WideCopy) missile1ScanCounter = 7 + missile1ScanSpeed * 4;
		}
	}

	private void ballClockCounter() {
		if (++ballCounter == 160) ballCounter = 0;
		if (ballScanCounter >= 0) ballScanCounter -= ballScanSpeed;
		// The ball does not have copies and does not wait for the next scanline to start even if recently reset
		// Start scans 4 clocks before. Scan is between 0 and 7, each pixel = 8 scan clocks
		if (ballCounter == 156) ballScanCounter = 7 + ballScanSpeed * 4;
	}

	private void playfieldDelaySpriteChange(int part, int sprite) {
		observableChange();
		if (debug) debugPixel(DEBUG_PF_SET_COLOR);
		playfieldPerformDelayedSpriteChange(true);
		playfieldDelayedChangeClock = clock;
		playfieldDelayedChangePart = part;
		playfieldDelayedChangePattern = sprite;
	}

	private void playfieldPerformDelayedSpriteChange(boolean force) {
		// Only commits change if there is one and the delay has passed
		if (playfieldDelayedChangePart == -1) return;
		if (!force) {
			int dif = clock - playfieldDelayedChangeClock;
			if (dif == 0 || dif == 1) return;
		}
		if 		(playfieldDelayedChangePart == 0) PF0 = playfieldDelayedChangePattern;
		else if	(playfieldDelayedChangePart == 1) PF1 = playfieldDelayedChangePattern;
		else if (playfieldDelayedChangePart == 2) PF2 = playfieldDelayedChangePattern;
		playfieldPatternInvalid = true;
		playfieldDelayedChangePart = -1;		// Marks the delayed change as nothing
	}

	private void playfieldUpdateCurrentPixel() {
		playfieldPerformDelayedSpriteChange(false);
		if (playfieldPatternInvalid) {
			playfieldPatternInvalid = false;
			// Shortcut if the Playfield is all clear
			if (PF0 == 0 && PF1 == 0 && PF2 == 0) {
				Arrays.fill(playfieldPattern, false);
				playfieldCurrentPixel = false;
				return;
			}
			int s, i;
			if (playfieldReflected) {
				s = 40; i = -1;
			} else {
				s = 19; i = 1;
			}
			playfieldPattern[0]  = playfieldPattern[s+=i] = (PF0 & 0x10) != 0;
			playfieldPattern[1]  = playfieldPattern[s+=i] = (PF0 & 0x20) != 0;
			playfieldPattern[2]  = playfieldPattern[s+=i] = (PF0 & 0x40) != 0;
			playfieldPattern[3]  = playfieldPattern[s+=i] = (PF0 & 0x80) != 0;
			playfieldPattern[4]  = playfieldPattern[s+=i] = (PF1 & 0x80) != 0;
			playfieldPattern[5]  = playfieldPattern[s+=i] = (PF1 & 0x40) != 0;
			playfieldPattern[6]  = playfieldPattern[s+=i] = (PF1 & 0x20) != 0;
			playfieldPattern[7]  = playfieldPattern[s+=i] = (PF1 & 0x10) != 0;
			playfieldPattern[8]  = playfieldPattern[s+=i] = (PF1 & 0x08) != 0;
			playfieldPattern[9]  = playfieldPattern[s+=i] = (PF1 & 0x04) != 0;
			playfieldPattern[10] = playfieldPattern[s+=i] = (PF1 & 0x02) != 0;
			playfieldPattern[11] = playfieldPattern[s+=i] = (PF1 & 0x01) != 0;
			playfieldPattern[12] = playfieldPattern[s+=i] = (PF2 & 0x01) != 0;
			playfieldPattern[13] = playfieldPattern[s+=i] = (PF2 & 0x02) != 0;
			playfieldPattern[14] = playfieldPattern[s+=i] = (PF2 & 0x04) != 0;
			playfieldPattern[15] = playfieldPattern[s+=i] = (PF2 & 0x08) != 0;
			playfieldPattern[16] = playfieldPattern[s+=i] = (PF2 & 0x10) != 0;
			playfieldPattern[17] = playfieldPattern[s+=i] = (PF2 & 0x20) != 0;
			playfieldPattern[18] = playfieldPattern[s+=i] = (PF2 & 0x40) != 0;
			playfieldPattern[19] = playfieldPattern[s+=i] = (PF2 & 0x80) != 0;
		}
		playfieldCurrentPixel = playfieldPattern[((clock - HBLANK_DURATION) >>> 2)];
	}

	private void playerDelaySpriteChange(int player, int sprite) {
		observableChange();
		if (debug) debugPixel(player == 0 ? DEBUG_P0_GR_COLOR : DEBUG_P1_GR_COLOR);
		if (playersDelayedSpriteChangesCount >= PLAYERS_DELAYED_SPRITE_GHANGES_MAX_COUNT) { 
			debugInfo(">>> Max player delayed changes reached: " + PLAYERS_DELAYED_SPRITE_GHANGES_MAX_COUNT); 
			return;
		}	
		playersDelayedSpriteChanges[playersDelayedSpriteChangesCount][0] = clock;
		playersDelayedSpriteChanges[playersDelayedSpriteChangesCount][1] = player;
		playersDelayedSpriteChanges[playersDelayedSpriteChangesCount][2] = sprite;
		playersDelayedSpriteChangesCount++;
	}

	private void playersPerformDelayedSpriteChanges() {
		if (playersDelayedSpriteChangesCount == 0 || playersDelayedSpriteChanges[0][0] == clock) return;
		for (int i = 0; i < playersDelayedSpriteChangesCount; i++) {
			int[] change = playersDelayedSpriteChanges[i];
			if (change[1] == 0) { 
				player0DelayedSprite = change[2];
				player1ActiveSprite = player1DelayedSprite; 
			} else { 
				player1DelayedSprite = change[2];
				player0ActiveSprite = player0DelayedSprite;
				ballEnabled = ballDelayedEnablement; 
			}
		}
		playersDelayedSpriteChangesCount = 0;
	}

	private void ballSetGraphic(int value) {
		observableChange();
		ballDelayedEnablement = (value & 0x02) != 0;
		if (!ballVerticalDelay) ballEnabled = ballDelayedEnablement;
	}

	private void player0SetShape(int shape) {
		observableChange();
		// Missile size
		int speed = shape & 0x30;
		if 		(speed == 0x00) speed = 8;		// Normal size = 8 = full speed = 1 pixel per clock
		else if	(speed == 0x10)	speed = 4;
		else if	(speed == 0x20) speed = 2;
		else if	(speed == 0x30)	speed = 1;
		if (missile0ScanSpeed != speed) {
			// if a copy is about to start, adjust for the new speed
			if (missile0ScanCounter > 7) missile0ScanCounter = 7 + (missile0ScanCounter - 7) / missile0ScanSpeed * speed;
			missile0ScanSpeed = speed;
		}
		// Player size and copies
		if ((shape & 0x07) == 0x05) {			// Double size = 1/2 speed
			speed = 2;
			player0CloseCopy = player0MediumCopy = player0WideCopy = false;	
		} else if ((shape & 0x07) == 0x07) {	// Quad size = 1/4 speed
			speed = 1;
			player0CloseCopy = player0MediumCopy = player0WideCopy = false;
		} else {
			speed = 4;							// Normal size = 4 = full speed = 1 pixel per clock
			player0CloseCopy =  (shape & 0x01) != 0;  
			player0MediumCopy = (shape & 0x02) != 0;  
			player0WideCopy =   (shape & 0x04) != 0;
		}
		if (player0ScanSpeed != speed) {
			// if a copy is about to start, adjust for the new speed
			if (player0ScanCounter > 31) player0ScanCounter = 31 + (player0ScanCounter - 31) / player0ScanSpeed * 4;
			player0ScanSpeed = speed;
		}
	}

	private void player1SetShape(int shape) {
		observableChange();
		// Missile size
		int speed = shape & 0x30;
		if 		(speed == 0x00) speed = 8;		// Normal size = 8 = full speed = 1 pixel per clock
		else if	(speed == 0x10)	speed = 4;
		else if	(speed == 0x20) speed = 2;
		else if	(speed == 0x30)	speed = 1;
		if (missile1ScanSpeed != speed) {
			// if a copy is about to start, adjust for the new speed
			if (missile1ScanCounter > 7) missile1ScanCounter = 7 + (missile1ScanCounter - 7) / missile1ScanSpeed * speed;
			missile1ScanSpeed = speed;
		}
		// Player size and copies
		if ((shape & 0x07) == 0x05) {			// Double size = 1/2 speed
			speed = 2;
			player1CloseCopy = player1MediumCopy = player1WideCopy = false;	
		} else if ((shape & 0x07) == 0x07) {	// Quad size = 1/4 speed
			speed = 1;
			player1CloseCopy = player1MediumCopy = player1WideCopy = false;
		} else {
			speed = 4;							// Normal size = 4 = full speed = 1 pixel per clock
			player1CloseCopy =  (shape & 0x01) != 0;  
			player1MediumCopy = (shape & 0x02) != 0;  
			player1WideCopy =   (shape & 0x04) != 0;
		}
		if (player1ScanSpeed != speed) {
			// if a copy is about to start, adjust for the new speed
			if (player1ScanCounter > 31) player1ScanCounter = 31 + (player1ScanCounter - 31) / player1ScanSpeed * 4;
			player1ScanSpeed = speed;
		}
	}

	private void playfieldAndBallSetShape(int shape) {
		observableChange();
		final boolean reflect = (shape & 0x01) != 0;
		if (playfieldReflected != reflect) {
			playfieldReflected = reflect;
			playfieldPatternInvalid = true;
		}
		playfieldScoreMode = (shape & 0x02) != 0;
		playfieldPriority = (shape & 0x04) != 0;
		int speed = shape & 0x30;
		if 		(speed == 0x00) speed = 8;		// Normal size = 8 = full speed = 1 pixel per clock
		else if	(speed == 0x10) speed = 4;
		else if	(speed == 0x20) speed = 2;
		else if	(speed == 0x30) speed = 1;
		if (ballScanSpeed != speed) {
			// if a copy is about to start, adjust for the new speed
			if (ballScanCounter > 7) ballScanCounter = 7 + (ballScanCounter - 7) / ballScanSpeed * speed;
			ballScanSpeed = speed;
		}
	}

	private void hitRESP0() {
		observableChange();
		if (debug) debugPixel(DEBUG_P0_RES_COLOR);
		if (clock >= HBLANK_DURATION) {
			if (player0Counter != 155) player0RecentReset = true;				
			player0Counter = 155;											// Normal +4 reset
		} else if (clock > 0 ) player0Counter = hMoveHitBlank ? 156 : 157;	// If during HBLANK, +2 reset or +3 if after HMOVE
		else player0Counter = 158;											// +1 reset 
	}
	
	private void hitRESP1() {
		observableChange();
		if (debug) debugPixel(DEBUG_P1_RES_COLOR);
		if (clock >= HBLANK_DURATION) {
			if (player1Counter != 155) player1RecentReset = true;				
			player1Counter = 155;
		} else if (clock > 0 ) player1Counter = hMoveHitBlank ? 156 : 157;
		else player1Counter = 158; 
	}
	
	private void hitRESM0() {
		observableChange();
		if (debug) debugPixel(DEBUG_M0_COLOR);
//		if (clock >= HBLANK_DURATION) {
//			if (missile0Counter != 155) missile0RecentReset = true;				
//			missile0Counter = 155;
//		}
//		else if (clock > 0 ) missile0Counter = hMoveHitBlank ? 156 : 157;
//		else missile0Counter = 158; 

		if (clock >= HBLANK_DURATION) 
			missile0Counter = 155;
		else if (hMoveHitBlank && clock > hMoveHitClock + 4 && clock < hMoveHitClock + 4 + 15 * 4 )
			missile0Counter = 157 - ((clock - hMoveHitClock - 4) >> 2);
		else 
			missile0Counter = 157;	
	}
	
	private void hitRESM1() {
		observableChange();
		if (debug) debugPixel(DEBUG_M1_COLOR);
//		if (clock >= HBLANK_DURATION) {
//			if (missile1Counter != 155) missile1RecentReset = true;				
//			missile1Counter = 155;
//		} else if (clock > 0 ) missile1Counter = hMoveHitBlank ? 156 : 157;
//		else missile1Counter = 158; 

		if (clock >= HBLANK_DURATION) 
			missile1Counter = 155;
		else if (hMoveHitBlank && clock > hMoveHitClock + 4 && clock < hMoveHitClock + 4 + 15 * 4 )
			missile1Counter = 157 - ((clock - hMoveHitClock - 4) >> 2);
		else 
			missile1Counter = 157;	
	}
	
	private void hitRESBL() {
		observableChange();
		if (debug) debugPixel(DEBUG_BL_COLOR);
//		if (clock >= HBLANK_DURATION) ballCounter = 155;
//		else if (clock > 0 ) ballCounter = hMoveHitBlank ? 156 : 157;
//		else ballCounter = 157;	

		if (clock >= HBLANK_DURATION) 
			ballCounter = 155;
		else if (hMoveHitBlank && clock > hMoveHitClock + 4 && clock < hMoveHitClock + 4 + 15 * 4 )
			ballCounter = 157 - ((clock - hMoveHitClock - 4) >> 2);
		else 
			ballCounter = 157;	
	}
	
	private void hitHMOVE() {
		if (debug) debugPixel(DEBUG_HMOVE_COLOR);
		// 210 is maybe the minimum clock to hit HMOVE for effect in the next line
		if (clock >= HBLANK_DURATION && clock < 210) {
			debugInfo("Illegal HMOVE hit");
			return;
		}
		hMoveHitBlank = clock < HBLANK_DURATION;
		hMoveHitClock = clock;
		int add;
		boolean vis = false;
		add = (hMoveHitBlank ? HMP0 : HMP0 + 8); if (add != 0) { 
			vis = true;
			if (add > 0) for (int i = add; i > 0; i--) player0ClockCounter();
			else { 
				player0Counter += add; if (player0Counter < 0) player0Counter += 160; 
				if (player0ScanCounter >= 0) player0ScanCounter -= player0ScanSpeed * add; 
			}
		}
		add = (hMoveHitBlank ? HMP1 : HMP1 + 8); if (add != 0) {
			vis = true;
			if (add > 0) for (int i = add; i > 0; i--) player1ClockCounter();
			else { 
				player1Counter += add; if (player1Counter < 0) player1Counter += 160; 
				if (player1ScanCounter >= 0) player1ScanCounter -= player1ScanSpeed * add; 
			}
		}
		add = (hMoveHitBlank ? HMM0 : HMM0 + 8); if (add != 0) {
			vis = true;
			if (add > 0) for (int i = add; i > 0; i--) missile0ClockCounter();
			else { 
				missile0Counter += add; if (missile0Counter < 0) missile0Counter += 160; 
				if (missile0ScanCounter >= 0) missile0ScanCounter -= missile0ScanSpeed * add; 
			}
		}
		add = (hMoveHitBlank ? HMM1 : HMM1 + 8); if (add != 0) {
			vis = true;
			if (add > 0) for (int i = add; i > 0; i--) missile1ClockCounter();
			else { 
				missile1Counter += add; if (missile1Counter < 0) missile1Counter += 160;
				if (missile1ScanCounter >= 0) missile1ScanCounter -= missile1ScanSpeed * add; 
			}
		}
		add = (hMoveHitBlank ? HMBL : HMBL + 8); if (add != 0) {
			vis = true;
			if (add > 0) for (int i = add; i > 0; i--) ballClockCounter();
			else { 
				ballCounter += add; if (ballCounter < 0) ballCounter += 160;
				if (ballScanCounter >= 0) ballScanCounter -= ballScanSpeed * add;
			}
		}
		if (vis) observableChange();
	}	
	
	private void missile0SetResetToPlayer(int res) {
		observableChange();
		if (missile0ResetToPlayer = (res & 0x02) != 0) missile0Enabled = false;
	}

	private void missile1SetResetToPlayer(int res) {
		observableChange();
		if (missile1ResetToPlayer = (res & 0x02) != 0) missile1Enabled = false;
	}

	private void vBlankSet(int blank) {
		observableChange();
		vBlankDecode.start((blank & 0x02) != 0);
		if ((blank & 0x40) != 0) {				// Enable Joystick Button latches
			controlsButtonsLatched = true;
		} else {								
			controlsButtonsLatched = false;		// Disable latches and update registers with the current button state
			if (controlsJOY0ButtonPressed) INPT4 &= 0x7f; else INPT4 |= 0x80;
			if (controlsJOY1ButtonPressed) INPT5 &= 0x7f; else INPT5 |= 0x80;
		}
		if ((blank & 0x80) != 0) {				// Ground paddle capacitors
			paddleCapacitorsGrounded = true;
			paddle0CapacitorCharge = paddle1CapacitorCharge = 0;
			INPT0 &= 0x7f; INPT1 &= 0x7f; INPT2 &= 0x7f; INPT3 &= 0x7f;
		} else
			paddleCapacitorsGrounded = false;
	}

	private void adjustLineAtEnd() {
		if (hMoveHitBlank) {
		 	// Fills the extended HBLANK portion of the line if needed
			linePixels[HBLANK_DURATION] =
			linePixels[HBLANK_DURATION + 1] =
			linePixels[HBLANK_DURATION + 2] =
			linePixels[HBLANK_DURATION + 3] =
			linePixels[HBLANK_DURATION + 4] =
			linePixels[HBLANK_DURATION + 5] =
			linePixels[HBLANK_DURATION + 6] =
			linePixels[HBLANK_DURATION + 7] = hBlankColor;		// This is faster than Arrays.fill()
			hMoveHitBlank = false;
		}
		if (debugLevel >= 2) processDebugPixelsInLine();
	}
	
	private void observableChange() {
		lastObservableChangeClock = clock;
		if (repeatLastLine) repeatLastLine = false;	
	}

	private void debug(int level) {
		debugLevel = level > 4 ? 0 : level;
		debug = debugLevel != 0;
		videoOutput.showOSD(debug ? "Debug Level " + debugLevel : "Debug OFF", true);
		bus.cpu.debug = debug;
		bus.pia.debug = debug;
		if (debug) debugSetColors();
		else debugRestoreColors();
	}

	private void debugSetColors() {
		player0Color = DEBUG_P0_COLOR;
		player1Color = DEBUG_P1_COLOR;
		missile0Color = DEBUG_M0_COLOR;
		missile1Color = DEBUG_M1_COLOR;
		ballColor = DEBUG_BL_COLOR;
		playfieldColor = DEBUG_PF_COLOR;
		playfieldBackground = DEBUG_BK_COLOR;
		hBlankColor = debugLevel >= 1 ? DEBUG_HBLANK_COLOR : HBLANK_COLOR;
		vBlankColor = debugLevel >= 2 ? DEBUG_VBLANK_COLOR : VBLANK_COLOR;
	}

	private void debugRestoreColors() {
		hBlankColor = HBLANK_COLOR;
		vBlankColor = VBLANK_COLOR;
		playfieldBackground = palette[0];
		Arrays.fill(linePixels, hBlankColor);
		observableChange();
	}

	private void debugInfo(String str) {
		if (debug) System.out.printf("Line: %3d, Pixel: %3d, " + str + "\n", videoOutput.monitor().currentLine(), clock);
	}
	
	private void debugPixel(int color) {
		debugPixels[clock] = color;
	}
	
	private boolean debugPausedNoMoreFrames() {
		if (debugPauseMoreFrames <= 0) return true;
		debugPauseMoreFrames--;
		return false;
	}

	private void processDebugPixelsInLine() {
		Arrays.fill(linePixels, 0, HBLANK_DURATION, hBlankColor);
		if (debugLevel >= 4 && videoOutput.monitor().currentLine() % 10 == 0)
			for (int i = 0; i < LINE_WIDTH; i++) {
				if (debugPixels[i] != 0) continue;
				if (i < HBLANK_DURATION) {
					if (i % 6 == 0 || i == 66 || i == 63)
						debugPixels[i] = DEBUG_MARKS_COLOR;
				} else {
					if ((i - HBLANK_DURATION - 1) % 6 == 0)
						debugPixels[i] = DEBUG_MARKS_COLOR;
				}
			}
		if (debugLevel >= 3) 
			for (int i = 0; i < LINE_WIDTH; i++)
				if (debugPixels[i] != 0) {
					linePixels[i] = debugPixels[i];
					debugPixels[i] = 0;
				}
		observableChange();
	}
	
	private void chargePaddleCapacitors() {
		if (INPT0 < 0x80 && ++paddle0CapacitorCharge >= paddle0Position) INPT0 |= 0x80;
		if (INPT1 < 0x80 && ++paddle1CapacitorCharge >= paddle1Position) INPT1 |= 0x80;
	}

	private void initLatchesAtPowerOn() {
		CXM0P = CXM1P = CXP0FB = CXP1FB = CXM0FB = CXM1FB = CXBLPF = CXPPMM = 0;
		INPT0 = INPT1 = INPT2 = INPT3 = 0;
		INPT4 = INPT5 = 0x80;
	}

	@Override
	public byte readByte(int address) {
		final int reg = address & READ_ADDRESS_MASK;

		if (reg == 0x00) return (byte) CXM0P;
		if (reg == 0x01) return (byte) CXM1P;
		if (reg == 0x02) return (byte) CXP0FB;
		if (reg == 0x03) return (byte) CXP1FB;
		if (reg == 0x04) return (byte) CXM0FB;
		if (reg == 0x05) return (byte) CXM1FB;
		if (reg == 0x06) return (byte) CXBLPF;
		if (reg == 0x07) return (byte) CXPPMM;
		if (reg == 0x08) return (byte) INPT0;
		if (reg == 0x09) return (byte) INPT1;
		if (reg == 0x0A) return (byte) INPT2;
		if (reg == 0x0B) return (byte) INPT3;
		if (reg == 0x0C) return (byte) INPT4;
		if (reg == 0x0D) return (byte) INPT5;

		// debugInfo(String.format("Invalid TIA read register address: %04x", address));
		return 0;
	}	

	@Override
	public void writeByte(int address, byte b) {
		final int i = b & 0xff;
		final int reg = address & WRITE_ADDRESS_MASK;
		
		if (reg == 0x1B) { /*GRP0   = i;*/ playerDelaySpriteChange(0, i); return; }
		if (reg == 0x1C) { /*GRP1   = i;*/ playerDelaySpriteChange(1, i); return; }
		if (reg == 0x02) { /*WSYNC  = i;*/ bus.cpu.RDY = false; if (debug) debugPixel(DEBUG_WSYNC_COLOR); return; } 	// <STROBE> Halts the CPU until the next HBLANK
		if (reg == 0x2A) { /*HMOVE  = i;*/ hitHMOVE();	return; }						   	
		if (reg == 0x0D) { if (PF0 != i || playfieldDelayedChangePart == 0) playfieldDelaySpriteChange(0, i); return; }
		if (reg == 0x0E) { if (PF1 != i || playfieldDelayedChangePart == 1) playfieldDelaySpriteChange(1, i); return; }
		if (reg == 0x0F) { if (PF2 != i || playfieldDelayedChangePart == 2) playfieldDelaySpriteChange(2, i); return; }
		if (reg == 0x14) { /*RESBL  = i;*/ hitRESBL(); return; }
		if (reg == 0x10) { /*RESP0  = i;*/ hitRESP0(); return; }
		if (reg == 0x11) { /*RESP1  = i;*/ hitRESP1(); return; }
		if (reg == 0x12) { /*RESM0  = i;*/ hitRESM0(); return; }
		if (reg == 0x13) { /*RESM1  = i;*/ hitRESM1(); return; }
		if (reg == 0x20) { HMP0   = (b >> 4); return; }
		if (reg == 0x21) { HMP1   = (b >> 4); return; }
		if (reg == 0x22) { HMM0   = (b >> 4); return; }
		if (reg == 0x23) { HMM1   = (b >> 4); return; }
		if (reg == 0x24) { HMBL   = (b >> 4); return; }
		if (reg == 0x2B) { /*HMCLR  = i;*/ HMP0 = HMP1 = HMM0 = HMM1 = HMBL = 0; return; }
		if (reg == 0x06) { /*COLUP0 = i;*/ observableChange(); if (!debug) player0Color = missile0Color = palette[i]; return; }
		if (reg == 0x07) { /*COLUP1 = i;*/ observableChange(); if (!debug) player1Color = missile1Color = palette[i]; return; }
		if (reg == 0x08) { /*COLUPF = i;*/ observableChange(); if (!debug) playfieldColor = ballColor = palette[i]; return; }
		if (reg == 0x09) { /*COLUBK = i;*/ observableChange(); if (!debug) playfieldBackground = palette[i]; return; }
		if (reg == 0x1D) { /*ENAM0  = i;*/ observableChange(); missile0Enabled = (i & 0x02) != 0; return; }
		if (reg == 0x1E) { /*ENAM1  = i;*/ observableChange(); missile1Enabled = (i & 0x02) != 0; return; }
		if (reg == 0x1F) { /*ENABL  = i;*/ ballSetGraphic(i); return; }
		if (reg == 0x04) { /*NUSIZ0 = i;*/ player0SetShape(i); return; }
		if (reg == 0x05) { /*NUSIZ1 = i;*/ player1SetShape(i); return; }
		if (reg == 0x0A) { /*CTRLPF = i;*/ playfieldAndBallSetShape(i); return; }
		if (reg == 0x0B) { /*REFP0  = i;*/ observableChange(); player0Reflected = (i & 0x08) != 0; return; }
		if (reg == 0x0C) { /*REFP1  = i;*/ observableChange(); player1Reflected = (i & 0x08) != 0; return; }
		if (reg == 0x25) { /*VDELP0 = i;*/ observableChange(); player0VerticalDelay = (i & 0x01) != 0; return; }
		if (reg == 0x26) { /*VDELP1 = i;*/ observableChange(); player1VerticalDelay = (i & 0x01) != 0; return; }
		if (reg == 0x27) { /*VDELBL = i;*/ observableChange(); ballVerticalDelay = (i & 0x01) != 0; return; }
		if (reg == 0x15) { AUDC0  = i; audioOutput.channel0().setControl(i & 0x0f); return; }
		if (reg == 0x16) { AUDC1  = i; audioOutput.channel1().setControl(i & 0x0f); return; }
		if (reg == 0x17) { AUDF0  = i; audioOutput.channel0().setDivider((i & 0x1f) + 1); return; }		// Bits 0-4, Divider from 1 to 32 )
		if (reg == 0x18) { AUDF1  = i; audioOutput.channel1().setDivider((i & 0x1f) + 1); return; }		// Bits 0-4, Divider from 1 to 32 )
		if (reg == 0x19) { AUDV0  = i; audioOutput.channel0().setVolume(i & 0x0f); return; }			// Bits 0-3, Volume from 0 to 15 )
		if (reg == 0x1A) { AUDV1  = i; audioOutput.channel1().setVolume(i & 0x0f); return; }			// Bits 0-3, Volume from 0 to 15 )
		if (reg == 0x28) { /*RESMP0 = i;*/ missile0SetResetToPlayer(i); return; }
		if (reg == 0x29) { /*RESMP1 = i;*/ missile1SetResetToPlayer(i); return; }
		if (reg == 0x01) { /*VBLANK = i;*/ vBlankSet(i); return; }
		if (reg == 0x00) { /*VSYNC  = i;*/ observableChange(); vSyncOn = (i & 0x02) != 0; return; }
		if (reg == 0x2C) { /*CXCLR  = i;*/ observableChange(); CXM0P = CXM1P = CXP0FB = CXP1FB = CXM0FB = CXM1FB = CXBLPF = CXPPMM = 0; return; }
		if (reg == 0x03) { /*RSYNC  = i;*/ /* clock = 0; */ return; }

		// debugInfo(String.format("Invalid TIA write register address: %04x value %d", address, b)); 
	}

	@Override
	public void controlStateChanged(ConsoleControls.Control control, boolean state) {
		switch (control) {
			case JOY0_BUTTON: 
				if (state) {
					controlsJOY0ButtonPressed = true;
					INPT4 &= 0x7f;
				} else {
					controlsJOY0ButtonPressed = false;
					if (!controlsButtonsLatched)			// Does not lift the button if Latched Mode is on
						INPT4 |= 0x80;
				}
				return;
			case JOY1_BUTTON:
				if (state) {
					controlsJOY1ButtonPressed = true;
					INPT5 &= 0x7f;
				} else {
					controlsJOY1ButtonPressed = false;
					if (!controlsButtonsLatched)			// Does not lift the button if Latched Mode is on
						INPT5 |= 0x80;
				}
				return;
		}
		// Toggles
		if (!state) return;
		switch (control) {
			case DEBUG:
				debug(debugLevel + 1); return;
			case NO_COLLISIONS:
				debugNoCollisions = !debugNoCollisions;
				videoOutput.showOSD(debugNoCollisions ? "Collisions OFF" : "Collisions ON", true);
				return;
			case PAUSE:
				debugPause = !debugPause; debugPauseMoreFrames = 0; 
				videoOutput.showOSD(debugPause ? "PAUSE" : "RESUME", true);
				return;
			case FRAME:
				debugPauseMoreFrames++; return;
			case TRACE:
				bus.cpu.trace = !bus.cpu.trace; return;
		}
	}

	@Override
	public void controlStateChanged(ConsoleControls.Control control, int position) {
		switch (control) {
			case PADDLE0_POSITION:
				paddle0Position = position; return;
			case PADDLE1_POSITION:
				paddle1Position = position; return;
		}
	}

	@Override
	public void controlsStateReport(Map<ConsoleControls.Control, Boolean> report) {
		//  No TIA controls visible outside by now
	}

	public TIAState saveState() {
		TIAState state = new TIAState();
		state.linePixels					   =  linePixels.clone();
		state.lastObservableChangeClock		   =  lastObservableChangeClock;
		state.repeatLastLine 				   =  repeatLastLine;
		state.vSyncOn                     	   =  vSyncOn;                    
		state.vBlankOn                    	   =  vBlankOn;
		state.playfieldPattern            	   =  playfieldPattern.clone();
		state.playfieldPatternInvalid     	   =  playfieldPatternInvalid;    
		state.playfieldCurrentPixel       	   =  playfieldCurrentPixel;      
		state.playfieldColor              	   =  playfieldColor;             
		state.playfieldBackground         	   =  playfieldBackground;        
		state.playfieldReflected          	   =  playfieldReflected;         
		state.playfieldScoreMode          	   =  playfieldScoreMode;         
		state.playfieldPriority           	   =  playfieldPriority;          
		state.player0ActiveSprite         	   =  player0ActiveSprite;        
		state.player0DelayedSprite        	   =  player0DelayedSprite;
		state.player0Color                	   =  player0Color;               
		state.player0RecentReset     	  	   =  player0RecentReset;      
		state.player0Counter	          	   =  player0Counter;	         
		state.player0ScanCounter	      	   =  player0ScanCounter;	     
		state.player0ScanSpeed            	   =  player0ScanSpeed;           
		state.player0VerticalDelay        	   =  player0VerticalDelay;       
		state.player0CloseCopy            	   =  player0CloseCopy;           
		state.player0MediumCopy           	   =  player0MediumCopy;
		state.player0WideCopy             	   =  player0WideCopy;            
		state.player0Reflected            	   =  player0Reflected;
		state.player1ActiveSprite         	   =  player1ActiveSprite;        
		state.player1DelayedSprite        	   =  player1DelayedSprite;
		state.player1Color  	           	   =  player1Color;               
		state.player1RecentReset			   =  player1RecentReset;      
		state.player1Counter              	   =  player1Counter;             
		state.player1ScanCounter		  	   =  player1ScanCounter;						
		state.player1ScanSpeed			  	   =  player1ScanSpeed;						
		state.player1VerticalDelay        	   =  player1VerticalDelay;       
		state.player1CloseCopy            	   =  player1CloseCopy;           
		state.player1MediumCopy           	   =  player1MediumCopy;
		state.player1WideCopy             	   =  player1WideCopy;            
		state.player1Reflected            	   =  player1Reflected;
		state.missile0Enabled             	   =  missile0Enabled;            
		state.missile0Color               	   =  missile0Color;              
		state.missile0RecentReset   	   	   =  missile0RecentReset;     
		state.missile0Counter             	   =  missile0Counter;            
		state.missile0ScanCounter         	   =  missile0ScanCounter;        
		state.missile0ScanSpeed			  	   =  missile0ScanSpeed;						
		state.missile0ResetToPlayer		  	   =  missile0ResetToPlayer;					
		state.missile1Enabled             	   =  missile1Enabled;            
		state.missile1Color               	   =  missile1Color;              
		state.missile1RecentReset   	   	   =  missile1RecentReset;     
		state.missile1Counter             	   =  missile1Counter;            
		state.missile1ScanCounter         	   =  missile1ScanCounter;        
		state.missile1ScanSpeed			  	   =  missile1ScanSpeed;						
		state.missile1ResetToPlayer		  	   =  missile1ResetToPlayer;					
		state.ballEnabled                 	   =  ballEnabled;                
		state.ballDelayedEnablement       	   =  ballDelayedEnablement;      
		state.ballColor                   	   =  ballColor;                  
		state.ballCounter                 	   =  ballCounter;                
		state.ballScanCounter             	   =  ballScanCounter;            
		state.ballScanSpeed				  	   =  ballScanSpeed;						
		state.ballVerticalDelay           	   =  ballVerticalDelay;          
		state.playfieldDelayedChangeClock	   =  playfieldDelayedChangeClock;
		state.playfieldDelayedChangePart	   =  playfieldDelayedChangePart;
		state.playfieldDelayedChangePattern	   =  playfieldDelayedChangePattern;
		state.playersDelayedSpriteChanges      =  Array2DCopy.copy(playersDelayedSpriteChanges);
		state.playersDelayedSpriteChangesCount =  playersDelayedSpriteChangesCount;
		state.PF0						  	   =  PF0;	  
		state.PF1						  	   =  PF1;
		state.PF2						  	   =  PF2;  		
		state.AUDC0						  	   =  AUDC0;
		state.AUDC1						  	   =  AUDC1;
		state.AUDF0						  	   =  AUDF0;
		state.AUDF1						  	   =  AUDF1;
		state.AUDV0						  	   =  AUDV0;
		state.AUDV1						  	   =  AUDV1;
		state.HMP0						  	   =  HMP0;
		state.HMP1						  	   =  HMP1;  	
		state.HMM0						  	   =  HMM0;  	
		state.HMM1						  	   =  HMM1;  	
		state.HMBL						  	   =  HMBL;  	
		state.CXM0P 					  	   =  CXM0P;  
		state.CXM1P 					  	   =  CXM1P;  
		state.CXP0FB 					  	   =  CXP0FB;
		state.CXP1FB 					  	   =  CXP1FB;
		state.CXM0FB 					  	   =  CXM0FB;
		state.CXM1FB 					  	   =  CXM1FB;
		state.CXBLPF 					  	   =  CXBLPF;
		state.CXPPMM 					  	   =  CXPPMM;
		return state;
	}

	public void loadState(TIAState state) {
		linePixels						 =  state.linePixels;
		lastObservableChangeClock		 =	state.lastObservableChangeClock;
		repeatLastLine 					 =	state.repeatLastLine;
		vSyncOn                     	 =  state.vSyncOn;                     
		vBlankOn                    	 =  state.vBlankOn;
		playfieldPattern            	 =  state.playfieldPattern;            
		playfieldPatternInvalid     	 =  state.playfieldPatternInvalid;     
		playfieldCurrentPixel       	 =  state.playfieldCurrentPixel;       
		playfieldColor              	 =  state.playfieldColor;              
		playfieldBackground         	 =  state.playfieldBackground;         
		playfieldReflected          	 =  state.playfieldReflected;          
		playfieldScoreMode          	 =  state.playfieldScoreMode;          
		playfieldPriority           	 =  state.playfieldPriority;           
		player0ActiveSprite         	 =  state.player0ActiveSprite;         
		player0DelayedSprite        	 =  state.player0DelayedSprite;
		player0Color                	 =  state.player0Color;                
		player0RecentReset       	 	 =  state.player0RecentReset;       
		player0Counter	            	 =  state.player0Counter;	          
		player0ScanCounter	        	 =  state.player0ScanCounter;	      
		player0ScanSpeed            	 =  state.player0ScanSpeed;            
		player0VerticalDelay        	 =  state.player0VerticalDelay;        
		player0CloseCopy            	 =  state.player0CloseCopy;            
		player0MediumCopy           	 =  state.player0MediumCopy;
		player0WideCopy             	 =  state.player0WideCopy;             
		player0Reflected            	 =  state.player0Reflected;
		player1ActiveSprite         	 =  state.player1ActiveSprite;         
		player1DelayedSprite        	 =  state.player1DelayedSprite;
		player1Color                	 =  state.player1Color;                
		player1RecentReset       		 =  state.player1RecentReset;       
		player1Counter              	 =  state.player1Counter;              
		player1ScanCounter				 =  state.player1ScanCounter;		 	
		player1ScanSpeed				 =  state.player1ScanSpeed;			 	
		player1VerticalDelay        	 =  state.player1VerticalDelay;        
		player1CloseCopy            	 =  state.player1CloseCopy;            
		player1MediumCopy           	 =  state.player1MediumCopy;
		player1WideCopy             	 =  state.player1WideCopy;             
		player1Reflected            	 =  state.player1Reflected;
		missile0Enabled             	 =  state.missile0Enabled;             
		missile0Color               	 =  state.missile0Color;               
		missile0RecentReset      	 	 =  state.missile0RecentReset;      
		missile0Counter             	 =  state.missile0Counter;             
		missile0ScanCounter         	 =  state.missile0ScanCounter;         
		missile0ScanSpeed				 =  state.missile0ScanSpeed;			 	
		missile0ResetToPlayer			 =  state.missile0ResetToPlayer;		 	
		missile1Enabled             	 =  state.missile1Enabled;             
		missile1Color               	 =  state.missile1Color;               
		missile1RecentReset      	 	 =  state.missile1RecentReset;      
		missile1Counter             	 =  state.missile1Counter;             
		missile1ScanCounter         	 =  state.missile1ScanCounter;         
		missile1ScanSpeed				 =  state.missile1ScanSpeed;			 	
		missile1ResetToPlayer			 =  state.missile1ResetToPlayer;		 	
		ballEnabled                 	 =  state.ballEnabled;                 
		ballDelayedEnablement       	 =  state.ballDelayedEnablement;       
		ballColor                   	 =  state.ballColor;                   
		ballCounter                 	 =  state.ballCounter;                 
		ballScanCounter             	 =  state.ballScanCounter;             
		ballScanSpeed					 =  state.ballScanSpeed;				 
		ballVerticalDelay           	 =  state.ballVerticalDelay;           
		playfieldDelayedChangeClock		 =  state.playfieldDelayedChangeClock;
		playfieldDelayedChangePart		 =  state.playfieldDelayedChangePart;
		playfieldDelayedChangePattern	 =  state.playfieldDelayedChangePattern;
		playersDelayedSpriteChanges      =  state.playersDelayedSpriteChanges;      
		playersDelayedSpriteChangesCount =  state.playersDelayedSpriteChangesCount; 
		PF0								 =  state.PF0;
		PF1								 =  state.PF1;
		PF2								 =  state.PF2;
		AUDC0							 =  state.AUDC0; audioOutput.channel0().setControl(AUDC0 & 0x0f);		// Also update the Audio Generator
		AUDC1							 =  state.AUDC1; audioOutput.channel1().setControl(AUDC1 & 0x0f);
		AUDF0							 =  state.AUDF0; audioOutput.channel0().setDivider((AUDF0 & 0x1f) + 1); 	
		AUDF1							 =  state.AUDF1; audioOutput.channel1().setDivider((AUDF1 & 0x1f) + 1); 
		AUDV0							 =  state.AUDV0; audioOutput.channel0().setVolume(AUDV0 & 0x0f);
		AUDV1							 =  state.AUDV1; audioOutput.channel1().setVolume(AUDV1 & 0x0f);
		HMP0							 =  state.HMP0;
		HMP1							 =  state.HMP1;		
		HMM0							 =  state.HMM0;
		HMM1							 =  state.HMM1;		
		HMBL							 =  state.HMBL;
		CXM0P 							 =  state.CXM0P; 
		CXM1P 							 =  state.CXM1P; 
		CXP0FB							 =  state.CXP0FB;
		CXP1FB							 =  state.CXP1FB;
		CXM0FB							 =  state.CXM0FB;
		CXM1FB							 =  state.CXM1FB;
		CXBLPF							 =  state.CXBLPF;
		CXPPMM							 =  state.CXPPMM;
		if (debug) debugSetColors();						// IF debug is on, ensure debug colors are used
	}
	
	
	// Variables ----------------------------------------------
	
	private final VideoGenerator videoOutput;
	private final AudioMonoGenerator audioOutput;
	
	private int clock = 0;

	private BUS bus;

	private boolean powerOn = false;
	private final int debugPixels[] = new int[LINE_WIDTH];
	
	private int[] palette;
	private int vSyncColor = 0xffdddddd;
	private int vBlankColor = VBLANK_COLOR;
	private int hBlankColor = VBLANK_COLOR;

	private boolean debugPause = false;
	private int debugPauseMoreFrames = 0;
	

	// State Variables ----------------------------------------------

	private boolean debug = false;
	private int debugLevel = 0;
	private boolean debugNoCollisions = false;

	private int[] linePixels = new int[LINE_WIDTH];
	private int lastObservableChangeClock = -1;
	private boolean repeatLastLine;

	private boolean vSyncOn = false;

	private boolean vBlankOn = false;
	private VBlankDecode vBlankDecode = new VBlankDecode();

	private boolean hMoveHitBlank = false;
	private int hMoveHitClock = -1;		// TODO State
	
	private boolean[] playfieldPattern = new boolean[40];
	private boolean playfieldPatternInvalid = true;
	private boolean playfieldCurrentPixel = false;
	private int playfieldColor = 0xff000000;
	private int playfieldBackground = 0xff000000;
	private boolean playfieldReflected = false;
	private boolean playfieldScoreMode = false;
	private boolean playfieldPriority = false;

	private int playfieldDelayedChangeClock = -1;
	private int playfieldDelayedChangePart = -1;			// Supports only one delayed change at a time.
	private int playfieldDelayedChangePattern = -1;
	
	private int player0ActiveSprite = 0;
	private int player0DelayedSprite = 0;
	private int player0Color = 0xff000000;
	private boolean player0RecentReset = false;
	private int player0LastResetClock = -1;	
	private int player0Counter = 0;							// Position!	
	private int player0ScanCounter = -1;					// 31 down to 0. Current scan position. Negative = scan not happening	
	private int player0ScanSpeed = 4;						// Decrement ScanCounter. 4 per clock = 1 pixel wide
	private boolean player0VerticalDelay = false;
	private boolean player0CloseCopy = false;
	private boolean player0MediumCopy = false;
	private boolean player0WideCopy = false;
	private boolean player0Reflected = false;
	
	private int player1ActiveSprite = 0;
	private int player1DelayedSprite = 0;
	private int player1Color = 0xff000000;
	private boolean player1RecentReset = false;
	private int player1LastResetClock = -1;	
	private int player1Counter = 0;
	private int player1ScanCounter = -1;
	private int player1ScanSpeed = 4;
	private boolean player1VerticalDelay = false;
	private boolean player1CloseCopy = false;
	private boolean player1MediumCopy = false;
	private boolean player1WideCopy = false;
	private boolean player1Reflected = false;
	
	private boolean missile0Enabled = false;
	private int missile0Color = 0xff000000;
	private boolean missile0RecentReset = false;
	private int missile0Counter = 0;
	private int missile0ScanCounter = -1;
	private int missile0ScanSpeed = 8;			// 8 per clock = 1 pixel wide
	private boolean missile0ResetToPlayer = false;

	private boolean missile1Enabled = false;
	private int missile1Color = 0xff000000;
	private boolean missile1RecentReset = false;
	private int missile1Counter = 0;
	private int missile1ScanCounter = -1;
	private int missile1ScanSpeed = 8;
	private boolean missile1ResetToPlayer = false;
	
	private boolean ballEnabled = false;
	private boolean ballDelayedEnablement = false;
	private int ballColor = 0xff000000;
	private int ballCounter = 0;
	private int ballScanCounter = -1;
	private int ballScanSpeed = 8;				// 8 per clock = 1 pixel wide			
	private boolean ballVerticalDelay = false;

	private int[][] playersDelayedSpriteChanges = new int[PLAYERS_DELAYED_SPRITE_GHANGES_MAX_COUNT][3];
	private int playersDelayedSpriteChangesCount = 0;

	private boolean controlsButtonsLatched = false;
	private boolean controlsJOY0ButtonPressed = false;
	private boolean controlsJOY1ButtonPressed = false;

	private boolean paddleCapacitorsGrounded = false;
	private int paddle0Position = -1;			// 380 = Left, 190 = Middle, 0 = Right. -1 = disconnected, won't charge POTs
	private int paddle0CapacitorCharge = 0;
	private int paddle1Position = -1;
	private int paddle1CapacitorCharge = 0;

	// Read registers -------------------------------------------

	private int CXM0P; 		// collision M0-P1, M0-P0 (Bit 7,6)
	private int CXM1P; 		// collision M1-P0, M1-P1
	private int CXP0FB;		// collision P0-PF, P0-BL
	private int CXP1FB;		// collision P1-PF, P1-BL
	private int CXM0FB;		// collision M0-PF, M0-BL
	private int CXM1FB;		// collision M1-PF, M1-BL
	private int CXBLPF;		// collision BL-PF, unused
	private int CXPPMM;		// collision P0-P1, M0-M1
	private int INPT0; 		// Paddle0 Left pot port
	private int INPT1; 		// Paddle0 Right pot port
	private int INPT2; 		// Paddle1 Left pot port
	private int INPT3; 		// Paddle1 Right pot port
	private int INPT4; 		// input (Joy0 button)
	private int INPT5; 		// input (Joy1 button)

	
	// Write registers -------------------------------------------

//	private int VSYNC;		// ......1.  vertical sync set-clear
//	private int VBLANK;		// 11....1.  vertical blank set-clear
//	private int WSYNC;		// <strobe>  wait for leading edge of horizontal blank
//	private int RSYNC;		// <strobe>  reset horizontal sync counter
//	private int NUSIZ0;		// ..111111  number-size player-missile 0
//	private int NUSIZ1;		// ..111111  number-size player-missile 1
//	private int COLUP0;		// 1111111.  color-lum player 0 and missile 0
//	private int COLUP1;		// 1111111.  color-lum player 1 and missile 1
//	private int COLUPF;		// 1111111.  color-lum playfield and ball
//	private int COLUBK;		// 1111111.  color-lum background
//	private int CTRLPF;		// ..11.111  control playfield ball size & collisions
//	private int REFP0;		// ....1...  reflect player 0
//	private int REFP1;		// ....1...  reflect player 1
	private int PF0;		// 1111....  playfield register byte 0
	private int PF1;		// 11111111  playfield register byte 1
	private int PF2;		// 11111111  playfield register byte 2
//	private int RESP0;		// <strobe>  reset player 0
//	private int RESP1;		// <strobe>  reset player 1
//	private int RESM0;		// <strobe>  reset missile 0
//	private int RESM1;		// <strobe>  reset missile 1
//	private int RESBL;		// <strobe>  reset ball
	private int AUDC0;		// ....1111  audio control 0
	private int AUDC1;		// ....1111  audio control 1
	private int AUDF0;		// ...11111  audio frequency 0
	private int AUDF1;		// ...11111  audio frequency 1
	private int AUDV0;		// ....1111  audio volume 0
	private int AUDV1;		// ....1111  audio volume 1
//	private int GRP0;		// 11111111  graphics player 0
//	private int GRP1;		// 11111111  graphics player 1
//	private int ENAM0;		// ......1.  graphics (enable) missile 0
//	private int ENAM1;		// ......1.  graphics (enable) missile 1
//	private int ENABL;		// ......1.  graphics (enable) ball
	private int HMP0;		// 1111....  horizontal motion player 0
	private int HMP1;		// 1111....  horizontal motion player 1
	private int HMM0;		// 1111....  horizontal motion missile 0
	private int HMM1;		// 1111....  horizontal motion missile 1
	private int HMBL;		// 1111....  horizontal motion ball
//	private int VDELP0;		// .......1  vertical delay player 0
//	private int VDELP1;		// .......1  vertical delay player 1
//	private int VDELBL;		// .......1  vertical delay ball
//	private int RESMP0;		// ......1.  reset missile 0 to player 0
//	private int RESMP1;		// ......1.  reset missile 1 to player 1
//	private int HMOVE;		// <strobe>  apply horizontal motion
//	private int HMCLR;		// <strobe>  clear horizontal motion registers
//	private int CXCLR;		// <strobe>  clear collision latches


	// Constants --------------------------------------------------
	
	private static final int HBLANK_COLOR = 0x00000000;		// Full transparency needed for CRT emulation modes
	private static final int VBLANK_COLOR = 0x00000000;

	private static final int HBLANK_DURATION = 68;
	private static final int LINE_WIDTH = 228;

	private static final int DEBUG_MARKS_COLOR  = 0xff202020;
	private static final int DEBUG_HBLANK_COLOR = 0xff444444;
	private static final int DEBUG_VBLANK_COLOR = 0xff2a2a2a;
	
	private static final int DEBUG_WSYNC_COLOR  = 0xff770077;
	private static final int DEBUG_HMOVE_COLOR  = 0xffffffff;

	private static final int DEBUG_P0_COLOR     = 0xff0000ff;
	private static final int DEBUG_P0_RES_COLOR = 0xff2222bb;
	private static final int DEBUG_P0_GR_COLOR  = 0xff111177;
	private static final int DEBUG_P1_COLOR     = 0xffff0000;
	private static final int DEBUG_P1_RES_COLOR = 0xffbb2222;
	private static final int DEBUG_P1_GR_COLOR  = 0xff771111;
	private static final int DEBUG_M0_COLOR     = 0xff6666ff;
	private static final int DEBUG_M1_COLOR     = 0xffff6666;

	private static final int DEBUG_PF_COLOR     = 0xff448844;
	private static final int DEBUG_PF_SET_COLOR = 0xff33dd33;
	private static final int DEBUG_BK_COLOR     = 0xff334433;
	private static final int DEBUG_BL_COLOR     = 0xffffff00;

	private static final int DEBUG_SPECIAL_COLOR  = 0xff00ffff;
	private static final int DEBUG_SPECIAL_COLOR2 = 0xffff00ff;

	private static final int READ_ADDRESS_MASK = 0x000f;
	private static final int WRITE_ADDRESS_MASK = 0x003f;
	
	private static final int PLAYERS_DELAYED_SPRITE_GHANGES_MAX_COUNT = 50;  // Supports a maximum of player GR changes before any is drawn
	
	private static final boolean SYNC_WITH_AUDIO_MONITOR = Parameters.TIA_SYNC_WITH_AUDIO_MONITOR;
	private static final boolean SYNC_WITH_VIDEO_MONITOR = Parameters.TIA_SYNC_WITH_VIDEO_MONITOR;
	
	private static final double FORCED_CLOCK = Parameters.TIA_FORCED_CLOCK;	//  TIA Real Clock = NTSC clock = 3584160 or 3579545 Hz


	// Delayed decodes
	private class VBlankDecode {
		private boolean isActive = false;
		private boolean newState;
		private void start(boolean state) {
			newState = state;
			isActive = true;
		}
		private void clockPulse() {
			isActive = false;
			vBlankOn = newState;
		}
	}

	// Used to save/load states
	public static class TIAState implements Serializable {
		int[] linePixels;
		int lastObservableChangeClock;
		boolean repeatLastLine;
		boolean vSyncOn;
		boolean vBlankOn;
		boolean[] playfieldPattern;
		boolean playfieldPatternInvalid;
		boolean playfieldCurrentPixel;
		int playfieldColor;
		int playfieldBackground;
		boolean playfieldReflected;
		boolean playfieldScoreMode;
		boolean playfieldPriority;
		int player0ActiveSprite;
		int player0DelayedSprite;
		int player0Color;
		boolean player0RecentReset;
		int player0Counter;	
		int player0ScanCounter;	
		int player0ScanSpeed;
		boolean player0VerticalDelay;
		boolean player0CloseCopy;
		boolean player0MediumCopy;
		boolean player0WideCopy;
		boolean player0Reflected;
		int player1ActiveSprite;
		int player1DelayedSprite;
		int player1Color;
		boolean player1RecentReset;
		int player1Counter;
		int player1ScanCounter;						
		int player1ScanSpeed;						
		boolean player1VerticalDelay;
		boolean player1CloseCopy;
		boolean player1MediumCopy;
		boolean player1WideCopy;
		boolean player1Reflected;
		boolean missile0Enabled;
		int missile0Color;
		boolean missile0RecentReset;
		int missile0Counter;
		int missile0ScanCounter;
		int missile0ScanSpeed;						
		boolean missile0ResetToPlayer;					
		boolean missile1Enabled;
		int missile1Color;
		boolean missile1RecentReset;
		int missile1Counter;
		int missile1ScanCounter;
		int missile1ScanSpeed;						
		boolean missile1ResetToPlayer;					
		boolean ballEnabled;
		boolean ballDelayedEnablement;
		int ballColor;
		int ballCounter;
		int ballScanCounter;
		int ballScanSpeed;						
		boolean ballVerticalDelay;
		int playfieldDelayedChangeClock;
		int playfieldDelayedChangePart;
		int playfieldDelayedChangePattern;
		int[][] playersDelayedSpriteChanges;
		int playersDelayedSpriteChangesCount;
		int PF0;
		int PF1;
		int PF2;
		int AUDC0;
		int AUDC1;
		int AUDF0;		
		int AUDF1;
		int AUDV0;
		int AUDV1;
		int HMP0;
		int HMP1;		
		int HMM0;
		int HMM1;		
		int HMBL;
		int CXM0P; 
		int CXM1P; 
		int CXP0FB;
		int CXP1FB;
		int CXM0FB;
		int CXM1FB;
		int CXBLPF;
		int CXPPMM;

		public static final long serialVersionUID = 3L;
	}

}
