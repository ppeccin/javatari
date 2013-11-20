// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.screen;

import java.awt.BufferCapabilities;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.ImageCapabilities;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import org.javatari.atari.cartridge.CartridgeSocket;
import org.javatari.atari.controls.ConsoleControlsSocket;
import org.javatari.general.av.video.VideoSignal;
import org.javatari.parameters.Parameters;
import org.javatari.pc.room.EmbeddedRoom;
import org.javatari.pc.room.Room;
import org.javatari.utils.SwingHelper;
import org.javatari.utils.slickframe.HotspotPanel;


public final class MonitorPanel extends HotspotPanel implements MonitorDisplay {

	public MonitorPanel() {
		super();
		setup();
		addHotspots();
		monitor = new Monitor();
		monitor.setDisplay(this);
	}

	public void connect(VideoSignal videoSignal, ConsoleControlsSocket controlsSocket, CartridgeSocket cartridgeSocket) {
		monitor.connect(videoSignal, cartridgeSocket);
	}

	public Monitor monitor() {
		return monitor;
	}

	public void powerOn() {
		SwingHelper.edtSmartInvokeAndWait(new Runnable() { @Override public void run() {
			setVisible(true);
			monitor.powerOn();
		}});
	}
	
	public void powerOff() {
		monitor.powerOff();
	}

	public void destroy() {
		monitor.destroy();
	}

	private void setup() {
		loadImages();
		setBackground(Color.BLACK);
		setIgnoreRepaint(true);
		setLayout(null);
		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);
		canvas.setIgnoreRepaint(true);
		canvas.setFocusTraversalKeysEnabled(false);
		add(canvas);
		positionCanvas();
		addControlInputComponents(keyControlsInputComponents());
		popupEnabled = EMBEDDED_POPUP && Room.currentRoom() instanceof EmbeddedRoom;
	}
		
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			Insets ins = getInsets();
			totalCanvasHorizPadding = ins.left + ins.right + SLICK_INSETS.left + SLICK_INSETS.right + BORDER_SIZE * 2;
			totalCanvasVertPadding = ins.top + ins.bottom + SLICK_INSETS.top + SLICK_INSETS.bottom + BORDER_SIZE * 2;
			canvasSetRenderingMode();
			SwingHelper.edtInvokeLater(new Runnable() {  @Override public void run() {
				repaint();
			}});
		}
	}
	
	public List<Component> keyControlsInputComponents() {
		return Arrays.asList((Component)this, canvas);
	}

	@Override
	public void displaySize(Dimension size) {
		// Redefines the panel bounds, and the internal Canvas will follow accordingly
		Dimension panelDim = panelDimensionForCanvasDimension(size);
		if (getSize().equals(panelDim)) return;
		setSize(panelDim);
		setPreferredSize(panelDim);
		setMinimumSize(panelDim);
		setMaximumSize(panelDim);
		canvas.setSize(size);
		validate();
	}

	@Override
	public void displayCenter() {
		// Screen should never change position
	}

	@Override
	public Dimension displayEffectiveSize() {
		return canvas.getSize();
	}

	@Override
	public Graphics2D displayGraphics() {
		Graphics2D graphics = (Graphics2D) (bufferStrategy != null ? bufferStrategy.getDrawGraphics() : canvas.getGraphics());
		return graphics;
	}

	@Override
	public void displayFinishFrame(Graphics2D graphics) {
		graphics.dispose();
		if (bufferStrategy != null) bufferStrategy.show();
	}

	@Override
	public Container displayContainer() {
		return this;
	}

	@Override
	public void displayClear() {
		Graphics2D canvasGraphics = displayGraphics();
		canvasGraphics.setColor(Color.BLACK);
		canvasGraphics.clearRect(0, 0, getWidth(), getHeight());
		displayFinishFrame(canvasGraphics);
	}
	
	@Override
	public float displayDefaultOpenningScaleX(int displayWidh, int displayHeight) {
		return Monitor.DEFAULT_SCALE_X;
	}

	@Override
	public void displayMinimumSize(Dimension minSize) {
		// Ignore
	}
	
	@Override
	public void displayRequestFocus() {
		requestFocus();
	}

	@Override
	public void displayLeaveFullscreen() {
		// Ignore
	}

	public void canvasSetRenderingMode() {
		if (Monitor.MULTI_BUFFERING <= 0) return;
		BufferCapabilities desiredCaps = new BufferCapabilities(
			new ImageCapabilities(true), new ImageCapabilities(true),
			Monitor.PAGE_FLIPPING ? FlipContents.BACKGROUND : null
		);
		Class<?> extBufCapClass = null;
		try {
			// Creates ExtendedBufferCapabilities via reflection to avoid problems with AccessControl
			extBufCapClass = Class.forName("sun.java2d.pipe.hw.ExtendedBufferCapabilities");
		} catch (Exception ex) {}
		// First try with vSync option
		if (extBufCapClass != null && Monitor.BUFFER_VSYNC != -1)
			try {
				// Creates ExtendedBufferCapabilities via reflection to avoid problems with AccessControl
				Class<?> vSyncTypeClass = Class.forName("sun.java2d.pipe.hw.ExtendedBufferCapabilities$VSyncType");
				Constructor<?> extBufCapConstructor = extBufCapClass.getConstructor(
					new Class[] { BufferCapabilities.class, vSyncTypeClass }
				);
	            Object vSyncType = vSyncTypeClass.getField(Monitor.BUFFER_VSYNC == 1 ? "VSYNC_ON" : "VSYNC_OFF").get(null);
	            BufferCapabilities extBuffCaps = (BufferCapabilities)extBufCapConstructor.newInstance(
	            	new Object[] { desiredCaps, vSyncType }
	            );
	            // Try creating the BufferStrategy
	            canvas.createBufferStrategy(Monitor.MULTI_BUFFERING, extBuffCaps);
			} catch (Exception ex) {}
		// Then try with remaining options (Flipping, etc)
		if (canvas.getBufferStrategy() == null)
			try {
				canvas.createBufferStrategy(Monitor.MULTI_BUFFERING, desiredCaps);
			} catch (Exception ex) {}
		// Last, use the default
		if (canvas.getBufferStrategy() == null) {
			System.out.println("Could not create desired BufferStrategy. Switching to default...");
			canvas.createBufferStrategy(Monitor.MULTI_BUFFERING);
		}
		bufferStrategy = canvas.getBufferStrategy();
		// Show info about the granted BufferStrategy
		if (bufferStrategy != null) System.out.println("Buffer Strategy: " + bufferStrategy.getClass().getSimpleName());
		BufferCapabilities grantedCaps = bufferStrategy.getCapabilities();
		System.out.println("Backbuffer Accelerated: " + grantedCaps.getBackBufferCapabilities().isAccelerated());
		System.out.println("PageFlipping Active: " + grantedCaps.isPageFlipping() + ", " + grantedCaps.getFlipContents());
		if (extBufCapClass != null && grantedCaps.getClass().equals(extBufCapClass))
			try {
				System.out.println("VSynch active: " + extBufCapClass.getMethod("getVSync",(Class<?>[])null).invoke(grantedCaps));
			} catch (Exception ex) {}
	}

	private void addControlInputComponents(List<Component> inputs) {
		MonitorPanelControlKeyListener lis = new MonitorPanelControlKeyListener();
		for (Component component : inputs)
			component.addKeyListener(lis);
	}

	private Dimension panelDimensionForCanvasDimension(Dimension size) {
		return new Dimension(
			size.width + totalCanvasHorizPadding,
			size.height + totalCanvasVertPadding
		);
	}

	private void positionCanvas() {
		canvas.setLocation(
			SLICK_INSETS.left + BORDER_SIZE, SLICK_INSETS.top + BORDER_SIZE
		);
	}

	private void addHotspots() {
		addHotspot(
			new Rectangle(HotspotPanel.CENTER_HOTSPOT, -27, 24, 28),
			new Runnable() { @Override public void run() { 
			}});
		addHotspot(
			new Rectangle(-30, -25, 21, 20), "Open Settings",
			new Runnable() { @Override public void run() {
				Room.currentRoom().openSettings(MonitorPanel.this);
				requestFocus();
			}});
		if (popupEnabled)
			addHotspot(
				new Rectangle(7, -25, 23, 20), "Detach Screen",
				new Runnable() { @Override public void run() { 
						((EmbeddedRoom) Room.currentRoom()).popUpScreen(false);		// Not in Fullscreen
				}});
	}

	private void loadImages() {
		try {
			topLeft = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/TopLeft.png");
			bottomLeft = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/BottomLeft.png");
			topRight = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/TopRight.png");
			bottomRight = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/BottomRight.png");
			top = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/Top.png");
			bottomLeftBar = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/BottomLeftBar.png");
			bottomLeftBarNoPower = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/BottomLeftBarNoPower.png");
			bottomRightBar = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/BottomRightBar.png");
			bottomRightBarFixedSize = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/BottomRightBarFixedSize.png");
			bottomBar = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/BottomBar.png");
			logoBar = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/LogoBar.png");
			popup = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/Popup.png");
		} catch (IOException ex) {
			System.out.println("Screen Window: unable to load images\n" + ex);
		}
	}
	
	@Override
	public void paintComponent(Graphics origGraphics) {
		super.paintComponent(origGraphics);
		Insets ins = getInsets();
		int w = getWidth() - ins.left - ins.right; 
		int h = getHeight() - ins.top - ins.bottom;
		Graphics g = origGraphics.create(ins.left, ins.top, w, h);
		// Clears the inner part, leaving decorations intact
		g.setColor(Color.BLACK);
		g.fillRect(SLICK_INSETS.left, SLICK_INSETS.top, getWidth() - SLICK_INSETS.left - SLICK_INSETS.right, getHeight() - SLICK_INSETS.top - SLICK_INSETS.bottom);
		// Draw decorations
		int halfH = (h - 30) / 2; 
		int halfW = w / 2;
		int maxHalfW = Math.min(halfW - 11, 512);
		for (int x = 0; x < w; x += 512) 
			g.drawImage(top, x, 0, null);
		g.drawImage(topLeft, 0, 0, null);
		g.drawImage(topRight, w - 4, 0, null);
		for (int x = 512; x < w - 512; x += 256) 
			g.drawImage(bottomBar, x, h - 30, x + 256, h, 0, 0, 256, 30, null);
		BufferedImage bLeftBar = monitor.isFixedSize() ? bottomLeftBarNoPower : bottomLeftBar;
		g.drawImage(bLeftBar, 0, h - 30, maxHalfW, h, 0, 0, maxHalfW, 30, null);
		BufferedImage bRightBar = monitor.isFixedSize() ? bottomRightBarFixedSize : bottomRightBar;
		g.drawImage(bRightBar, w - maxHalfW, h - 30, w, h, 512 - maxHalfW, 0, 512, 30, null);
		g.drawImage(logoBar, halfW - 12, h - 30, null);
		g.drawImage(bottomLeft, 0, halfH, 4, h - 30, 0, 600 - halfH, 4, 600, null);
		g.drawImage(bottomRight, w - 4, halfH, w, h - 30, 0, 600 - halfH, 4, 600, null);
		if (popupEnabled) g.drawImage(popup, 11, h - 22, null);
		
		paintHotspots(g);
		
		g.dispose();
	}
	
	private Monitor monitor;
	private Canvas canvas;

	private BufferStrategy bufferStrategy;
	private boolean popupEnabled;

	private BufferedImage topLeft, bottomLeft, topRight, bottomRight, top,
		bottomBar, bottomLeftBar, bottomLeftBarNoPower, bottomRightBar, bottomRightBarFixedSize, logoBar, popup;
	
	private int totalCanvasVertPadding = SLICK_INSETS.top + SLICK_INSETS.bottom + BORDER_SIZE * 2;
	private int totalCanvasHorizPadding = SLICK_INSETS.left + SLICK_INSETS.right + BORDER_SIZE * 2;

	private static final Insets SLICK_INSETS = new Insets(4, 4, 30, 4);

	public static final int BORDER_SIZE = Parameters.SCREEN_BORDER_SIZE;
	private static final boolean EMBEDDED_POPUP = Parameters.SCREEN_EMBEDDED_POPUP;

	public static final long serialVersionUID = 1L;


	private class MonitorPanelControlKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			int code = e.getKeyCode();
			switch (e.getModifiersEx()) {
				case 0:
					if (code == KEY_EXIT)
						Room.currentRoom().currentConsole().powerOff();
					return;
				case KeyEvent.ALT_DOWN_MASK:
					if (popupEnabled && code == KEY_FULL_SCR)
						((EmbeddedRoom) Room.currentRoom()).popUpScreen(true);	// in Fullscreen
			}
		}
		static final int KEY_EXIT     = KeyEvent.VK_ESCAPE;
		static final int KEY_FULL_SCR = KeyEvent.VK_ENTER;
	}

	
}
