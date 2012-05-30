// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.screen;

import general.av.video.VideoMonitor;
import general.av.video.VideoSignal;
import general.av.video.VideoStandard;
import general.board.Clock;
import general.board.ClockDriven;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import parameters.Parameters;
import pc.cartridge.FileROMChooser;
import pc.cartridge.URLROMChooser;
import pc.controls.AWTConsoleControls;
import utils.GraphicsDeviceHelper;
import utils.Terminator;
import atari.cartridge.Cartridge;
import atari.cartridge.CartridgeSocket;
import atari.controls.ConsoleControlsSocket;

public class Screen implements ClockDriven, VideoMonitor {
	
	public Screen(VideoSignal videoSignal, ConsoleControlsSocket controlsInput, CartridgeSocket cartridgeSocket) {
		this.fps = DEFAULT_FPS;
		this.videoSignal = videoSignal;
		this.consoleControlsSocket = controlsInput;
		this.cartridgeSocket = cartridgeSocket;
		init();
	}

	@Override
	public boolean nextLine(final int[] pixels, boolean vSynchSignal) {
		// Adjusts to the new signal state (on or off) as necessary
		if (!signalState(pixels != null))		// If signal is off, we are done
			return false;
		// Process new line received
		boolean vSynced = false;
		// Synchronize to avoid changing the standard while receiving lines / refreshing frame 
		synchronized (newDataMonitor) {
			if (line < signalHeight)
				System.arraycopy(pixels, 0, backBuffer, line * signalWidth, signalWidth);
			else 
				vSynced = maxLineExceeded();
			line++;
			if (videoStandardDetected == null) videoStandardDetectionLines++;
			if (vSynchSignal) {
				if (--VSYNCDetectionCount == 0) {
					if (videoStandardDetected == null) videoStandardDetectionNewFrame();
					vSynced = newFrame();
				}
			} else
				VSYNCDetectionCount = VSYNC_DETECTION;
		}
		return vSynced;
	}

	@Override
	public VideoStandard videoStandardDetected() {
		return videoStandardDetected;
	}

	@Override
	public void videoStandardDetectionStart() {
		videoStandardDetected = null;
		videoStandardDetectionFrameCount = videoStandardDetectionTotalLinesCount = 0;
	}

	@Override
	public int currentLine() {
		return line;
	}

	@Override
	public void synchOutput() {
		refresh();
	}

	@Override
	public void showOSD(String message) {
		osdMessage = message;
		osdFramesLeft = OSD_FRAMES;
	}
	
	@Override
	public void clockPulse() {
		synchOutput();
		// If in "On Demand" mode (fps < 0) then just wait for the next frame to interrupt the sleep, but no more than 2 frames
		if (fps < 0 && !Thread.interrupted()) try { Thread.sleep(1000 / 60 * 2,  0); } catch (InterruptedException e) { /* Awake! */ };
	}

	public void powerOn(){
		SwingUtilities.invokeLater(new Runnable() {  @Override public void run() {
			fullScreen(FULLSCREEN);
			paintLogo();
			clock.go();
		}});
	}

	public void p1ControlsMode(boolean state) {
		toolkitControls.p1ControlsMode(state);
	}

	public void cartridgeChangeEnabled(boolean state) {
		cartridgeChangeEnabled = state;
	}

	public boolean isCartridgeChangeEnabled() {
		return cartridgeChangeEnabled;
	}

	public void cartridgeInsert(Cartridge cart, boolean autoPower) {
		cartridgeSocket.insert(cart, autoPower);
		if (window.isVisible())
			window.requestFocus();
	};

	private boolean newFrame() {
		if (debug > 0) window.setTitle(BASE_TITLE + " - " + line + " lines");
		if (line < signalHeight - VSYNC_TOLERANCE) return false;
		// Copy only the contents needed (displayWidth x displayHight) to the frontBuffer
		arrayCopyWithStride(
				backBuffer, displayOriginY * signalWidth + displayOriginX, 
				frontBuffer, 0, displayWidth * displayHeight, 
				displayWidth, signalWidth
		);
		if (fps < 0) clock.interrupt();
		cleanBackBuffer();
		line = 0;
		return true;
	}

	private boolean maxLineExceeded() {
		if (line > signalHeight + VSYNC_TOLERANCE) {
			if (debug > 0) System.out.println("Display maximum scanlines exceeded, line: " + line);
			return newFrame();
		}
		return false;
	}
	
	private boolean signalState(boolean state) {
		signalOn = state;
		if (signalOn)
			adjustToVideoSignal();
		else 
			adjustToVideoSignalOff();
		return signalOn;
	}

	private void cleanBackBuffer() {
		// Clear screen if in debug mode, and put a nice green for detection of undrawn lines
		if (debug > 0) Arrays.fill(backBuffer, Color.GREEN.getRGB());		 
	}

	private void videoStandardDetectionNewFrame() {
		int linesCount = videoStandardDetectionLines;
		videoStandardDetectionLines = 0;
		// Only consider frames with linesCount in range
		if (linesCount < 250 || linesCount > 325) return;
		videoStandardDetectionTotalLinesCount += linesCount;
		if (++videoStandardDetectionFrameCount < 4) return;
		int averageLPF = videoStandardDetectionTotalLinesCount / videoStandardDetectionFrameCount;
		videoStandardDetected = averageLPF < 290 ? VideoStandard.NTSC : VideoStandard.PAL;
	}

	private void openWindow() {
		GraphicsDevice dev = GraphicsDeviceHelper.defaultScreenDevice(); 
		if (dev.isFullScreenSupported()) dev.setFullScreenWindow(null);
		fullWindow.setVisible(false);
		window.setVisible(true);
		fullScreen = false;
		canvas = window;
		canvasSetRenderingMode();
		float scX = canvas.getDefaultOpenningScaleX(displayWidth, displayHeight);
		setDisplayScale(scX, scX / DEFAULT_SCALE_ASPECT_X);
		canvasUpdateSize();
		canvasCenter();
	}

	private boolean openFullWindow() {
		GraphicsDevice dev = GraphicsDeviceHelper.defaultScreenDevice(); 
		if (!dev.isFullScreenSupported()) return false;
		window.setVisible(false);
		fullWindow.setVisible(true);
		dev.setFullScreenWindow(fullWindow);
		fullScreen = true;
		canvas = fullWindow;
		canvasSetRenderingMode();
		float scX = canvas.getDefaultOpenningScaleX(displayWidth, displayHeight);
		setDisplayScale(scX, scX / DEFAULT_SCALE_ASPECT_X);
		canvasUpdateSize();
		canvasCenter();
		return true;
	}

	private void canvasUpdateSize() {
		synchronized (refreshMonitor) {
			Dimension size = new Dimension((int) (displayWidth * displayScaleX), (int) (displayHeight * displayScaleY));
			if (!fullScreen) {
				window.canvasSize(size);
				window.canvasMinimumSize(new Dimension((int) (displayWidth * DEFAULT_SCALE_X / DEFAULT_SCALE_Y), displayHeight));
			} else {
				fullWindow.canvasSize(size);
			}
		}
	}

	private void canvasCenter() {
		if (canvas == null) return;
		synchronized (refreshMonitor) {
			canvas.canvasCenter();
		}
	}

	private void canvasSetRenderingMode() {
		synchronized (refreshMonitor) {
			canvas.canvasSetRenderingMode();
		}
	}

	private Graphics2D canvasGraphics() {
		if (canvas == null) return null;
		Graphics2D canvasGraphics = canvas.canvasGraphics();
		canvasGraphics.setComposite(AlphaComposite.Src);
		// Adjusts the Render Quality if needed
		if (canvasGraphics != null) 
			canvasGraphics.setRenderingHint(
				RenderingHints.KEY_RENDERING, 
				qualityRendering ? RenderingHints.VALUE_RENDER_QUALITY : RenderingHints.VALUE_RENDER_DEFAULT
			);
		return canvasGraphics;
	}

	private void canvasFinish(Graphics2D graphics) {
		canvas.canvasFinishGraphics(graphics);
	}

	private void buildGUI() {
		// Create the window for Windowed display
		window = new ScreenWindow(this);
		window.setTitle(BASE_TITLE);
		try {
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		} catch(AccessControlException ex) {
			// Ignore. Cannot set DefaultCloseOperation if running from an Applet
		}
		// Create the window for FullScreen display
		fullWindow = new ScreenFullWindow();
		fullWindow.setTitle(BASE_TITLE);
		// Prepare the Logo image
		try {
			logoIcon = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/Logo.png");
		} catch (IOException e) {}
		// Prepare the OSD paint component
		osdComponent = new JButton();
		osdComponent.setForeground(Color.GREEN);
		osdComponent.setBackground(new Color(0x50000000, true));
		osdComponent.setFont(new Font(osdComponent.getName(), Font.BOLD, 15));
		osdComponent.setBorder(new EmptyBorder(5, 12, 5, 12));
		// Prepare CRT mode 2 texture
		scanlinesTextureImage = new BufferedImage(2048, 1280, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g = scanlinesTextureImage.createGraphics();
		g.setColor(new Color((int)(SCANLINES_STRENGTH * 255) << 24, true));
		for(int i = 1; i < scanlinesTextureImage.getHeight(); i += 2)
			g.drawLine(0, i, scanlinesTextureImage.getWidth(), i);
		g.dispose();
		if (SCANLINES_ACCELERATION >= 0) scanlinesTextureImage.setAccelerationPriority(SCANLINES_ACCELERATION);
		// Prepare CRT mode 3 and 4 composition
		crtTriadComposite = new CRTTriadComposite();
		// Prepare intermediate image for CRT modes or OSD rendering in SingleBuffer mode
		intermFrameImage = new BufferedImage(2048, 1280, BufferedImage.TYPE_INT_RGB);
		if (IMTERM_FRAME_ACCELERATION >= 0) intermFrameImage.setAccelerationPriority(IMTERM_FRAME_ACCELERATION);
	}

	private void init() {
		buildGUI();	 	
		videoSignal.connectMonitor(this);
		adjustToVideoSignal();
		setDisplayDefaultSize();	
		clock = new Clock(this, fps);
		toolkitControls = new AWTConsoleControls(consoleControlsSocket, this, new Component[] {window, fullWindow});
		new ScreenControlsAdapter(this, new Component[] {window, fullWindow});
		cleanBackBuffer();
		newFrame();
	}

	private void adjustToVideoSignal() {
		if (signalStandard == videoSignal.standard()) return;
		adjustToVideoStandard(videoSignal.standard());
	}

	private void adjustToVideoStandard(VideoStandard videoStandard) {
		// Synchronize on nextLine() and refresh() monitors to avoid changing the standard while receiving lines / refreshing frame 
		synchronized (refreshMonitor) { synchronized (newDataMonitor) {
			signalStandard = videoSignal.standard();
			signalWidth = videoStandard.width;
			signalHeight = videoStandard.height;
			setDisplaySize(displayWidth, displayHeightPct);
			setDisplayOrigin(displayOriginX, displayOriginYPct);
			backBuffer = new int[signalWidth * signalHeight];
			frontBuffer = new int[signalWidth * signalHeight];
			frameImage = new BufferedImage(signalWidth, signalHeight, BufferedImage.TYPE_INT_ARGB);
			if (FRAME_ACCELERATION >= 0) frameImage.setAccelerationPriority(FRAME_ACCELERATION);
		}}
	}

	private void adjustToVideoSignalOff() {
		VSYNCDetectionCount = VSYNC_DETECTION;
		line = 0;
		canvas.canvasClear();
		paintLogo();
	}

	private void paintLogo() {
		synchronized (refreshMonitor) {
			Graphics2D canvasGraphics = canvasGraphics();
			if (canvasGraphics == null) return;
			int w = canvas.canvasEffectiveSize().width;
			int h = canvas.canvasEffectiveSize().height;
			canvasGraphics.setBackground(Color.BLACK);
			canvasGraphics.clearRect(0, 0, w, h);
			canvasGraphics.drawImage(
				logoIcon, 
				(w - logoIcon.getWidth(null)) / 2, 
				(h - logoIcon.getHeight(null)) / 2,
				null
			);
			paintOSD(canvasGraphics);
			canvasFinish(canvasGraphics);
		}
	}
		
	private void paintOSD(Graphics2D canvasGraphics) {
		if (--osdFramesLeft < 0) return;
		canvasGraphics.setComposite(AlphaComposite.SrcOver);
		osdComponent.setText(osdMessage);
		Dimension s = osdComponent.getPreferredSize();
		SwingUtilities.paintComponent(
			canvasGraphics, osdComponent, canvas.canvasContainer(), 
			(canvas.canvasEffectiveSize().width - s.width) - 12, 12, 
			s.width, s.height
		);
	}

	private void refresh() {
		if (!signalOn) {
			paintLogo();
			return;
		}
		// Synchronize to avoid changing image properties while refreshing frame
		synchronized (refreshMonitor) {
			Graphics2D canvasGraphics = canvasGraphics();
			if (canvasGraphics == null) return;
			// Get the entire Canvas
			int canvasEffectiveWidth = canvas.canvasEffectiveSize().width;
			int canvasEffectiveHeight = canvas.canvasEffectiveSize().height;
			// CRT mode 3 OR no MultiBuffering active and needs to superimpose (CRT mode 1, 2 or OSD)
			// draw frameImage to intermediate image with composite then transfer to Canvas
			if (crtMode >= 3 || (MULTI_BUFFERING < 2 && (osdFramesLeft >= 0 || crtMode > 0))) {
				int intermWidth = Math.min(canvasEffectiveWidth, 2048);
				int intermHeight = Math.min(canvasEffectiveHeight, 1280);
				Graphics2D intermGraphics = intermFrameImage.createGraphics();
				// Renders to intermediate image
				renderFrame(intermGraphics, intermWidth, intermHeight, true);
				// If CRT mode 2, alpha-superimpose the prepared scanlines image
				if (crtMode == 2)
					renderScanlines(intermGraphics, intermWidth, intermHeight);
				// If CRT mode 3 or 4, sets the CRTTriadComposite and rewrite
				if (crtMode >= 3) {
					intermGraphics.setComposite(crtTriadComposite);
					intermGraphics.drawImage(
						intermFrameImage,
						0, 0, intermWidth, intermHeight,
						0, 0, intermWidth, intermHeight,
						null);
				}
				paintOSD(intermGraphics);
				intermGraphics.dispose();
				// Then transfer to Canvas
				canvasGraphics.drawImage(
					intermFrameImage,
					0, 0, intermWidth, intermHeight,
					0, 0, intermWidth, intermHeight,
					null);
			} else {
				// Renders directly to Canvas
				renderFrame(canvasGraphics, canvasEffectiveWidth, canvasEffectiveHeight, crtMode > 0);
				// If CRT mode 2, alpha-superimpose the prepared scanlines image
				if (crtMode == 2)
					renderScanlines(canvasGraphics, canvasEffectiveWidth, canvasEffectiveHeight);
				paintOSD(canvasGraphics);
			}
			canvasFinish(canvasGraphics);
		}
	}

	private void renderFrame(Graphics2D graphics, int canvasWidth, int canvasHeight, boolean clear) {
		if (clear) {
			graphics.setBackground(Color.BLACK);
			graphics.clearRect(0, 0, canvasWidth, canvasHeight);
		}
		// If CRT mode 1, 2 or 4, set composite for last and new frame over each other, and draw old frame
		if (crtMode > 0 && crtMode != 3) {
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, CRT_RETENTION_ALPHA));
			// Draw old frame
			graphics.drawImage(frameImage, 0, 0, canvasWidth, canvasHeight, 0, 0, displayWidth, displayHeight, null);
		}
		// Update the image to draw with contents stored in the frontBuffer
		frameImage.getRaster().setDataElements(0, 0, displayWidth, displayHeight, frontBuffer);
		// Draw new frame
		graphics.drawImage(frameImage, 0, 0, canvasWidth, canvasHeight, 0, 0, displayWidth, displayHeight, null);
	}

	private void renderScanlines(Graphics2D graphics, int canvasWidth, int canvasHeight) {
		graphics.setComposite(AlphaComposite.SrcOver);
		graphics.drawImage( scanlinesTextureImage, 0, 0, canvasWidth, canvasHeight, 0, 0, canvasWidth, canvasHeight, null);
	}

	private void setDisplayDefaultSize() {
		setDisplaySize(DEFAULT_WIDTH, DEFAULT_HEIGHT_PCT);
		setDisplayOrigin(DEFAULT_ORIGIN_X, DEFAULT_ORIGIN_Y_PCT);
		setDisplayScale(DEFAULT_SCALE_X, DEFAULT_SCALE_Y);
		canvasCenter();
	}

	private void setDisplayOrigin(int x, double yPct) {
		displayOriginX = x;
		if (displayOriginX < 0) displayOriginX = 0;
		else if (displayOriginX > signalWidth - displayWidth) displayOriginX = signalWidth - displayWidth;
		displayOriginYPct = yPct;
		if (displayOriginYPct < 0) displayOriginYPct = 0;
		else if ((displayOriginYPct / 100 * signalHeight) > signalHeight - displayHeight) displayOriginYPct = ((double)signalHeight - displayHeight) / signalHeight * 100; 
		displayOriginY = (int) (displayOriginYPct / 100 * signalHeight);
	}

	private void setDisplaySize(int width, double heightPct) {
		displayWidth = width;
		if (displayWidth < 10) displayWidth = 10;
		else if (displayWidth > signalWidth) displayWidth = signalWidth;
		displayHeightPct = heightPct;
		if (displayHeightPct < 10) displayHeightPct = 10;
		else if (displayHeightPct > 100) displayHeightPct = 100;
		displayHeight = (int) (displayHeightPct / 100 * signalHeight);
		setDisplayOrigin(displayOriginX, displayOriginYPct);
		canvasUpdateSize();
	}

	private void setDisplayScale(float x, float y) {
		displayScaleX = x;
		if (displayScaleX < 1) displayScaleX = 1;
		displayScaleY = y;
		if (displayScaleY < 1) displayScaleY = 1;
		canvasUpdateSize();
	}

	private void setDisplayScaleDefaultAspect(float y) {
		int scaleY = (int) y;
		if (scaleY < 1) scaleY = 1;
		setDisplayScale(scaleY * DEFAULT_SCALE_ASPECT_X, scaleY);
	}

	private void loadCartridgeFromFile(boolean autoPower) {
		if (cartridgeChangeDisabledWarning()) return;
		if (fullScreen) fullScreen(false);
		Cartridge cart = FileROMChooser.chooseFile();
		if (cart != null) cartridgeInsert(cart, autoPower);
	}

	private void loadCartridgeFromURL(boolean autoPower) {
		if (cartridgeChangeDisabledWarning()) return;
		if (fullScreen) fullScreen(false);
		Cartridge cart = URLROMChooser.chooseURL();
		if (cart != null) cartridgeInsert(cart, autoPower);
	}

	private void loadCartridgeEmpty() {
		if (cartridgeChangeDisabledWarning()) return;
		cartridgeSocket.insert(null, false);
	}

	private boolean cartridgeChangeDisabledWarning() {
		if (!isCartridgeChangeEnabled()) {
			showOSD("Cartridge change is disabled");
			return true;
		}
		return false;
	}

	private void fullScreen(boolean state) {
		synchronized (refreshMonitor) {
			if (state)
				if (openFullWindow()) return;
			openWindow();
		}
	}

	private void crtModeToggle() {
		synchronized (refreshMonitor) {
			crtMode++;
			if (crtMode > 4) crtMode = 0;
			showOSD(crtMode == 0 ? "CRT mode off" : "CRT mode " + crtMode);
		}
	}

	public void controlStateChanged(Control control, boolean state) {
		// Toggles
		if (!state) return;
		switch(control) {
		case LOAD_CARTRIDGE_FILE:
				loadCartridgeFromFile(true); break;
			case LOAD_CARTRIDGE_FILE_NO_AUTO_POWER:
				loadCartridgeFromFile(false); break;
			case LOAD_CARTRIDGE_URL:
				loadCartridgeFromURL(true); break;
			case LOAD_CARTRIDGE_URL_NO_AUTO_POWER:
				loadCartridgeFromURL(false); break;
			case LOAD_CARTRIDGE_EMPTY:
				loadCartridgeEmpty(); break;
			case FULL_SCREEN:
				fullScreen(!fullScreen); break;
			case QUALITY:
				qualityRendering = !qualityRendering;
				showOSD(qualityRendering ? "Filter ON" : "Filter OFF");
				break;
			case CRT_MODES:
				crtModeToggle(); break;
			case DEBUG:
				debug++;
				if (debug > 4) debug = 0;
				break;
			case EXIT:
				if (fullScreen) {
					fullScreen(false);
					break;
				}
				// Close program
				Terminator.terminate();
			case HELP:
				window.consolePanelWindow.toggle();	break;
			case ORIGIN_X_MINUS:
				setDisplayOrigin(displayOriginX + 1, displayOriginYPct); break;
			case ORIGIN_X_PLUS:		
				setDisplayOrigin(displayOriginX - 1, displayOriginYPct); break;
			case ORIGIN_Y_MINUS:
				setDisplayOrigin(displayOriginX, displayOriginYPct + 0.5); break;
			case ORIGIN_Y_PLUS:
				setDisplayOrigin(displayOriginX, displayOriginYPct - 0.5); break;
			case WIDTH_MINUS:
				setDisplaySize(displayWidth - 1, displayHeightPct); break;
			case WIDTH_PLUS:		
				setDisplaySize(displayWidth + 1, displayHeightPct); break;
			case HEIGHT_MINUS:
				setDisplaySize(displayWidth, displayHeightPct - 0.5); break;
			case HEIGHT_PLUS:
				setDisplaySize(displayWidth, displayHeightPct + 0.5); break;
			case SCALE_X_MINUS:
				setDisplayScale(displayScaleX - 0.5f, displayScaleY); break;
			case SCALE_X_PLUS:		
				setDisplayScale(displayScaleX + 0.5f, displayScaleY); break;
			case SCALE_Y_MINUS:
				setDisplayScale(displayScaleX, displayScaleY - 0.5f); break;
			case SCALE_Y_PLUS:
				setDisplayScale(displayScaleX, displayScaleY + 0.5f); break;
			case SIZE_MINUS:
				setDisplayScaleDefaultAspect(displayScaleY - 1); break;
			case SIZE_PLUS:
				setDisplayScaleDefaultAspect(displayScaleY + 1); break;
			case SIZE_DEFAULT:
				setDisplayDefaultSize();
		}
	}

	private static void arrayCopyWithStride(int[] src, int srcPos, int dest[], int destPos, int length, int chunk, int stride) {
		int total = 0;
		while(total < length) {
			System.arraycopy(src, srcPos, dest, destPos, chunk);
			srcPos += stride;
			destPos += chunk;
			total += chunk;
		}
	}
	

	public Clock clock;
	public final ConsoleControlsSocket consoleControlsSocket;

	private AWTConsoleControls toolkitControls;
	private boolean cartridgeChangeEnabled = CARTRIDGE_CHANGE;
	
	private final VideoSignal videoSignal;
	private final CartridgeSocket cartridgeSocket;
	private final double fps;
	
	private VideoStandard signalStandard;
	private int signalWidth;
	private int signalHeight;

	private VideoStandard videoStandardDetected;
	private int videoStandardDetectionFrameCount;
	private int videoStandardDetectionTotalLinesCount;
	private int videoStandardDetectionLines = 0;

	private int[] backBuffer;
	private int[] frontBuffer;

	private int displayWidth;
	private int displayHeight;
	private double displayHeightPct;
	private int displayOriginX;
	private int displayOriginY;
	private double displayOriginYPct;
	private float displayScaleX;
	private float displayScaleY;
	
	private boolean signalOn = false;
	private boolean fullScreen = false;
	
	private int osdFramesLeft = -1;
	private String osdMessage; 
	private JButton osdComponent;
	
	private boolean qualityRendering = QUALITY_RENDERING;
	private int crtMode = CRT_MODE;

	private int debug = 0;
	
	private int line = 0;
	
	private ScreenWindow window;
	private ScreenFullWindow fullWindow;

	private DisplayCanvas canvas;

	private BufferedImage frameImage;
	
	private BufferedImage scanlinesTextureImage;
	private CRTTriadComposite crtTriadComposite;
	private BufferedImage intermFrameImage;
	
	private Image logoIcon;
	
	private int VSYNCDetectionCount = 0;

	private String newDataMonitor = "nextLineMonitor";		// Used only for synchronization
	private String refreshMonitor = "refreshMonitor";		// Used only for synchronization

	private static final String BASE_TITLE = "javatari";

	private static final int VSYNC_DETECTION = 2;
	private static final int VSYNC_TOLERANCE = Parameters.SCREEN_VSYNC_TOLERANCE;
	
	public static final double DEFAULT_FPS = Parameters.SCREEN_DEFAULT_FPS;

	public static final int      DEFAULT_ORIGIN_X = Parameters.SCREEN_DEFAULT_ORIGIN_X;
	public static final double   DEFAULT_ORIGIN_Y_PCT = Parameters.SCREEN_DEFAULT_ORIGIN_Y_PCT;		// Percentage of height
	public static final int      DEFAULT_WIDTH = Parameters.SCREEN_DEFAULT_WIDTH;
	public static final double   DEFAULT_HEIGHT_PCT = Parameters.SCREEN_DEFAULT_HEIGHT_PCT;			// Percentage of height
	public static final float    DEFAULT_SCALE_X = Parameters.SCREEN_DEFAULT_SCALE_X;
	public static final float    DEFAULT_SCALE_Y = Parameters.SCREEN_DEFAULT_SCALE_Y;
	public static final float    DEFAULT_SCALE_ASPECT_X = Parameters.SCREEN_DEFAULT_SCALE_ASPECT_X;
	public static final int      OSD_FRAMES = Parameters.SCREEN_OSD_FRAMES;
	public static final boolean  QUALITY_RENDERING = Parameters.SCREEN_QUALITY_RENDERING;
	public static final int      CRT_MODE = Parameters.SCREEN_CRT_MODE;
	public static final float    CRT_RETENTION_ALPHA = Parameters.SCREEN_CRT_RETENTION_ALPHA;
	public static final float    SCANLINES_STRENGTH = Parameters.SCREEN_SCANLINES_STRENGTH;
	public static final int      MULTI_BUFFERING = Parameters.SCREEN_MULTI_BUFFERING;
	public static final boolean  PAGE_FLIPPING = Parameters.SCREEN_PAGE_FLIPPING;
	public static final int      BUFFER_VSYNC = Parameters.SCREEN_BUFFER_VSYNC;
	public static final float    FRAME_ACCELERATION = Parameters.SCREEN_FRAME_ACCELERATION;
	public static final float    IMTERM_FRAME_ACCELERATION = Parameters.SCREEN_INTERM_FRAME_ACCELERATION;
	public static final float    SCANLINES_ACCELERATION = Parameters.SCREEN_SCANLINES_ACCELERATION;
	private static final boolean CARTRIDGE_CHANGE = Parameters.SCREEN_CARTRIDGE_CHANGE;
	public static final boolean  FULLSCREEN = Parameters.SCREEN_FULLSCREEN;

	public static final long serialVersionUID = 0L;

	public static enum Control {
		WIDTH_PLUS, HEIGHT_PLUS, 
		WIDTH_MINUS, HEIGHT_MINUS, 
		ORIGIN_X_PLUS, ORIGIN_Y_PLUS, 
		ORIGIN_X_MINUS, ORIGIN_Y_MINUS, 
		SCALE_X_PLUS, SCALE_Y_PLUS, 
		SCALE_X_MINUS, SCALE_Y_MINUS, 
		SIZE_PLUS, SIZE_MINUS, 
		SIZE_DEFAULT,
		LOAD_CARTRIDGE_FILE, LOAD_CARTRIDGE_FILE_NO_AUTO_POWER,
		LOAD_CARTRIDGE_URL, LOAD_CARTRIDGE_URL_NO_AUTO_POWER,
		LOAD_CARTRIDGE_EMPTY,
		FULL_SCREEN, QUALITY, CRT_MODES,
		HELP, DEBUG,
		EXIT
	}

}


// Simulates the TV display at the sub pixel level (triads) 
class CRTTriadComposite implements Composite {
	private int[] data = new int[3000];
	public CompositeContext context = new CompositeContext() {
		@Override
		public void dispose() {
		}
		@Override
		public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
			int w = Math.min(src.getWidth(), dstOut.getWidth());
			int h = Math.min(src.getHeight(), dstOut.getHeight());
			for(int y = 0; y < h; y++) {
				src.getDataElements(src.getMinX(), src.getMinY() + y, w, 1, data);
				int c;
				for(c = 0; c < w - 2; c += 3) {
					data[c]   = (int) Math.min((data[c] & 0xff0000) * 1.30f, 0xff0000) & 0xff0000;
					data[c+1] = (int) Math.min((data[c+1] & 0xff00) * 1.30f, 0xff00) & 0xff00;
					data[c+2] = (int) Math.min((data[c+2] & 0xff) * 1.35f, 0xff);
				}
				if (c < w) data[c] = (int) Math.min((data[c] & 0xff0000) * 1.30f, 0xff0000) & 0xff0000;
				if (c < w - 1) data[c+1] = (int) Math.min((data[c+1] & 0xff00) * 1.30f, 0xff00) & 0xff00;
				dstOut.setDataElements(dstOut.getMinX(), dstOut.getMinY() + y, w, 1, data);
			}
		}
	};
	@Override
	public CompositeContext createContext(ColorModel srcColorModel,	ColorModel dstColorModel, RenderingHints hints) {
		return context;
	}
}