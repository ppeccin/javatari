// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.screen;


import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;

import org.javatari.atari.cartridge.CartridgeSocket;
import org.javatari.atari.controls.ConsoleControlsSocket;
import org.javatari.general.av.video.VideoSignal;
import org.javatari.parameters.Parameters;



public final class PanelScreen extends JPanel implements Screen {

	public PanelScreen(boolean screenFixedSize) {
		super();
		monitorPanel = new MonitorPanel();
		monitorPanel.monitor().setFixedSize(screenFixedSize);
		monitorPanel.monitor().addControlInputComponents(this);
		if (CONSOLE_PANEL) consolePanel = new ConsolePanel(monitorPanel.monitor(), null);
		setup();
	}

	@Override
	public void connect(VideoSignal videoSignal, ConsoleControlsSocket controlsSocket, CartridgeSocket cartridgeSocket) {
		monitorPanel.connect(videoSignal, controlsSocket, cartridgeSocket);
		if (consolePanel != null) consolePanel.connect(controlsSocket);
	}
	
	@Override
	public void powerOn() {
		monitorPanel.powerOn();
	}
	
	@Override
	public void powerOff() {
		monitorPanel.powerOff();
	}

	@Override
	public void destroy() {
		monitorPanel.destroy();
	}
	
	@Override
	public Monitor monitor() {
		return monitorPanel.monitor();
	}

	@Override
	public Component[] controlsInputComponents() {
		return new Component[] { this };
	}
	
	@Override
	public synchronized void addKeyListener(KeyListener l) {
		super.addKeyListener(l);
		monitorPanel.addKeyListener(l);
	}
	@Override
	public synchronized void removeKeyListener(KeyListener l) {
		super.removeKeyListener(l);
		monitorPanel.removeKeyListener(l);
	}


	private void setup() {
		monitorPanel.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (e.getComponent() == monitorPanel) validate();
			}});		
		setLayout(new FlowLayout(
				FlowLayout.CENTER,
				0, 0
		));
		setOpaque(false);
		add(monitorPanel);
		if (consolePanel != null) add(consolePanel);
		validate();
	}

	public MonitorPanel monitorPanel;
	public ConsolePanel consolePanel;

	private static final boolean CONSOLE_PANEL = Parameters.SCREEN_CONSOLE_PANEL;

	private static final long serialVersionUID = 1L;

}
