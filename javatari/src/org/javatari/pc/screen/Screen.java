// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.screen;


import java.awt.Component;

import org.javatari.atari.cartridge.CartridgeSocket;
import org.javatari.atari.controls.ConsoleControlsSocket;
import org.javatari.general.av.video.VideoSignal;


public interface Screen {

	public void connect(VideoSignal videoSignal, ConsoleControlsSocket controlsSocket, CartridgeSocket cartridgeSocket);

	public Monitor monitor();

	public Component[] controlsInputComponents();
	
	public void powerOn();
	public void powerOff();
	public void destroy();

}