// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.tia;		

import general.av.video.VideoSignal;
import general.av.video.VideoStandard;
import general.board.BUS16Bits;
import general.board.ClockDriven;
import general.m6502.M6502;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import parameters.Parameters;
import utils.Array2DCopy;
import atari.console.Console;
import atari.controls.ConsoleControls;
import atari.controls.ConsoleControlsInput;
import atari.pia.PIA;
import atari.tia.audio.AudioGenerator;
import atari.tia.audio.AudioMonoGenerator;
import atari.tia.video.NTSCPalette;
import atari.tia.video.PALPalette;
import atari.tia.video.VideoGenerator;

@SuppressWarnings("unused")
public final class TIA implements BUS16Bits, ClockDriven, ConsoleControlsInput {

	public TIA(M6502 cpu, PIA pia) {
		this.cpu = cpu;
		this.pia = pia;
		videoOutput = new VideoGenerator();
		audioOutput = new AudioMonoGenerator();
	}

	public VideoGenerator videoOutput() {	// VideoSignal
		return videoOutput;
	}

	public AudioGenerator audioOutput() {	// AudioSignal
		return audioOutput;
	}

	public void videoStandard(VideoStandard standard) {
		videoOutput.standard = standard;
		palette = standard.equals(VideoStandard.NTSC) ? NTSCPalette.getPalette() : PALPalette.getPalette();
	}
	
	public double desiredClockForVideoStandard() {
		if (FORCED_CLOCK != 0) return FORCED_CLOCK;
		return videoOutput.standard().equals(VideoStandard.NTSC) ? DEFAUL_CLOCK_NTSC : DEFAUL_CLOCK_PAL;
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
	}

	@Override
	public void clockPulse() {
		if (!debugPauseHook()) return;
		boolean videoOutputVSynched = false;	
		do {
			// Releases the CPU at the beginning of the line in case a WSYNC has halted it
			cpu.RDY = true;
			// HBLANK period
			for (clock = 3; clock < HBLANK_DURATION; clock += 3) {	// 3 .. 66
				// If one entire line since last observable change has just completed, enter repeatLastLine mode
				if (clock == lastObservableChangeClock) {
					repeatLastLine = true;
					lastObservableChangeClock = -1;
				}
				// Send clock pulse to the CPU and PIA each 3 TIA cycles, at the end of the 3rd cycle, 
				pia.clockPulse();
				cpu.clockPulse();
			}
			// Display period
			for (clock = 68; clock < LINE_WIDTH; clock++) {			// 68 .. 227
				// If one entire line since last observable change has just completed, enter repeatLastLine mode
				if (clock == lastObservableChangeClock) {
					repeatLastLine = true;
					lastObservableChangeClock = -1;
				}
				// Send clock pulse to the CPU and PIA each 3 TIA cycles, at the end of the 3rd cycle, 
				if (clock % 3 == 0) {
					pia.clockPulse();
					cpu.clockPulse();
				}
				objectsTriggerScanCounters();
				if (!repeatLastLine) {
					if (vBlankOn) linePixels[clock] = vSyncOn ? vSyncColor : vBlankColor;
					else setPixelValue();
				}
				objectsIncrementCounters();
			}
			// Send the last clock pulse to the CPU and PIA, at the end of the 227th cycle, perceived by the TIA at clock 0 next line
			clock = 0;
			pia.clockPulse();
			cpu.clockPulse();
			// End of scan line
			// Handle Paddles capacitor charging
			if (!paddleCapacitorsGrounded && paddle0Position != -1)	chargePaddleCapacitors();	// Only if paddles are connected (position != -1)
			// Send the finished line to the output
			adjustLineAtEnd();
			videoOutputVSynched = videoOutput.newLine(linePixels, vSyncOn);
			// 2 audio samples per scan line ~ 31440 KHz
			audioOutput.generateNextSamples(2);		
		} while (!videoOutputVSynched && powerOn);
		if (powerOn) {
			audioOutput.sendGeneratedSamplesToMonitor();
			// If needed, synch with audio output after each frame
			if (SYNC_WITH_AUDIO_MONITOR) audioOutput.monitor.synchOutput();
			// If needed, synch with video output
			if (SYNC_WITH_VIDEO_MONITOR) videoOutput.monitor.synchOutput();
		}
	}

	private boolean debugPauseHook() {
		if (!debugPause) return true;
		if (debugPauseMoreFrames <= 0) return false;
		debugPauseMoreFrames--;
		return true;
	}

	private void setPixelValue() {
		// Updates the current PlayFiled pixel to draw only each 4 pixels, or at the first calculated pixel after stopped using cached line
		if (clock == lastObservableChangeClock || clock % 4 == 0) playfieldUpdateCurrentPixel();
		// Pixel color
		int color = 0;
		// Flags for Collision latches
		boolean P0 = false, P1 = false, M0 = false, M1 = false, FL = false, BL = false;
		// Get the value for the PlayField and Ball first only if PlayField and Ball have higher priority
		if (playfieldPriority) {
			// Get the value for the Ball
			if (ballScanCounter == 0) {
				playersPerformDelayedSpriteChanges();		// May trigger Ball delayed enablement
				if (ballEnabled) {
					BL = true;
					color = ballColor;
				}
			}
			if (playfieldCurrentPixel) { 
				FL = true;
				if (color == 0) color = !playfieldScoreMode ? playfieldColor : (clock < 148 ? player0Color : player1Color);
			}
		}
		// Get the value for Player0
		if (player0ScanCounter >= 0) {
			playersPerformDelayedSpriteChanges();
			int sprite = player0VerticalDelay ? player0ActiveSprite : player0DelayedSprite;
			if (sprite != 0)
				if (((sprite >> (player0Reflected ? (7 - player0ScanCounter) : player0ScanCounter)) & 0x01) != 0) {
					P0 = true;
					if (color == 0) color = player0Color;
				}
		}
		if (missile0ScanCounter == 0 && missile0Enabled && !missile0ResetToPlayer) {
			M0 = true;
			if (color == 0) color = missile0Color;
		}
		// Get the value for Player1
		if (player1ScanCounter >= 0) {
			playersPerformDelayedSpriteChanges();
			int sprite = player1VerticalDelay ? player1ActiveSprite : player1DelayedSprite;
			if (sprite != 0)
				if (((sprite >> (player1Reflected ? (7 - player1ScanCounter) : player1ScanCounter)) & 0x01) != 0) {
					P1 = true;
					if (color == 0) color = player1Color;
				}
		}
		if (missile1ScanCounter == 0 && missile1Enabled && !missile1ResetToPlayer) {
			M1 = true;
			if (color == 0) color = missile1Color;
		}
		if (!playfieldPriority) {
			// Get the value for the Ball (low priority)
			if (ballScanCounter == 0) {
				playersPerformDelayedSpriteChanges();		// May trigger Ball delayed enablement
				if (ballEnabled) {
					BL = true;
					if (color == 0) color = ballColor;
				}
			}
			// Get the value for the the PlayField (low priority)
			if (playfieldCurrentPixel) {
				FL = true;
				if (color == 0) color = !playfieldScoreMode ? playfieldColor : (clock < 148 ? player0Color : player1Color);
			}
		}
		// If nothing more is showing, get the PlayField background value (low priority)
		if (color == 0) color = playfieldBackground;	
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
			if (P1) CXM0P |= 0x80;
			if (P0) CXM0P |= 0x40; 
			if (FL) CXM0FB |= 0x80;
			if (BL) CXM0FB |= 0x40; 
		}
		if (M1) {
			if (P0) CXM1P |= 0x80;
			if (P1) CXM1P |= 0x40; 
			if (FL) CXM1FB |= 0x80;
			if (BL) CXM1FB |= 0x40; 
			if (M0) CXPPMM |= 0x40; 
		}
	}

	private void playfieldUpdateCurrentPixel() {
		playfieldPerformDelayedSpriteChange(false);
		if (playfieldPatternInvalid) {
			// Shortcuts if the Playfield is all clear
			if (PF0 == 0 && PF1 == 0 && PF2 == 0) {
				Arrays.fill(playfieldPattern, false);
				playfieldPatternInvalid = false;
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
			playfieldPatternInvalid = false;
		}
		playfieldCurrentPixel = playfieldPattern[(clock - HBLANK_DURATION) / 4 % 40];
	}

	private void playfieldDelaySpriteChange(int part, int sprite) {
		observableChange();
		playfieldPerformDelayedSpriteChange(true);
		playfieldDelayedChangeClock = clock;
		playfieldDelayedChangePart = part;
		playfieldDelayedChangePattern = sprite;
		if (debug) debugPixel(DEBUG_SPECIAL_COLOR2);
	}

	private void playfieldPerformDelayedSpriteChange(boolean force) {
		// Only commits change if there is one and the delay has passed
		if (playfieldDelayedChangePart == -1) return;
		if (!force) {
			int dif = clock - playfieldDelayedChangeClock;
			if (dif >= 0 && dif <= 1) return;
		}
		switch (playfieldDelayedChangePart) {
			case 0:	PF0 = playfieldDelayedChangePattern; break;
			case 1:	PF1 = playfieldDelayedChangePattern; break;
			case 2:	PF2 = playfieldDelayedChangePattern;
		}
		playfieldPatternInvalid = true;
		playfieldDelayedChangePart = -1;		// Marks the delayed change as nothing
	}

	private void playfieldAndBallSetShape(int shape) {
		observableChange();
		boolean reflect = (shape & 0x01) != 0;
		if (playfieldReflected != reflect) {
			playfieldReflected = reflect;
			playfieldPatternInvalid = true;
		}
		playfieldScoreMode = (shape & 0x02) != 0;	// Only if normal priority as per specification???
		playfieldPriority = (shape & 0x04) != 0;
		switch (shape & 0x30) {
			case 0x00:
				ballScanSpeed = 1; break;
			case 0x10:
				ballScanSpeed = 2; break;
			case 0x20:
				ballScanSpeed = 4; break;
			case 0x30:
				ballScanSpeed = 8; break;
		}
	}

	private void objectsTriggerScanCounters() {
		switch (player0Counter) {					// Sets the delay countdown to actually start the scan of each copy
			case 156:
				if (player0RecentResetHit) player0RecentResetHit = false;
				else player0ScanStartCountdown = player0ScanSpeed == 1 ? 5 : 6;		// If Double or Quadruple size, delays 1 additional clock 
				break;
			case 12:
				if (player0CloseCopy) player0ScanStartCountdown = 5;
				break;
			case 28:
				if (player0MediumCopy) player0ScanStartCountdown = 5;
				break;
			case 60:
				if (player0WideCopy) player0ScanStartCountdown = 5;
				break;
		}
		// Actually starts the scans when they should
		if (player0ScanStartCountdown >= 0)
			if (--player0ScanStartCountdown < 0) {
				player0ScanCounter = 7; player0ScanSubCounter = player0ScanSpeed;
			}
		switch (player1Counter) {					// Sets the delay countdown to actually start the scan of each copy
			case 156:
				if (player1RecentResetHit) player1RecentResetHit = false;
				else player1ScanStartCountdown = player1ScanSpeed == 1 ? 5 : 6;		// If Double or Quadruple size, delays 1 additional clock
				break;
			case 12:
				if (player1CloseCopy) player1ScanStartCountdown = 5;
				break;
			case 28:
				if (player1MediumCopy) player1ScanStartCountdown = 5;
				break;
			case 60:
				if (player1WideCopy) player1ScanStartCountdown = 5;
				break;
		}
		// Actually starts the scans when they should
		if (player1ScanStartCountdown >= 0)
			if (--player1ScanStartCountdown < 0) {
				player1ScanCounter = 7;	player1ScanSubCounter = player1ScanSpeed;
			}
		// Missiles and ball dont have delays to start the scan
		switch (missile0Counter) {
			case 0:
				if (missile0RecentResetHit)
					missile0RecentResetHit = false;
				else {
					missile0ScanCounter = 0; missile0ScanSubCounter = missile0ScanSpeed;
				}
				break;
			case 16:
				if (player0CloseCopy) {
					missile0ScanCounter = 0; missile0ScanSubCounter = missile0ScanSpeed;
				}
				break;
			case 32:
				if (player0MediumCopy) {
					missile0ScanCounter = 0; missile0ScanSubCounter = missile0ScanSpeed;
				}
				break;
			case 64:
				if (player0WideCopy) {
					missile0ScanCounter = 0; missile0ScanSubCounter = missile0ScanSpeed;
				}
				break;
		}
		switch (missile1Counter) {
			case 0:
				if (missile1RecentResetHit)
					missile1RecentResetHit = false;
				else {
					missile1ScanCounter = 0; missile1ScanSubCounter = missile1ScanSpeed;
				}
				break;
			case 16:
				if (player1CloseCopy) {
					missile1ScanCounter = 0; missile1ScanSubCounter = missile1ScanSpeed;
				}
				break;
			case 32:
				if (player1MediumCopy) {
					missile1ScanCounter = 0; missile1ScanSubCounter = missile1ScanSpeed;
				}
				break;
			case 64:
				if (player1WideCopy) {
					missile1ScanCounter = 0; missile1ScanSubCounter = missile1ScanSpeed;
				}
				break;
		}
		// The ball does not have copies and does not wait for the next scanline to start even if recently reset
		if (ballCounter == 0) {							
			ballScanCounter = 0; ballScanSubCounter = ballScanSpeed;
		}
	}

	private void objectsIncrementCounters() {
		if (++player0Counter == 160) player0Counter = 0;
		if (player0ScanCounter >= 0) {
			// If missileResetToPlayer is on and the player scan has started the FIRST copy
			if (player0ScanCounter == 7 && missile0ResetToPlayer && player0Counter < 12 )	
				missile0Counter = 156;
			if (player0ScanSpeed == 1) {
				player0ScanCounter--;
			} else {
				if (--player0ScanSubCounter == 0) {
					player0ScanCounter--; player0ScanSubCounter = player0ScanSpeed;
				}
			}
		}
		if (++player1Counter == 160) player1Counter = 0;
		if (player1ScanCounter >= 0) {
			// If missileResetToPlayer is on and the player scan has started the FIRST copy
			if (player1ScanCounter == 7 && missile1ResetToPlayer && player1Counter < 12 )	
				missile1Counter = 156;
			if (player1ScanSpeed == 1) {
				player1ScanCounter--;
			} else {
				if (--player1ScanSubCounter == 0) {
					player1ScanCounter--; player1ScanSubCounter = player1ScanSpeed;
				}
			}
		}
		if (++missile0Counter == 160) missile0Counter = 0;
		if (missile0ScanCounter >= 0) {
			if (missile0ScanSpeed == 1) {
				missile0ScanCounter--;
			} else {
				if (--missile0ScanSubCounter == 0) {
					missile0ScanCounter--; missile0ScanSubCounter = missile0ScanSpeed;
				}
			}
		}
		if (++missile1Counter == 160) missile1Counter = 0;
		if (missile1ScanCounter >= 0) {
			if (missile1ScanSpeed == 1) {
				missile1ScanCounter--;
			} else {
				if (--missile1ScanSubCounter == 0) {
					missile1ScanCounter--; missile1ScanSubCounter = missile1ScanSpeed;
				}
			}
		}
		if (++ballCounter == 160) ballCounter = 0;
		if (ballScanCounter >= 0) {
			if (ballScanSpeed == 1) {
				ballScanCounter--;
			} else {
				if (--ballScanSubCounter == 0) {
					ballScanCounter--; ballScanSubCounter = ballScanSpeed;
				}
			}
		}
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
		repeatLastLine = false;	
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
			switch (change[1]) {
				case 0: 
					player0DelayedSprite = change[2];
					player1ActiveSprite = player1DelayedSprite; 
					break;
				case 1: 
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
		if (!ballVerticalDelay)
			ballEnabled = ballDelayedEnablement;
	}

	private void player0SetShape(int shape) {
		observableChange();
		switch (shape & 0x30) {
			case 0x00:
				missile0ScanSpeed = 1; break;
			case 0x10:
				missile0ScanSpeed = 2; break;
			case 0x20:
				missile0ScanSpeed = 4; break;
			case 0x30:
				missile0ScanSpeed = 8; break;
		}
		if ((shape & 0x07) == 0x05) {
			player0ScanSubCounter = player0ScanSpeed = 2;
			player0CloseCopy = player0MediumCopy = player0WideCopy = false;
			return;
		}
		if ((shape & 0x07) == 0x07) {
			player0ScanSubCounter = player0ScanSpeed = 4;
			player0CloseCopy = player0MediumCopy = player0WideCopy = false;
			return;
		}
		player0ScanSubCounter = player0ScanSpeed = 1;
		player0CloseCopy = (shape & 0x01) != 0;  
		player0MediumCopy = (shape & 0x02) != 0;  
		player0WideCopy = (shape & 0x04) != 0;  
	}

	private void player1SetShape(int shape) {
		observableChange();
		switch (shape & 0x30) {
			case 0x00:
				missile1ScanSpeed = 1; break;
			case 0x10:
				missile1ScanSpeed = 2; break;
			case 0x20:
				missile1ScanSpeed = 4; break;
			case 0x30:
				missile1ScanSpeed = 8; break;
		}
		if ((shape & 0x07) == 0x05) {
			player1ScanSubCounter = player1ScanSpeed = 2;
			player1CloseCopy = player1MediumCopy = player1WideCopy = false;
			return;
		}
		if ((shape & 0x07) == 0x07) {
			player1ScanSubCounter = player1ScanSpeed = 4;
			player1CloseCopy = player1MediumCopy = player1WideCopy = false;
			return;
		}
		player1ScanSubCounter = player1ScanSpeed = 1;
		player1CloseCopy = (shape & 0x01) != 0;  
		player1MediumCopy = (shape & 0x02) != 0;  
		player1WideCopy = (shape & 0x04) != 0;  
	}

	private void hitHMOVE() {
		// Only if needed
		if (clock >= HBLANK_DURATION && clock < 210) return;		// 210 is maybe the minimum clock to hit HMOVE for effect in the next line
		if (clock < HBLANK_DURATION)
			hMoveHitBlank = true;
		else
			hMoveHitBlank = false;
		if (debug) debugPixel(DEBUG_HMOVE_COLOR);
		int add;
		boolean inv = false;
		add = (hMoveHitBlank ? HMP0 : HMP0 + 8); if (add != 0) { player0Counter += add; if (player0Counter > 159) player0Counter -= 160; inv = true; }
		add = (hMoveHitBlank ? HMM0 : HMM0 + 8); if (add != 0) { missile0Counter += add; if (missile0Counter > 159) missile0Counter -= 160; inv = true; }
		add = (hMoveHitBlank ? HMP1 : HMP1 + 8); if (add != 0) { player1Counter +=  add; if (player1Counter > 159) player1Counter -= 160; inv = true; }
		add = (hMoveHitBlank ? HMM1 : HMM1 + 8); if (add != 0) { missile1Counter += add; if (missile1Counter > 159) missile1Counter -= 160; inv = true; }
		add = (hMoveHitBlank ? HMBL : HMBL + 8); if (add != 0) { ballCounter += add; if (ballCounter > 159) ballCounter -= 160; inv = true; }
		if (inv) observableChange();
	}	
	
	private void hitRESP0() {										// TODO Is this 158 wrong? Seems to work that way...
		observableChange();
		player0RecentResetHit = player0Counter != 156;
		player0Counter = clock < HBLANK_DURATION ? 158 : 156; 	
		if (debug) debugPixel(DEBUG_P0_RES_COLOR);
	}
	
	private void hitRESP1() {
		observableChange();
		player1RecentResetHit = player1Counter != 156;
		player1Counter = clock < HBLANK_DURATION ? 158 : 156; 
		if (debug) debugPixel(DEBUG_P1_RES_COLOR);
	}
	
	private void hitRESM0() {
		observableChange();
		missile0Counter = clock < HBLANK_DURATION ? 158 : 156; 
		missile0RecentResetHit = true;
		if (debug) debugPixel(DEBUG_M0_COLOR);
	}
	
	private void hitRESM1() {
		observableChange();
		missile1Counter = clock < HBLANK_DURATION ? 158 : 156; 
		missile1RecentResetHit = true;
		if (debug) debugPixel(DEBUG_M1_COLOR);
	}
	
	private void hitRESBL() {
		observableChange();
		ballCounter = clock < HBLANK_DURATION ? 158 : 156;				 
		if (debug) debugPixel(DEBUG_BL_COLOR);
	}
	
	private void missile0SetResetToPlayer(int res) {
		observableChange();
		if (missile0ResetToPlayer = (res & 0x02) != 0)
			missile0Enabled = false;
	}

	private void missile1SetResetToPlayer(int res) {
		observableChange();
		if (missile1ResetToPlayer = (res & 0x02) != 0)
			missile1Enabled = false;
	}

	private void vBlankSet(int blank) {
		observableChange();
		vBlankOn = (blank & 0x02) != 0;
		if ((blank & 0x40) != 0) {				// Enable latches
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

	private void debug(int level) {
		debugLevel = level > 4 ? 0 : level;
		debug = debugLevel != 0;
		cpu.debug = debug;
		pia.debug = debug;
		hBlankColor = debugLevel >= 2 ? DEBUG_HBLANK_COLOR : HBLANK_COLOR;
		vBlankColor = debugLevel >= 2 ? DEBUG_VBLANK_COLOR : VBLANK_COLOR;
		if (debug)
			debugSetColors();
		else {
			Arrays.fill(linePixels, hBlankColor);
			observableChange();
		}
	}

	private void debugSetColors() {
		player0Color = DEBUG_P0_COLOR;
		player1Color = DEBUG_P1_COLOR;
		missile0Color = DEBUG_M0_COLOR;
		missile1Color = DEBUG_M1_COLOR;
		ballColor = DEBUG_BL_COLOR;
		playfieldColor = DEBUG_PF_COLOR;
		playfieldBackground = DEBUG_BK_COLOR;
	}

	private void debugInfo(String str) {
		if (debug) {
			System.out.printf("Line: %3d, Pixel: %3d, " + str + "\n", videoOutput.monitor.currentLine(), clock);
			// System.out.println(cpu.printState());
			// System.out.println(cpu.printMemory(0x0090, 32));
		}
	}
	
	private void debugPixel(int color) {
		debugPixels[clock] = color;
	}
	
	private void processDebugPixelsInLine() {
		Arrays.fill(linePixels, 0, HBLANK_DURATION, hBlankColor);
		if (debugLevel >= 4 && videoOutput.monitor.currentLine() % 10 == 0)
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
		switch(address & READ_ADDRESS_MASK) {
			case 0x00:	return (byte) CXM0P;
			case 0x01:	return (byte) CXM1P;
			case 0x02:	return (byte) CXP0FB;
			case 0x03:	return (byte) CXP1FB;
			case 0x04:	return (byte) CXM0FB;
			case 0x05:	return (byte) CXM1FB;
			case 0x06:	return (byte) CXBLPF;
			case 0x07:	return (byte) CXPPMM;
			case 0x08:	return (byte) INPT0;
			case 0x09:	return (byte) INPT1;
			case 0x0A:	return (byte) INPT2;
			case 0x0B:	return (byte) INPT3;
			case 0x0C:	return (byte) INPT4;
			case 0x0D:	return (byte) INPT5;
			case 0x0E:	// Register DOES NOT EXIST
			case 0x0F:	// Register DOES NOT EXIST
			default:	debugInfo(String.format("Invalid TIA read register address: %04x", address)); return 0;
		}
	}	

	@Override
	public int unsignedByte(int address) {
		return readByte(address) & 0xff;
	}	

	@Override
	public void writeByte(int address, byte b) {
		int i = b & 0xff;
		switch(address & WRITE_ADDRESS_MASK) {
			case 0x00:	VSYNC  = i; observableChange(); vSyncOn = (i & 0x02) != 0; return;
			case 0x01:	VBLANK = i; vBlankSet(i); return;
			case 0x02:	WSYNC  = i; cpu.RDY = false; if (debug) debugPixel(DEBUG_WSYNC_COLOR); return;		// <STROBE> Halts the CPU until the next HBLANK
			case 0x03:	RSYNC  = i; /* clock = 0; */ return;
			case 0x04:	NUSIZ0 = i; player0SetShape(i); return;
			case 0x05:	NUSIZ1 = i; player1SetShape(i); return;
			case 0x06:	COLUP0 = i; observableChange(); if (!debug) player0Color = missile0Color = palette[i]; return;
			case 0x07:	COLUP1 = i; observableChange(); if (!debug) player1Color = missile1Color = palette[i]; return;
			case 0x08:	COLUPF = i; observableChange(); if (!debug) playfieldColor = ballColor = palette[i]; return;
			case 0x09:	COLUBK = i; observableChange(); if (!debug) playfieldBackground = palette[i]; return;
			case 0x0A:	CTRLPF = i; playfieldAndBallSetShape(i); return;
			case 0x0B:	REFP0  = i; observableChange(); player0Reflected = (i & 0x08) != 0; return;
			case 0x0C:	REFP1  = i; observableChange(); player1Reflected = (i & 0x08) != 0; return;
			case 0x0D:	if (PF0 != i || playfieldDelayedChangePart == 0) playfieldDelaySpriteChange(0, i); return;
			case 0x0E:	if (PF1 != i || playfieldDelayedChangePart == 1) playfieldDelaySpriteChange(1, i); return;
			case 0x0F:	if (PF2 != i || playfieldDelayedChangePart == 2) playfieldDelaySpriteChange(2, i); return;
			case 0x10:	RESP0  = i; hitRESP0(); return;
			case 0x11:	RESP1  = i; hitRESP1(); return;
			case 0x12:	RESM0  = i; hitRESM0(); return;
			case 0x13:	RESM1  = i; hitRESM1(); return;
			case 0x14:	RESBL  = i; hitRESBL(); return;
			case 0x15:	AUDC0  = i; audioOutput.channel0().setControl(i & 0x0f); return;
			case 0x16:	AUDC1  = i; audioOutput.channel1().setControl(i & 0x0f); return;
			case 0x17:	AUDF0  = i; audioOutput.channel0().setDivider((i & 0x1f) + 1); return;		// Bits 0-4, Divider from 1 to 32 )
			case 0x18:	AUDF1  = i; audioOutput.channel1().setDivider((i & 0x1f) + 1); return;		// Bits 0-4, Divider from 1 to 32 )
			case 0x19:	AUDV0  = i; audioOutput.channel0().setVolume(i & 0x0f); return;				// Bits 0-3, Volume from 0 to 15 )
			case 0x1A:	AUDV1  = i; audioOutput.channel1().setVolume(i & 0x0f); return;				// Bits 0-3, Volume from 0 to 15 )
			case 0x1B:	GRP0   = i; playerDelaySpriteChange(0, i); return;
			case 0x1C:	GRP1   = i; playerDelaySpriteChange(1, i); return;
			case 0x1D:	ENAM0  = i; observableChange(); missile0Enabled = (i & 0x02) != 0; return;
			case 0x1E:	ENAM1  = i; observableChange(); missile1Enabled = (i & 0x02) != 0; return;
			case 0x1F:	ENABL  = i; ballSetGraphic(i); return;
			case 0x20:	HMP0   = (b >> 4); return;
			case 0x21:	HMP1   = (b >> 4); return;
			case 0x22:	HMM0   = (b >> 4); return;
			case 0x23:	HMM1   = (b >> 4); return;
			case 0x24:	HMBL   = (b >> 4); return;
			case 0x25:	VDELP0 = i; observableChange(); player0VerticalDelay = (i & 0x01) != 0; return;
			case 0x26:	VDELP1 = i; observableChange(); player1VerticalDelay = (i & 0x01) != 0; return;
			case 0x27:	VDELBL = i; observableChange(); ballVerticalDelay = (i & 0x01) != 0; return;
			case 0x28:	RESMP0 = i; missile0SetResetToPlayer(i); return;
			case 0x29:	RESMP1 = i; missile1SetResetToPlayer(i); return;
			case 0x2A:	HMOVE  = i; hitHMOVE();	return;						   	
			case 0x2B:	HMCLR  = i; HMP0 = HMP1 = HMM0 = HMM1 = HMBL = 0; return;
			case 0x2C:	CXCLR  = i; observableChange(); CXM0P = CXM1P = CXP0FB = CXP1FB = CXM0FB = CXM1FB = CXBLPF = CXPPMM = 0; return;
			case 0x2D:	// Register DOES NOT EXIST
			case 0x2E:	// Register DOES NOT EXIST
			case 0x2F:	// Register DOES NOT EXIST
			default:	debugInfo(String.format("Invalid TIA write register address: %04x value %d", address, b)); 
		}
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
			case DEBUG_NO_COLLISIONS:
				debugNoCollisions = !debugNoCollisions; return;
			case PAUSE:
				debugPause = !debugPause; debugPauseMoreFrames = 0; return;
			case FRAME:
				debugPauseMoreFrames++; return;
			case TRACE:
				cpu.trace = !cpu.trace; return;
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
		state.debug                       	   =  debug;
		state.debugLevel                       =  debugLevel;
		state.debugNoCollisions           	   =  debugNoCollisions;
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
		state.player0RecentResetHit       	   =  player0RecentResetHit;      
		state.player0Counter	          	   =  player0Counter;	         
		state.player0ScanStartCountdown   	   =  player0ScanStartCountdown;	 
		state.player0ScanCounter	      	   =  player0ScanCounter;	     
		state.player0ScanSpeed            	   =  player0ScanSpeed;           
		state.player0ScanSubCounter       	   =  player0ScanSubCounter;      
		state.player0VerticalDelay        	   =  player0VerticalDelay;       
		state.player0CloseCopy            	   =  player0CloseCopy;           
		state.player0MediumCopy           	   =  player0MediumCopy;
		state.player0WideCopy             	   =  player0WideCopy;            
		state.player0Reflected            	   =  player0Reflected;
		state.player1ActiveSprite         	   =  player1ActiveSprite;        
		state.player1DelayedSprite        	   =  player1DelayedSprite;
		state.player1Color                	   =  player1Color;               
		state.player1RecentResetHit       	   =  player1RecentResetHit;      
		state.player1Counter              	   =  player1Counter;             
		state.player1ScanStartCountdown   	   =  player1ScanStartCountdown;										
		state.player1ScanCounter		  	   =  player1ScanCounter;						
		state.player1ScanSpeed			  	   =  player1ScanSpeed;						
		state.player1ScanSubCounter		  	   =  player1ScanSubCounter;					
		state.player1VerticalDelay        	   =  player1VerticalDelay;       
		state.player1CloseCopy            	   =  player1CloseCopy;           
		state.player1MediumCopy           	   =  player1MediumCopy;
		state.player1WideCopy             	   =  player1WideCopy;            
		state.player1Reflected            	   =  player1Reflected;
		state.missile0Enabled             	   =  missile0Enabled;            
		state.missile0Color               	   =  missile0Color;              
		state.missile0RecentResetHit      	   =  missile0RecentResetHit;     
		state.missile0Counter             	   =  missile0Counter;            
		state.missile0ScanCounter         	   =  missile0ScanCounter;        
		state.missile0ScanSpeed			  	   =  missile0ScanSpeed;						
		state.missile0ScanSubCounter	  	   =  missile0ScanSubCounter;					
		state.missile0ResetToPlayer		  	   =  missile0ResetToPlayer;					
		state.missile1Enabled             	   =  missile1Enabled;            
		state.missile1Color               	   =  missile1Color;              
		state.missile1RecentResetHit      	   =  missile1RecentResetHit;     
		state.missile1Counter             	   =  missile1Counter;            
		state.missile1ScanCounter         	   =  missile1ScanCounter;        
		state.missile1ScanSpeed			  	   =  missile1ScanSpeed;						
		state.missile1ScanSubCounter	  	   =  missile1ScanSubCounter;					
		state.missile1ResetToPlayer		  	   =  missile1ResetToPlayer;					
		state.ballEnabled                 	   =  ballEnabled;                
		state.ballDelayedEnablement       	   =  ballDelayedEnablement;      
		state.ballColor                   	   =  ballColor;                  
		state.ballCounter                 	   =  ballCounter;                
		state.ballScanCounter             	   =  ballScanCounter;            
		state.ballScanSpeed				  	   =  ballScanSpeed;						
		state.ballScanSubCounter		  	   =  ballScanSubCounter;					
		state.ballVerticalDelay           	   =  ballVerticalDelay;          
		state.playfieldDelayedChangeClock	   =  playfieldDelayedChangeClock;
		state.playfieldDelayedChangePart	   =  playfieldDelayedChangePart;
		state.playfieldDelayedChangePattern	   =  playfieldDelayedChangePattern;
		state.playersDelayedSpriteChanges      =  Array2DCopy.copy(playersDelayedSpriteChanges);
		state.playersDelayedSpriteChangesCount =  playersDelayedSpriteChangesCount;
		state.controlsButtonsLatched      	   =  controlsButtonsLatched;     
		state.controlsJOY0ButtonPressed   	   =  controlsJOY0ButtonPressed;  
		state.controlsJOY1ButtonPressed   	   =  controlsJOY1ButtonPressed;  
		state.paddle0Position				   =  paddle0Position;
		state.paddle0CapacitorCharge 		   =  paddle0CapacitorCharge;
		state.paddle1Position				   =  paddle1Position;
		state.paddle1CapacitorCharge 		   =  paddle1CapacitorCharge;
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
		state.INPT0 					  	   =  INPT0;
		state.INPT1 					  	   =  INPT1;
		state.INPT2 					  	   =  INPT2;
		state.INPT3 					  	   =  INPT3;
		state.INPT4 					  	   =  INPT4;
		state.INPT5 					  	   =  INPT5;
		return state;
	}

	public void loadState(TIAState state) {
//		debug                       	 =  state.debug;				// Keep the current debug modes
//		debugLevel                     	 =  state.debugLevel;
//		debugNoCollisions           	 =  state.debugNoCollisions;
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
		player0RecentResetHit       	 =  state.player0RecentResetHit;       
		player0Counter	            	 =  state.player0Counter;	          
		player0ScanStartCountdown		 =  state.player0ScanStartCountdown;  
		player0ScanCounter	        	 =  state.player0ScanCounter;	      
		player0ScanSpeed            	 =  state.player0ScanSpeed;            
		player0ScanSubCounter       	 =  state.player0ScanSubCounter;       
		player0VerticalDelay        	 =  state.player0VerticalDelay;        
		player0CloseCopy            	 =  state.player0CloseCopy;            
		player0MediumCopy           	 =  state.player0MediumCopy;
		player0WideCopy             	 =  state.player0WideCopy;             
		player0Reflected            	 =  state.player0Reflected;
		player1ActiveSprite         	 =  state.player1ActiveSprite;         
		player1DelayedSprite        	 =  state.player1DelayedSprite;
		player1Color                	 =  state.player1Color;                
		player1RecentResetHit       	 =  state.player1RecentResetHit;       
		player1Counter              	 =  state.player1Counter;              
		player1ScanStartCountdown		 =  state.player1ScanStartCountdown; 							
		player1ScanCounter				 =  state.player1ScanCounter;		 	
		player1ScanSpeed				 =  state.player1ScanSpeed;			 	
		player1ScanSubCounter			 =  state.player1ScanSubCounter;		 	
		player1VerticalDelay        	 =  state.player1VerticalDelay;        
		player1CloseCopy            	 =  state.player1CloseCopy;            
		player1MediumCopy           	 =  state.player1MediumCopy;
		player1WideCopy             	 =  state.player1WideCopy;             
		player1Reflected            	 =  state.player1Reflected;
		missile0Enabled             	 =  state.missile0Enabled;             
		missile0Color               	 =  state.missile0Color;               
		missile0RecentResetHit      	 =  state.missile0RecentResetHit;      
		missile0Counter             	 =  state.missile0Counter;             
		missile0ScanCounter         	 =  state.missile0ScanCounter;         
		missile0ScanSpeed				 =  state.missile0ScanSpeed;			 	
		missile0ScanSubCounter			 =  state.missile0ScanSubCounter;	 	
		missile0ResetToPlayer			 =  state.missile0ResetToPlayer;		 	
		missile1Enabled             	 =  state.missile1Enabled;             
		missile1Color               	 =  state.missile1Color;               
		missile1RecentResetHit      	 =  state.missile1RecentResetHit;      
		missile1Counter             	 =  state.missile1Counter;             
		missile1ScanCounter         	 =  state.missile1ScanCounter;         
		missile1ScanSpeed				 =  state.missile1ScanSpeed;			 	
		missile1ScanSubCounter			 =  state.missile1ScanSubCounter;	 	
		missile1ResetToPlayer			 =  state.missile1ResetToPlayer;		 	
		ballEnabled                 	 =  state.ballEnabled;                 
		ballDelayedEnablement       	 =  state.ballDelayedEnablement;       
		ballColor                   	 =  state.ballColor;                   
		ballCounter                 	 =  state.ballCounter;                 
		ballScanCounter             	 =  state.ballScanCounter;             
		ballScanSpeed					 =  state.ballScanSpeed;				 
		ballScanSubCounter				 =  state.ballScanSubCounter;		 
		ballVerticalDelay           	 =  state.ballVerticalDelay;           
		playfieldDelayedChangeClock		 =  state.playfieldDelayedChangeClock;
		playfieldDelayedChangePart		 =  state.playfieldDelayedChangePart;
		playfieldDelayedChangePattern	 =  state.playfieldDelayedChangePattern;
		playersDelayedSpriteChanges      =  state.playersDelayedSpriteChanges;      
		playersDelayedSpriteChangesCount =  state.playersDelayedSpriteChangesCount; 
		controlsButtonsLatched   		 =  state.controlsButtonsLatched;      
		// controlsJOY0ButtonPressed	 =  state.controlsJOY0ButtonPressed;	// Do not load controls state
		// controlsJOY1ButtonPressed	 =  state.controlsJOY1ButtonPressed;
		// paddle0Position				 =  state.paddle0Position;
		// paddle0CapacitorCharge 		 =  state.paddle0CapacitorCharge;
		// paddle1Position				 =  state.paddle1Position;
		// paddle1CapacitorCharge 		 =  state.paddle1CapacitorCharge;
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
		// INPT0 					 	 =	state.INPT0;	// Do not load controls state
		// INPT1 					 	 =	state.INPT1;
		// INPT2 					 	 =	state.INPT2;
		// INPT3 					 	 =	state.INPT3;
		// INPT4 						 =	state.INPT4;
		// INPT5 				 		 =	state.INPT5;
		if (debug) debugSetColors();						// IF debug is on, ensure debug colors are used
	}
	
	
	// Variables ----------------------------------------------
	
	private final VideoGenerator videoOutput;
	private final AudioGenerator audioOutput;
	
	private int clock = 0;

	private final M6502 cpu;
	private final PIA pia;

	private boolean powerOn = false;
	private final int debugPixels[] = new int[LINE_WIDTH];
	
	private int[] palette;
	private int vBlankColor = 0xff000000;
	private int vSyncColor = 0xffdddddd;
	private int hBlankColor = 0xff000000;

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
	private boolean hMoveHitBlank = false;
	
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
	private boolean player0RecentResetHit = false;
	private int player0Counter = 0;							// Position!	
	private int player0ScanStartCountdown = -1;				// Delay until the scan actually starts						
	private int player0ScanCounter = -1;					// Current scan position; Negative means scan not happening	
	private int player0ScanSpeed = 1;						// Scan (decrement ScanCounter) only at each X cycles
	private int player0ScanSubCounter = 1;					// Use to count the X cycles above
	private boolean player0VerticalDelay = false;
	private boolean player0CloseCopy = false;
	private boolean player0MediumCopy = false;
	private boolean player0WideCopy = false;
	private boolean player0Reflected = false;
	
	private int player1ActiveSprite = 0;
	private int player1DelayedSprite = 0;
	private int player1Color = 0xff000000;
	private boolean player1RecentResetHit = false;
	private int player1Counter = 0;
	private int player1ScanStartCountdown = -1;										
	private int player1ScanCounter = -1;
	private int player1ScanSpeed = 1;
	private int player1ScanSubCounter = 1;
	private boolean player1VerticalDelay = false;
	private boolean player1CloseCopy = false;
	private boolean player1MediumCopy = false;
	private boolean player1WideCopy = false;
	private boolean player1Reflected = false;
	
	private boolean missile0Enabled = false;
	private int missile0Color = 0xff000000;
	private boolean missile0RecentResetHit = false;
	private int missile0Counter = 0;
	private int missile0ScanCounter = -1;
	private int missile0ScanSpeed = 1;						
	private int missile0ScanSubCounter = 1;					
	private boolean missile0ResetToPlayer = false;					

	private boolean missile1Enabled = false;
	private int missile1Color = 0xff000000;
	private boolean missile1RecentResetHit = false;
	private int missile1Counter = 0;
	private int missile1ScanCounter = -1;
	private int missile1ScanSpeed = 1;						
	private int missile1ScanSubCounter = 1;					
	private boolean missile1ResetToPlayer = false;					
	
	private boolean ballEnabled = false;
	private boolean ballDelayedEnablement = false;
	private int ballColor = 0xff000000;
	private int ballCounter = 0;
	private int ballScanCounter = -1;
	private int ballScanSpeed = 1;						
	private int ballScanSubCounter = 1;					
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

	private int VSYNC;		// ......1.  vertical sync set-clear
	private int VBLANK;		// 11....1.  vertical blank set-clear
	private int WSYNC;		// <strobe>  wait for leading edge of horizontal blank
	private int RSYNC;		// <strobe>  reset horizontal sync counter
	private int NUSIZ0;		// ..111111  number-size player-missile 0
	private int NUSIZ1;		// ..111111  number-size player-missile 1
	private int COLUP0;		// 1111111.  color-lum player 0 and missile 0
	private int COLUP1;		// 1111111.  color-lum player 1 and missile 1
	private int COLUPF;		// 1111111.  color-lum playfield and ball
	private int COLUBK;		// 1111111.  color-lum background
	private int CTRLPF;		// ..11.111  control playfield ball size & collisions
	private int REFP0;		// ....1...  reflect player 0
	private int REFP1;		// ....1...  reflect player 1
	private int PF0;		// 1111....  playfield register byte 0
	private int PF1;		// 11111111  playfield register byte 1
	private int PF2;		// 11111111  playfield register byte 2
	private int RESP0;		// <strobe>  reset player 0
	private int RESP1;		// <strobe>  reset player 1
	private int RESM0;		// <strobe>  reset missile 0
	private int RESM1;		// <strobe>  reset missile 1
	private int RESBL;		// <strobe>  reset ball
	private int AUDC0;		// ....1111  audio control 0
	private int AUDC1;		// ....1111  audio control 1
	private int AUDF0;		// ...11111  audio frequency 0
	private int AUDF1;		// ...11111  audio frequency 1
	private int AUDV0;		// ....1111  audio volume 0
	private int AUDV1;		// ....1111  audio volume 1
	private int GRP0;		// 11111111  graphics player 0
	private int GRP1;		// 11111111  graphics player 1
	private int ENAM0;		// ......1.  graphics (enable) missile 0
	private int ENAM1;		// ......1.  graphics (enable) missile 1
	private int ENABL;		// ......1.  graphics (enable) ball
	private int HMP0;		// 1111....  horizontal motion player 0
	private int HMP1;		// 1111....  horizontal motion player 1
	private int HMM0;		// 1111....  horizontal motion missile 0
	private int HMM1;		// 1111....  horizontal motion missile 1
	private int HMBL;		// 1111....  horizontal motion ball
	private int VDELP0;		// .......1  vertical delay player 0
	private int VDELP1;		// .......1  vertical delay player 1
	private int VDELBL;		// .......1  vertical delay ball
	private int RESMP0;		// ......1.  reset missile 0 to player 0
	private int RESMP1;		// ......1.  reset missile 1 to player 1
	private int HMOVE;		// <strobe>  apply horizontal motion
	private int HMCLR;		// <strobe>  clear horizontal motion registers
	private int CXCLR;		// <strobe>  clear collision latches


	// Constants --------------------------------------------------
	
	private static final int HBLANK_COLOR = 0xff000000;
	private static final int VBLANK_COLOR = 0xff000000;

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

	private static final int DEBUG_PF_COLOR  = 0xff336633;
	private static final int DEBUG_BK_COLOR  = 0xff000000;
	private static final int DEBUG_BL_COLOR  = 0xffffff00;

	private static final int DEBUG_SPECIAL_COLOR  = 0xff00ffff;
	private static final int DEBUG_SPECIAL_COLOR2  = 0xff00ff00;

	private static final int READ_ADDRESS_MASK = 0x000f;
	private static final int WRITE_ADDRESS_MASK = 0x003f;
	
	private static final int PLAYERS_DELAYED_SPRITE_GHANGES_MAX_COUNT = 50;  // Supports a maximum of player GR changes before any is drawn
	
	public static final boolean SYNC_WITH_AUDIO_MONITOR = Parameters.TIA_SYNC_WITH_AUDIO_MONITOR;
	public static final boolean SYNC_WITH_VIDEO_MONITOR = Parameters.TIA_SYNC_WITH_VIDEO_MONITOR;
	
	public static final double FORCED_CLOCK = Parameters.TIA_FORCED_CLOCK;		//  TIA Real Clock = NTSC clock = 3584160 or 3579545 Hz

	public static final double DEFAUL_CLOCK_NTSC = Parameters.TIA_DEFAULT_CLOCK_NTSC;		
	public static final double DEFAUL_CLOCK_PAL = Parameters.TIA_DEFAULT_CLOCK_PAL;		


	// Used to save/load states
	public static class TIAState implements Serializable {
		boolean debug;
		int debugLevel;
		boolean debugNoCollisions;
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
		boolean player0RecentResetHit;
		int player0Counter;	
		int player0ScanStartCountdown;	
		int player0ScanCounter;	
		int player0ScanSpeed;
		int player0ScanSubCounter;
		boolean player0VerticalDelay;
		boolean player0CloseCopy;
		boolean player0MediumCopy;
		boolean player0WideCopy;
		boolean player0Reflected;
		int player1ActiveSprite;
		int player1DelayedSprite;
		int player1Color;
		boolean player1RecentResetHit;
		int player1Counter;
		int player1ScanStartCountdown;										
		int player1ScanCounter;						
		int player1ScanSpeed;						
		int player1ScanSubCounter;					
		boolean player1VerticalDelay;
		boolean player1CloseCopy;
		boolean player1MediumCopy;
		boolean player1WideCopy;
		boolean player1Reflected;
		boolean missile0Enabled;
		int missile0Color;
		boolean missile0RecentResetHit;
		int missile0Counter;
		int missile0ScanCounter;
		int missile0ScanSpeed;						
		int missile0ScanSubCounter;					
		boolean missile0ResetToPlayer;					
		boolean missile1Enabled;
		int missile1Color;
		boolean missile1RecentResetHit;
		int missile1Counter;
		int missile1ScanCounter;
		int missile1ScanSpeed;						
		int missile1ScanSubCounter;					
		boolean missile1ResetToPlayer;					
		boolean ballEnabled;
		boolean ballDelayedEnablement;
		int ballColor;
		int ballCounter;
		int ballScanCounter;
		int ballScanSpeed;						
		int ballScanSubCounter;					
		boolean ballVerticalDelay;
		int playfieldDelayedChangeClock;
		int playfieldDelayedChangePart;
		int playfieldDelayedChangePattern;
		int[][] playersDelayedSpriteChanges;
		int playersDelayedSpriteChangesCount;
		boolean controlsButtonsLatched;
		boolean controlsJOY0ButtonPressed;
		boolean controlsJOY1ButtonPressed;
		int paddle0Position;
		int paddle0CapacitorCharge;
		int paddle1Position;
		int paddle1CapacitorCharge;
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
		int INPT0; 
		int INPT1; 
	    int INPT2; 
		int INPT3; 
		int INPT4; 
		int INPT5; 

		public static final long serialVersionUID = 2L;
	}

}

