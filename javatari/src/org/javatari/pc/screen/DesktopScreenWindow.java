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
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.TransferHandler;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeSocket;
import org.javatari.atari.controls.ConsoleControlsSocket;
import org.javatari.general.av.video.VideoSignal;
import org.javatari.parameters.Parameters;
import org.javatari.pc.room.EmbeddedRoom;
import org.javatari.pc.room.Room;
import org.javatari.utils.SwingHelper;
import org.javatari.utils.slickframe.HotspotPanel;
import org.javatari.utils.slickframe.SlickFrame;


public final class DesktopScreenWindow extends SlickFrame implements MonitorDisplay, Screen {

	public DesktopScreenWindow() {
		super();
		setup();
	}

	@Override
	public void connect(VideoSignal videoSignal, ConsoleControlsSocket controlsSocket, CartridgeSocket cartridgeSocket) {
		monitor.connect(videoSignal, cartridgeSocket);
		if (consolePanelWindow != null) consolePanelWindow.connect(controlsSocket, cartridgeSocket);
	}

	@Override
	public void powerOn() {
		powerOn(FULLSCREEN);
	}
		
	public void powerOn(final boolean fullScreen) {
		SwingHelper.edtSmartInvokeAndWait(new Runnable() { @Override public void run() {
			fullScreen(fullScreen);
			monitor.powerOn();
		}});
	}

	@Override
	public void powerOff() {
		SwingHelper.edtSmartInvokeAndWait(new Runnable() { @Override public void run() {
			if (fullScreen) fullScreen(false);
			monitor.powerOff();
			setVisible(false);
		}});
	}

	@Override
	public void destroy() {
		close();
		monitor.destroy();
	}

	@Override
	public void close() {
		setVisible(false);
	}

	@Override
	public Monitor monitor() {
		return monitor;
	}

	@Override
	protected void init() {
		loadImages();
		setBackground(Color.BLACK);
		getContentPane().setBackground(Color.BLACK);
		getContentPane().setIgnoreRepaint(true);
		setLayout(null);
		setIconImages(Arrays.asList(new Image[] { icon64, icon32, favicon }));
		setTitle(BASE_TITLE);
		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);
		canvas.setIgnoreRepaint(true);
		canvas.setFocusTraversalKeysEnabled(false);
		add(canvas);
		super.init();
	}
		
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			// Consider this window actual insets, they may not be zero if its still decorated for some reason
			Insets ins = getInsets();
			totalCanvasHorizPadding = ins.left + ins.right + SLICK_INSETS.left + SLICK_INSETS.right + BORDER_SIZE * 2;
			totalCanvasVertPadding = ins.top + ins.bottom + SLICK_INSETS.top + SLICK_INSETS.bottom + BORDER_SIZE * 2;
			if (consolePanelWindow != null) consolePanelWindow.setVisible(true);
			canvasSetRenderingMode();
			SwingHelper.edtInvokeLater(new Runnable() {  @Override public void run() {
				repaint();
			}});
		} else
			if (consolePanelWindow != null) consolePanelWindow.setVisible(false);
	}
	
	public void fullScreen(boolean state) {
		synchronized (monitor.refreshMonitor) {
			if (state) openFullWindow();
			else openWindow();
		}
	}

	@Override
	public List<Component> keyControlsInputComponents() {
		List<Component> comps = new ArrayList<Component>(Arrays.asList((Component)this, canvas, fullWindow));
		if (consolePanelWindow != null) comps.add(consolePanelWindow);
		return comps;
	}

	@Override
	public synchronized void addMouseListener(MouseListener l) {
		super.addMouseListener(l);
		canvas.addMouseListener(l);
	}
	@Override
	public synchronized void addMouseMotionListener(MouseMotionListener l) {
		super.addMouseMotionListener(l);
		canvas.addMouseMotionListener(l);
	}

	@Override
	public void displaySize(Dimension size) {
		// Redefines the entire Window bounds, and the internal Canvas will follow accordingly
		Dimension winDim = windowDimensionForCanvasDimension(size);
		if (getSize().equals(winDim)) return;
		// Maintain the window center
		int centerX = getX() + getWidth() / 2;
		int centerY = getY() + (getHeight() + DesktopConsolePanel.EXPANDED_HEIGHT) / 4;
		int newX = centerX - winDim.width / 2;
		int newY = centerY - (winDim.height + DesktopConsolePanel.EXPANDED_HEIGHT) / 4;
		setBounds(newX, newY, winDim.width, winDim.height);
		// validate();
		// repaint();
	}

	@Override
	public void displayCenter() {
		Rectangle bounds = SwingHelper.getGraphicsConfigurationForCurrentLocation(this).getBounds();
		int x = (bounds.width - getWidth()) / 2 + bounds.x;
		int y = (bounds.height - getHeight() - DesktopConsolePanel.EXPANDED_HEIGHT) / 4 + bounds.y;
		setLocation(x, y);
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
		minimunResize(windowDimensionForCanvasDimension(minSize));
	}
	
	@Override
	public void displayRequestFocus() {
		requestFocus();
	}

	@Override
	public void displayLeaveFullscreen() {
		if (fullScreen) fullScreen(false);
	}

	private void setup() {
		popinEnabled = EMBEDDED_POPUP && Room.currentRoom() instanceof EmbeddedRoom;
		addHotspots();
		monitor = new Monitor();
		fullWindow = new DesktopScreenFullWindow(this);
		monitor.addControlInputComponents(this.keyControlsInputComponents());
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (e.getComponent() == DesktopScreenWindow.this) positionCanvas();
			}});		
		getRootPane().setTransferHandler(new ROMDropTransferHandler());
		if (CONSOLE_PANEL) {
			consolePanelWindow = new DesktopConsolePanel(this, monitor);
			consolePanelWindow.setTransferHandler(new ROMDropTransferHandler());
		}
		addInputComponents(keyControlsInputComponents());
		monitor.setDisplay(this);
		displayCenter();
	}

	private void addInputComponents(List<Component> inputs) {
		DesktopScreenControlKeyListener lis = new DesktopScreenControlKeyListener();
		for (Component component : inputs)
			component.addKeyListener(lis);
	}

	private void openWindow() {
		if (isVisible()) return;
		// Exit FullScreen mode, if that was the case
		if (fullWindow != null && fullWindow.isVisible()) {
			try {
				GraphicsConfiguration graphicsConfig = 
						SwingHelper.getGraphicsConfigurationForCurrentLocation(fullWindow);
				graphicsConfig.getDevice().setFullScreenWindow(null);
			} catch (Exception e) {
				// Ignore. Probably FSEM is not supported
			}
			fullWindow.setVisible(false);
		}
		setVisible(true);
		toFront();
		requestFocus();
		monitor.setDisplay(this);
		fullScreen = false;
	}

	private void openFullWindow() {
		if (fullWindow.isVisible()) return;
		// Try to set full screen in the monitor we are currently in
		GraphicsConfiguration graphicsConfig = 
				SwingHelper.getGraphicsConfigurationForCurrentLocation(this);
		setVisible(false);
		fullWindow.setBounds(graphicsConfig.getBounds());
		fullWindow.setVisible(true);
		// Try to enter FullScreen Exclusive mode if desired
		if (USE_FSEM != 0) {
			try {
				graphicsConfig.getDevice().setFullScreenWindow(fullWindow);
			} catch (Exception e) {
				// Ignore. Probably FSEM is not supported
			}
		}
		fullWindow.toFront();
		fullWindow.requestFocus();
		monitor.setDisplay(fullWindow);
		fullScreen = true;
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

	private Dimension windowDimensionForCanvasDimension(Dimension size) {
		return new Dimension(
			size.width + totalCanvasHorizPadding,
			size.height + totalCanvasVertPadding
		);
	}
	
	private void positionCanvas() {
		canvas.setBounds(
			SLICK_INSETS.left + BORDER_SIZE, SLICK_INSETS.top + BORDER_SIZE,
			getWidth() - totalCanvasHorizPadding, 
			getHeight() - totalCanvasVertPadding
		);
	}

	private void addHotspots() {
		HotspotPanel hotspotPanel = getContentHotspotPanel();
		hotspotPanel.addHotspot(
			new Rectangle(HotspotPanel.CENTER_HOTSPOT, -27, 24, 28), "Toggle Console Controls",
			new Runnable() { @Override public void run() { 
				if (consolePanelWindow != null) consolePanelWindow.toggle();
			}});
		hotspotPanel.addHotspot(
			new Rectangle(-70 -44, -20, 13, 15), "Minimize",
			new Runnable() { @Override public void run() { 
				setState(ICONIFIED);
			}});
		hotspotPanel.addHotspot(
			new Rectangle(-52 - 44, -21, 12, 16), "Decrease Size", 
			new Runnable() { @Override public void run() { 
				monitor.controlActivated(Monitor.Control.SIZE_MINUS);
			}});
		hotspotPanel.addHotspot(
			new Rectangle(-35 - 44, -24, 14, 19), "Increase Size", 
			new Runnable() { @Override public void run() { 
				monitor.controlActivated(Monitor.Control.SIZE_PLUS);
			}});
		hotspotPanel.addHotspot(
			new Rectangle(-16 -44, -24, 17, 19), "Go Full Screen", 
			new Runnable() { @Override public void run() { 
				fullScreen(!fullScreen);
			}});
		hotspotPanel.addHotspot(
			new Rectangle(-37, -24, 16, 19), "Open Settings",
			new Runnable() { @Override public void run() {
				Room.currentRoom().openSettings(DesktopScreenWindow.this);
				requestFocus();
			}});
		if (popinEnabled)
			hotspotPanel.addHotspot(
				new Rectangle(7, -25, 23, 20), "Re-attach Screen",
				new Runnable() { @Override public void run() { 
					((EmbeddedRoom) Room.currentRoom()).reembedScreen();
				}});
		else
			hotspotPanel.addHotspot(
				new Rectangle(8, -25, 19, 19), "Shutdown",
				new Runnable() { @Override public void run() { 
					Room.currentRoom().exit();
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
			bottomBar = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/BottomBar.png");
			logoBar = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/LogoBar.png");
			popin = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/Popin.png");
			favicon = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/Favicon.png");
			icon64 = SwingHelper.loadAsCompatibleTranslucentImage("org/javatari/pc/screen/images/LogoIcon64.png");
			icon32 = SwingHelper.loadAsCompatibleTranslucentImage("org/javatari/pc/screen/images/LogoIcon32.png");
		} catch (IOException ex) {
			System.out.println("Screen Window: unable to load images\n" + ex);
		}
	}
	
	@Override
	public void paint(Graphics origGraphics) {
		Insets ins = getInsets();
		int w = getWidth() - ins.left - ins.right; 
		int h = getHeight() - ins.top - ins.bottom;
		Graphics g = origGraphics.create(ins.left, ins.top, w, h);
		// Clears the inner part of the window, leaving decorations intact
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
		g.drawImage(popinEnabled ? bottomLeftBarNoPower : bottomLeftBar, 0, h - 30, maxHalfW, h, 0, 0, maxHalfW, 30, null);
		g.drawImage(bottomRightBar, w - maxHalfW, h - 30, w, h, 512 - maxHalfW, 0, 512, 30, null);
		g.drawImage(logoBar, halfW - 12, h - 30, null);
		g.drawImage(bottomLeft, 0, halfH, 4, h - 30, 0, 600 - halfH, 4, 600, null);
		g.drawImage(bottomRight, w - 4, halfH, w, h - 30, 0, 600 - halfH, 4, 600, null);
		if (popinEnabled) g.drawImage(popin, 11, h - 22, null);

		paintHotspots(g);	
		
		g.dispose();
	}
	
	private Monitor monitor;

	private Canvas canvas;
	
	private DesktopConsolePanel consolePanelWindow;
	private DesktopScreenFullWindow fullWindow;
	private boolean fullScreen = false;
		
	private BufferStrategy bufferStrategy;
	private boolean popinEnabled;

	private int totalCanvasVertPadding = SLICK_INSETS.top + SLICK_INSETS.bottom;
	private int totalCanvasHorizPadding = SLICK_INSETS.left + SLICK_INSETS.right;

	private BufferedImage topLeft, bottomLeft, topRight, bottomRight, top,
		bottomBar, bottomLeftBar, bottomLeftBarNoPower, bottomRightBar, logoBar, popin;
	
	public BufferedImage favicon, icon64, icon32;	// Other windows use these

	private static final Insets SLICK_INSETS = new Insets(4, 4, 30, 4);

	public static final String BASE_TITLE = "javatari";
	public static final boolean FULLSCREEN = Parameters.SCREEN_FULLSCREEN;
	public static final int USE_FSEM = Parameters.SCREEN_USE_FSEM;
	public static final int BORDER_SIZE = Parameters.SCREEN_BORDER_SIZE;
	public static final boolean CONSOLE_PANEL = Parameters.SCREEN_CONSOLE_PANEL;
	private static final boolean EMBEDDED_POPUP = Parameters.SCREEN_EMBEDDED_POPUP;

	public static final long serialVersionUID = 1L;


	private class DesktopScreenControlKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			int code = e.getKeyCode();
			switch (e.getModifiersEx()) {
			case 0:
				if (code == KEY_EXIT) {
					if (fullScreen) fullScreen(false);
					else if	(popinEnabled)((EmbeddedRoom) Room.currentRoom()).reembedScreen();
					else Room.currentRoom().exit();
				}
				return;
			case KeyEvent.ALT_DOWN_MASK:
				switch (code) {
					case KEY_FULL_SCR: 
						fullScreen(!fullScreen); return;
					case KEY_CONSOLE_PANEL: 
						if (!fullScreen && consolePanelWindow != null) consolePanelWindow.toggle(); return;
				}
			}
		}
		static final int KEY_EXIT          = KeyEvent.VK_ESCAPE;
		static final int KEY_CONSOLE_PANEL = KeyEvent.VK_H;
		static final int KEY_FULL_SCR      = KeyEvent.VK_ENTER;
	}

	
	// To handle drag and drop of ROM files and links
	private class ROMDropTransferHandler extends TransferHandler {
		@Override
		public boolean canImport(TransferSupport support) {
			if (!monitor.isCartridgeChangeEnabled()) return false;
			Transferable transf = support.getTransferable();
			if (!ROMTransferHandlerUtil.canAccept(transf)) return false;
			if (support.isDrop() && support.getUserDropAction() != LINK) support.setDropAction(COPY);
			return true;
		}
		@Override
		public boolean importData(TransferSupport support) {
			if (!canImport(support)) return false;
			monitor.showOSD("LOADING CARTRIDGE...", true);
			Cartridge cart = ROMTransferHandlerUtil.importCartridgeData(support.getTransferable());
			monitor.showOSD(null, true);
			if (cart == null) return false;
			// LINK Action means load Cartridge without auto power! :-)
			boolean autoPower = !support.isDrop() || support.getDropAction() != LINK;
			monitor.cartridgeInsert(cart, autoPower);
			return true;
		}
		private static final long serialVersionUID = 1L;
	}

}
