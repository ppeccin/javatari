// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.screen;

import general.av.video.VideoSignal;

import java.awt.BufferCapabilities;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.ImageCapabilities;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Constructor;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import parameters.Parameters;
import pc.room.Room;
import utils.GraphicsDeviceHelper;
import utils.Terminator;
import utils.slickframe.HotspotManager;
import atari.cartridge.Cartridge;
import atari.cartridge.CartridgeSocket;
import atari.controls.ConsoleControlsSocket;

public final class MonitorPanel extends JPanel implements MonitorDisplay {

	public MonitorPanel() {
		super();
		init();
		monitor = new Monitor();
		monitor.setDisplay(this);
		// Does not add control components. Its the parent responsibility to do so.
	}

	public void connect(VideoSignal videoSignal, ConsoleControlsSocket controlsSocket, CartridgeSocket cartridgeSocket) {
		monitor.connect(videoSignal, cartridgeSocket);
	}

	public Monitor monitor() {
		return monitor;
	}

	public void powerOn() {
		SwingUtilities.invokeLater(new Runnable() {  @Override public void run() {
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

	private void init() {
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
		addKeyListener(new AppletScreenControlKeyListener());
		setTransferHandler(new ROMDropTransferHandler());
		addHotspots();
	}
		
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			Insets ins = getInsets();
			totalCanvasHorizPadding = ins.left + ins.right + SLICK_INSETS.left + SLICK_INSETS.right + BORDER_SIZE * 2;
			totalCanvasVertPadding = ins.top + ins.bottom + SLICK_INSETS.top + SLICK_INSETS.bottom + BORDER_SIZE * 2;
			canvasSetRenderingMode();
			SwingUtilities.invokeLater(new Runnable() {  @Override public void run() {
				repaint();
			}});
		}
	}
	
	@Override
	public synchronized void addKeyListener(KeyListener l) {
		super.addKeyListener(l);
		canvas.addKeyListener(l);
	}
	@Override
	public synchronized void removeKeyListener(KeyListener l) {
		super.removeKeyListener(l);
		canvas.removeKeyListener(l);
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
	public synchronized void removeMouseListener(MouseListener l) {
		super.removeMouseListener(l);
		canvas.removeMouseListener(l);
	}
	@Override
	public synchronized void removeMouseMotionListener(MouseMotionListener l) {
		super.removeMouseMotionListener(l);
		canvas.removeMouseMotionListener(l);
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
		// First try with vSync option
		Class<?> extBufCapClass = null;
		if (Monitor.BUFFER_VSYNC != -1)
			try {
				// Creates ExtendedBufferCapabilities via reflection to avoid problems with AccessControl
				extBufCapClass = Class.forName("sun.java2d.pipe.hw.ExtendedBufferCapabilities");
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
		BufferCapabilities grantedCaps = bufferStrategy.getCapabilities();
		System.out.println("Backbuffer accelerated: " + grantedCaps.getBackBufferCapabilities().isAccelerated());
		System.out.println("PageFlipping active: " + grantedCaps.isPageFlipping() + ", " + grantedCaps.getFlipContents());
		if (extBufCapClass != null && grantedCaps.getClass().equals(extBufCapClass))
			try {
				System.out.println("VSynch active: " + extBufCapClass.getMethod("getVSync",(Class<?>[])null).invoke(grantedCaps));
			} catch (Exception ex) {}
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
		hotspots = new HotspotManager(this);
		hotspots.addHotspot(
			new Rectangle(HotspotManager.CENTER_HOTSPOT, -27, 24, 28), 	// Logo. Horizontally centered
			new Runnable() { @Override public void run() { 
			}});
		hotspots.addHotspot(
			new Rectangle(-28, -24, 17, 19),
			new Runnable() { @Override public void run() {
				Room.currentRoom().openSettings();
				requestFocus();
			}});
	}

	private void loadImages() {
		try {
			topLeft = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/TopLeft.png");
			bottomLeft = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/BottomLeft.png");
			topRight = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/TopRight.png");
			bottomRight = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/BottomRight.png");
			top = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/Top.png");
			bottomLeftBar = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/BottomLeftBar.png");
			bottomLeftBarNoPower = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/BottomLeftBarNoPower.png");
			bottomRightBar = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/BottomRightBar.png");
			bottomRightBarFixedSize = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/BottomRightBarFixedSize.png");
			bottomBar = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/BottomBar.png");
			logoBar = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/LogoBar.png");
		} catch (IOException ex) {
			System.out.println("Screen Window: unable to load images\n" + ex);
		}
	}
	
	private void exit() {
		// Close program
		Terminator.terminate();
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
		g.drawImage(logoBar, halfW - 12, h - 30, null);
		BufferedImage bLeftBar = monitor.isFixedSize() ? bottomLeftBarNoPower : bottomLeftBar;
		g.drawImage(bLeftBar, 0, h - 30, maxHalfW, h, 0, 0, maxHalfW, 30, null);
		BufferedImage bRightBar = monitor.isFixedSize() ? bottomRightBarFixedSize : bottomRightBar;
		g.drawImage(bRightBar, w - maxHalfW, h - 30, w, h, 512 - maxHalfW, 0, 512, 30, null);
		g.drawImage(bottomLeft, 0, halfH, 4, h - 30, 0, 600 - halfH, 4, 600, null);
		g.drawImage(bottomRight, w - 4, halfH, w, h - 30, 0, 600 - halfH, 4, 600, null);
		g.dispose();
	}
	
	private Monitor monitor;
	private Canvas canvas;

	private BufferStrategy bufferStrategy;
	private HotspotManager hotspots;

	private BufferedImage topLeft, bottomLeft, topRight, bottomRight, top,
		bottomBar, bottomLeftBar, bottomLeftBarNoPower, bottomRightBar, bottomRightBarFixedSize, logoBar;
	
	public static final int BORDER_SIZE = Parameters.SCREEN_BORDER_SIZE;

	private int totalCanvasVertPadding = SLICK_INSETS.top + SLICK_INSETS.bottom + BORDER_SIZE * 2;
	private int totalCanvasHorizPadding = SLICK_INSETS.left + SLICK_INSETS.right + BORDER_SIZE * 2;

	private static final Insets SLICK_INSETS = new Insets(4, 4, 30, 4);

	public static final long serialVersionUID = 1L;


	private class AppletScreenControlKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			int code = e.getKeyCode();
			switch (e.getModifiersEx()) {
			case 0:
				if (code == KEY_EXIT) exit();
				return;
			case KeyEvent.ALT_DOWN_MASK:
				switch (code) {
					case KEY_HELP: return;
				}
			}
		}
		static final int KEY_EXIT     = KeyEvent.VK_ESCAPE;
		static final int KEY_HELP     = KeyEvent.VK_H;
	}

	
	// To handle drag and drop of ROM files and links
	class ROMDropTransferHandler extends TransferHandler {
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
