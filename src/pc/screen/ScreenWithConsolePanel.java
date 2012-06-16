// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.screen;

import general.av.video.VideoSignal;

import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

import pc.controls.AWTConsoleControls;
import atari.cartridge.CartridgeSocket;
import atari.controls.ConsoleControlsSocket;

public class ScreenWithConsolePanel extends JPanel {

	public ScreenWithConsolePanel(VideoSignal videoSignal, ConsoleControlsSocket controlsSocket, CartridgeSocket cartridgeSocket, boolean screenFixedSize, boolean showConsolePanel) throws HeadlessException {
		super();
		screenPanel = new ScreenPanel(videoSignal, cartridgeSocket);
		screenPanel.screen().setFixedSize(screenFixedSize);
		screenPanel.screen().addControlInputComponent(this);
		if (showConsolePanel) consolePanel = new ConsolePanel(controlsSocket, screenPanel.screen());
		AWTConsoleControls consoleControls = new AWTConsoleControls(controlsSocket, screenPanel.screen());
		consoleControls.addInputComponents(screenPanel, this);
		setup();
	}
	
	public void powerOn() {
		screenPanel.powerOn();
	}
	
	public void powerOff() {
		screenPanel.powerOff();
	}
	
	private void setup() {
		screenPanel.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (e.getComponent() == screenPanel) validate();
			}});		
		setLayout(new FlowLayout(
				FlowLayout.CENTER,
				0, 0
		));
		setOpaque(false);
		add(screenPanel);
		if (consolePanel != null) add(consolePanel);
		validate();
	}

	public ScreenPanel screenPanel;
	public ConsolePanel consolePanel;


	private static final long serialVersionUID = 1L;

}
