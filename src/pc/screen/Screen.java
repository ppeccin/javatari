// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.screen;

import general.av.video.VideoSignal;

import java.awt.Component;

import atari.cartridge.CartridgeSocket;
import atari.controls.ConsoleControlsSocket;

public interface Screen {

	public void connect(VideoSignal videoSignal, ConsoleControlsSocket controlsSocket, CartridgeSocket cartridgeSocket);

	public Monitor monitor();

	public Component[] controlsInputComponents();
	
	public void powerOn();
	public void powerOff();
	public void destroy();

}