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
import java.awt.GraphicsDevice;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;

import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import parameters.Parameters;
import pc.controls.AWTConsoleControls;
import utils.GraphicsDeviceHelper;
import utils.Terminator;
import utils.slickframe.HotspotManager;
import utils.slickframe.SlickFrame;
import atari.cartridge.Cartridge;
import atari.cartridge.CartridgeSocket;
import atari.controls.ConsoleControlsSocket;

public class DesktopScreenWindow extends SlickFrame implements ScreenDisplay {

	public DesktopScreenWindow(VideoSignal videoSignal, ConsoleControlsSocket controlsSocket, CartridgeSocket cartridgeSocket) throws HeadlessException {
		super();
		screen = new Screen(videoSignal, cartridgeSocket);
		consolePanelWindow = new DesktopConsolePanel(this, screen, controlsSocket);
		fullWindow = new DesktopScreenFullWindow(this);
		screen.setControlInputComponent(this);
		consoleControls = new AWTConsoleControls(controlsSocket, screen, this);
		setup();
	}

	public void powerOn() {
		SwingUtilities.invokeLater(new Runnable() {  @Override public void run() {
			fullScreen(FULLSCREEN);
			screen.powerOn();
		}});
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
			// Considers this window actual insets, they may not be zero if its still decorated for some reason
			Insets ins = getInsets();
			totalCanvasHorizPadding = ins.left + ins.right + SLICK_INSETS.left + SLICK_INSETS.right + BORDER_SIZE * 2;
			totalCanvasVertPadding = ins.top + ins.bottom + SLICK_INSETS.top + SLICK_INSETS.bottom + BORDER_SIZE * 2;
			consolePanelWindow.setVisible(true);
			canvasSetRenderingMode();
			SwingUtilities.invokeLater(new Runnable() {  @Override public void run() {
				repaint();
			}});
		} else
			consolePanelWindow.setVisible(false);
	}
	
	@Override
	public synchronized void addKeyListener(KeyListener l) {
		super.addKeyListener(l);
		canvas.addKeyListener(l);
		fullWindow.addKeyListener(l);
		consolePanelWindow.addKeyListener(l);
	}
	@Override
	public synchronized void removeKeyListener(KeyListener l) {
		super.removeKeyListener(l);
		canvas.removeKeyListener(l);
		fullWindow.removeKeyListener(l);
		consolePanelWindow.removeKeyListener(l);
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
	public void canvasSize(Dimension size) {
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
	public void canvasCenter() {
		Toolkit tk = Toolkit.getDefaultToolkit();
		int x = (tk.getScreenSize().width - getWidth()) / 2;
		int y = (tk.getScreenSize().height - getHeight() - DesktopConsolePanel.EXPANDED_HEIGHT) / 4;
		setLocation(x, y);
	}

	@Override
	public Dimension canvasEffectiveSize() {
		return canvas.getSize();
	}

	@Override
	public Graphics2D canvasGraphics() {
		Graphics2D graphics = (Graphics2D) (bufferStrategy != null ? bufferStrategy.getDrawGraphics() : canvas.getGraphics());
		return graphics;
	}

	@Override
	public void canvasFinishFrame(Graphics2D graphics) {
		graphics.dispose();
		if (bufferStrategy != null) bufferStrategy.show();
	}

	@Override
	public Container canvasContainer() {
		return this;
	}

	@Override
	public void canvasClear() {
		Graphics2D canvasGraphics = canvasGraphics();
		canvasGraphics.setColor(Color.BLACK);
		canvasGraphics.clearRect(0, 0, getWidth(), getHeight());
		canvasFinishFrame(canvasGraphics);
	}
	
	@Override
	public float canvasDefaultOpenningScaleX(int displayWidh, int displayHeight) {
		return Screen.DEFAULT_SCALE_X;
	}

	@Override
	public void canvasMinimumSize(Dimension minSize) {
		minimunResize(windowDimensionForCanvasDimension(minSize));
	}
	
	@Override
	public void canvasLeaveFullscreen() {
		if (fullScreen) fullScreen(false);
	}

	private void setup() {
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (e.getComponent() == DesktopScreenWindow.this) positionCanvas();
			}});		
		getRootPane().setTransferHandler(new ROMDropTransferHandler());
		addHotspots();
		addKeyListener(new DesktopControlKeyListener());
	}

	private void fullScreen(boolean state) {
		synchronized (screen.refreshMonitor) {
			if (state)
				if (openFullWindow()) return;
			openWindow();
		}
	}

	private void openWindow() {
		GraphicsDevice dev = GraphicsDeviceHelper.defaultScreenDevice(); 
		if (dev.isFullScreenSupported()) dev.setFullScreenWindow(null);
		fullWindow.setVisible(false);
		setVisible(true);
		fullScreen = false;
		screen.setCanvas(this);
	}

	private boolean openFullWindow() {
		GraphicsDevice dev = GraphicsDeviceHelper.defaultScreenDevice(); 
		if (!dev.isFullScreenSupported()) return false;
		setVisible(false);
		dev.setFullScreenWindow(fullWindow);	// will call setVisible(true)
		fullScreen = true;
		screen.setCanvas(fullWindow);
		return true;
	}

	public void canvasSetRenderingMode() {
		if (Screen.MULTI_BUFFERING <= 0) return;
		BufferCapabilities desiredCaps = new BufferCapabilities(
			new ImageCapabilities(true), new ImageCapabilities(true),
			Screen.PAGE_FLIPPING ? FlipContents.BACKGROUND : null
		);
		// First try with vSync option
		Class<?> extBufCapClass = null;
		if (Screen.BUFFER_VSYNC != -1)
			try {
				// Creates ExtendedBufferCapabilities via reflection to avoid problems with AccessControl
				extBufCapClass = Class.forName("sun.java2d.pipe.hw.ExtendedBufferCapabilities");
				Class<?> vSyncTypeClass = Class.forName("sun.java2d.pipe.hw.ExtendedBufferCapabilities$VSyncType");
				Constructor<?> extBufCapConstructor = extBufCapClass.getConstructor(
					new Class[] { BufferCapabilities.class, vSyncTypeClass }
				);
	            Object vSyncType = vSyncTypeClass.getField(Screen.BUFFER_VSYNC == 1 ? "VSYNC_ON" : "VSYNC_OFF").get(null);
	            BufferCapabilities extBuffCaps = (BufferCapabilities)extBufCapConstructor.newInstance(
	            	new Object[] { desiredCaps, vSyncType }
	            );
	            // Try creating the BufferStrategy
	            canvas.createBufferStrategy(Screen.MULTI_BUFFERING, extBuffCaps);
			} catch (Exception ex) {}
		// Then try with remaining options (Flipping, etc)
		if (canvas.getBufferStrategy() == null)
			try {
				canvas.createBufferStrategy(Screen.MULTI_BUFFERING, desiredCaps);
			} catch (Exception ex) {}
		// Last, use the default
		if (canvas.getBufferStrategy() == null) {
			System.out.println("Could not create desired BufferStrategy. Switching to default...");
			canvas.createBufferStrategy(Screen.MULTI_BUFFERING);
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
		hotspots = new HotspotManager(this, detachMouseListener());
		hotspots.addHotspot(
			new Rectangle(8, -25, 19, 19), 
			new Runnable() { @Override public void run() { 
				exit();
			}});
		hotspots.addHotspot(
			new Rectangle(-74 -22, -20, 13, 15), 
			new Runnable() { @Override public void run() { 
				setState(ICONIFIED);
			}});
		hotspots.addHotspot(
			new Rectangle(-56 - 22, -21, 13, 16), 
			new Runnable() { @Override public void run() { 
				screen.controlStateChanged(Screen.Control.SIZE_MINUS, true);
			}});
		hotspots.addHotspot(
			new Rectangle(-40 - 22, -24, 14, 19), 
			new Runnable() { @Override public void run() { 
				screen.controlStateChanged(Screen.Control.SIZE_PLUS, true);
			}});
		hotspots.addHotspot(
			new Rectangle(-42, -24, 17, 19), 
			new Runnable() { @Override public void run() { 
				fullScreen(!fullScreen);
			}});
		hotspots.addHotspot(
			new Rectangle(CENTER_HOTSPOT, -27, 24, 28), 	// Logo. Horizontally centered
			new Runnable() { @Override public void run() { 
				consolePanelWindow.toggle();
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
			bottomRightBar = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/BottomRightBar.png");
			bottomBar = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/BottomBar.png");
			logoBar = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/LogoBar.png");
			favicon = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/Favicon.png");
			icon64 = GraphicsDeviceHelper.loadAsCompatibleTranslucentImage("pc/screen/images/LogoIcon64.png");
			icon32 = GraphicsDeviceHelper.loadAsCompatibleTranslucentImage("pc/screen/images/LogoIcon32.png");
		} catch (IOException ex) {
			System.out.println("Screen Window: unable to load images\n" + ex);
		}
	}
	
	private void exit() {
		// Close program
		Terminator.terminate();
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
		g.drawImage(logoBar, halfW - 12, h - 30, null);
		g.drawImage(bottomLeftBar, 0, h - 30, maxHalfW, h, 0, 0, maxHalfW, 30, null);
		g.drawImage(bottomRightBar, w - maxHalfW, h - 30, w, h, 512 - maxHalfW, 0, 512, 30, null);
		g.drawImage(bottomLeft, 0, halfH, 4, h - 30, 0, 600 - halfH, 4, 600, null);
		g.drawImage(bottomRight, w - 4, halfH, w, h - 30, 0, 600 - halfH, 4, 600, null);
		g.dispose();
	}
	
	public Screen screen;
	public AWTConsoleControls consoleControls;
	
	private Canvas canvas;
	
	private DesktopScreenFullWindow fullWindow;
	private boolean fullScreen = false;
		
	private BufferStrategy bufferStrategy;
	private HotspotManager hotspots;

	private BufferedImage topLeft, bottomLeft, topRight, bottomRight, top,
		bottomBar, bottomLeftBar, bottomRightBar;
	
	public BufferedImage logoBar, favicon, icon64, icon32;	// Other windows use these
	public static final String BASE_TITLE = "javatari";

	public DesktopConsolePanel consolePanelWindow;
	public static final boolean FULLSCREEN = Parameters.SCREEN_FULLSCREEN;
	public static final int BORDER_SIZE = Parameters.SCREEN_BORDER_SIZE;

	private int totalCanvasVertPadding = SLICK_INSETS.top + SLICK_INSETS.bottom;
	private int totalCanvasHorizPadding = SLICK_INSETS.left + SLICK_INSETS.right;

	private static final Insets SLICK_INSETS = new Insets(4, 4, 30, 4);

	public static final long serialVersionUID = 1L;


	private class DesktopControlKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			int code = e.getKeyCode();
			switch (e.getModifiersEx()) {
			case 0:
				if (code == KEY_EXIT) 
					if (fullScreen) fullScreen(!fullScreen);
					else exit();
				return;
			case KeyEvent.ALT_DOWN_MASK:
				switch (code) {
					case KEY_FULL_SCR: fullScreen(!fullScreen); return;
					case KEY_HELP: if (!fullScreen)	consolePanelWindow.toggle(); return;
				}
			}
		}
		static final int KEY_EXIT     = KeyEvent.VK_ESCAPE;
		static final int KEY_HELP     = KeyEvent.VK_H;
		static final int KEY_FULL_SCR = KeyEvent.VK_ENTER;
	}

	
	// To handle drag and drop of ROM files and links
	class ROMDropTransferHandler extends TransferHandler {
		@Override
		public boolean canImport(TransferSupport support) {
			if (!screen.isCartridgeChangeEnabled()) return false;
			Transferable transf = support.getTransferable();
			if (!ROMTransferHandlerUtil.canAccept(transf)) return false;
			if (support.isDrop() && support.getUserDropAction() != LINK) support.setDropAction(COPY);
			return true;
		}
		@Override
		public boolean importData(TransferSupport support) {
			if (!canImport(support)) return false;
			Cartridge cart = ROMTransferHandlerUtil.importCartridgeData(support.getTransferable());
			if (cart == null) return false;
			// LINK Action means load Cartridge without auto power! :-)
			boolean autoPower = !support.isDrop() || support.getDropAction() != LINK;
			screen.cartridgeInsert(cart, autoPower);
			return true;
		}
		private static final long serialVersionUID = 1L;
	}

}
