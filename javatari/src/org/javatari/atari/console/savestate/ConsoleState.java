// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.console.savestate;

import java.io.Serializable;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.pia.PIA.PIAState;
import org.javatari.atari.pia.RAM.RAMState;
import org.javatari.atari.tia.TIA.TIAState;
import org.javatari.general.av.video.VideoStandard;
import org.javatari.general.m6502.M6502.M6502State;


public final class ConsoleState implements Serializable {

	public ConsoleState(TIAState tia, PIAState pia, RAMState ram, M6502State cpu, Cartridge cartridge, VideoStandard videoStandard) {
		this.tiaState = tia;
		this.piaState = pia;
		this.ramState = ram;
		this.cpuState = cpu;
		this.cartridge = cartridge;
		this.videoStandard = videoStandard;
	}

	public TIAState tiaState;
	public PIAState piaState;
	public RAMState ramState;
	public M6502State cpuState;
	public Cartridge cartridge;
	public VideoStandard videoStandard;


	public static final long serialVersionUID = 2L;

}
