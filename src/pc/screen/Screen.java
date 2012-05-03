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
import java.net.URL;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import parameters.Parameters;
import pc.AWTConsoleControls;
import pc.FileCartridgeReader;
import utils.GraphicsDeviceHelper;
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
			if (vSynchSignal) {
				if (--VSYNCDetectionCount == 0)
					vSynced = newFrame();
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
	public int currentLine() {
		return line;
	}

	@Override
	public void synchOutput() {
		// Saves data from the frontBuffer for rendering, so the next frame can change it without visual artifacts
		System.arraycopy(frontBuffer, 0, frameBuffer, 0, displayWidth * displayHeight);
		refresh();
	}

	@Override
	public void showOSD(String message) {
		osdMessage = message;
		osdFramesLeft = OSD_FRAMES;
	}
	
	public void p1ControlsMode(boolean state) {
		toolkitControls.p1ControlsMode(state);
	}

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
		if (videoStandardDetected != null) videoStandardDetectionNewFrame(line);
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
		if (state) {
			adjustToVideoSignal();
			// If signal was off before, start a bew VideoStandard detection
			if (!signalOn) videoStandardDetectionReset();
		}
		signalOn = state;
		// Paints the Logo if the signal is off
		if (!signalOn) {
			canvas.canvasClear();
			paintLogo();
		}
		return signalOn;
	}

	private void cleanBackBuffer() {
		// Clear screen if in debug mode, and put a nice green for detection of undrawn lines
		if (debug > 0) Arrays.fill(backBuffer, Color.GREEN.getRGB());		 
	}

	public void powerOn(){
		SwingUtilities.invokeLater(new Runnable() {  @Override public void run() {
			fullScreen(false);
			paintLogo();
			clock.go();
		}});
	}

	private void videoStandardDetectionReset() {
		videoStandardDetected = null;
		videoStandardDetectionFrameCount = videoStandardDetectionLinesCount = 0;
	}

	private void videoStandardDetectionNewFrame(int linesCount) {
		if (videoStandardDetectionFrameCount > 20) return;
		videoStandardDetectionLinesCount += linesCount;
		if (++videoStandardDetectionFrameCount > 20) {
			if (videoStandardDetectionLinesCount / videoStandardDetectionFrameCount > 275)
				videoStandardDetected = VideoStandard.PAL;
			else
				videoStandardDetected = VideoStandard.NTSC;
		}
	}

	private void openWindow() {
		GraphicsDevice dev = GraphicsDeviceHelper.defaultScreenDevice(); 
		if (dev.isFullScreenSupported())
			dev.setFullScreenWindow(null);
		fullWindow.setVisible(false);
		window.setVisible(true);
		fullScreen = false;
		canvas = window;
		canvasSetRenderingMode();
		float scX = canvas.getDefaultOpenningScaleX(displayWidth, displayHeight);
		setDisplayScale(scX, scX / DEFAULT_SCALE_ASPECT_X);
		canvasUpdateSize();
	}

	private void openFullWindow() {
		GraphicsDevice dev = GraphicsDeviceHelper.defaultScreenDevice(); 
		if (dev.isFullScreenSupported()) {
			window.setVisible(false);
			fullWindow.setVisible(true);
			dev.setFullScreenWindow(fullWindow);
			fullScreen = true;
			canvas = fullWindow;
			canvasSetRenderingMode();
			float scX = canvas.getDefaultOpenningScaleX(displayWidth, displayHeight);
			setDisplayScale(scX, scX / DEFAULT_SCALE_ASPECT_X);
			canvasUpdateSize();
		}
	}

	public void canvasUpdateSize() {
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

	private void canvasSetRenderingMode() {
		synchronized (refreshMonitor) {
			canvas.canvasSetRenderingMode();
		}
	}

	private Graphics2D canvasGraphics() {
		if (canvas == null) return null;
		Graphics2D canvasGraphics = canvas.canvasGraphics();
		if (canvasGraphics != null) 
			// Adjusts the Render Quality
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
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Create the window for FullScreen display
		fullWindow = new ScreenFullWindow();
		fullWindow.setTitle(BASE_TITLE);
		// Prepare the Logo image
		URL url = ClassLoader.getSystemResource("pc/screen/images/Logo.png");
		logoIcon = new ImageIcon(url).getImage();
		// Prepare the OSD paint compoment
		osdComponent = new JButton();
		osdComponent.setForeground(Color.GREEN);
		osdComponent.setBackground(new Color(0x50000000, true));
		osdComponent.setFont(new Font(osdComponent.getName(), Font.BOLD, 15));
		osdComponent.setBorder(new EmptyBorder(5, 12, 5, 12));
		// Prepare Scanlines mode 1 texture
		scanlines1TextureImage = new BufferedImage(2048, 1280, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g = scanlines1TextureImage.createGraphics();
		g.setColor(new Color((int)(SCANLINES1_STRENGTH * 255) << 24, true));
		for(int i = 1; i < scanlines1TextureImage.getHeight(); i += 2)
			g.drawLine(0, i, scanlines1TextureImage.getWidth(), i);
		g.dispose();
		if (SCANLINES1_ACCELERATION >= 0) scanlines1TextureImage.setAccelerationPriority(SCANLINES1_ACCELERATION);
		// Prepare Scanlines mode 2 composition 
		scanlines2Composite = new TVTriadComposite();
		// Prepare intermediate image for Scanlines or OSD rendering in SingleBuffer mode
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
			frameBuffer = new int[signalWidth * signalHeight];
			frameImage = new BufferedImage(signalWidth, signalHeight, BufferedImage.TYPE_INT_RGB);
			if (FRAME_ACCELERATION >= 0) frameImage.setAccelerationPriority(FRAME_ACCELERATION);
			// showOSD(videoStandard.name);
		}}
	}

	private void paintLogo() {
		synchronized (refreshMonitor) {
			Graphics2D canvasGraphics = canvasGraphics();
			if (canvasGraphics == null) return;
			canvasGraphics.setColor(Color.BLACK);
			int w = canvas.canvasEffectiveSize().width;
			int h = canvas.canvasEffectiveSize().height;
			canvasGraphics.fillRect(0, 0, w, h);
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
			// Update the image to draw with contents stored in the frameBuffer
			frameImage.getRaster().setDataElements(0, 0, displayWidth, displayHeight, frameBuffer);
			// Get the entire Canvas
			int canvasEffectiveWidth = canvas.canvasEffectiveSize().width;
			int canvasEffectiveHeight = canvas.canvasEffectiveSize().height;
			// Scanlines mode 2 OR no MultiBuffering active and needs to superimpose (Scanlines mode 1 or OSD)
			// draw frameImage to intermediate image with composite then transfer to Canvas
			if (scanlinesRendering == 2 || (MULTI_BUFFERING < 2 && (osdFramesLeft >= 0 || scanlinesRendering == 1))) { 
				Graphics2D intermGraphics = intermFrameImage.createGraphics();
				// If Scanlines mode 2, sets the Composite
				if (scanlinesRendering == 2) 
					intermGraphics.setComposite(scanlines2Composite);
				intermGraphics.drawImage(
					frameImage, 
					0, 0, canvasEffectiveWidth, canvasEffectiveHeight, 
					0, 0, displayWidth, displayHeight,
					null);
				if (scanlinesRendering == 2) 
					intermGraphics.setComposite(AlphaComposite.SrcOver);
				// If Scanlines mode 1, alpha-superimpose the prepared scanlines image
				if (scanlinesRendering == 1) 
					intermGraphics.drawImage(
						scanlines1TextureImage, 
						0, 0, canvasEffectiveWidth, canvasEffectiveHeight, 
						0, 0, canvasEffectiveWidth, canvasEffectiveHeight, 
						null);
				paintOSD(intermGraphics);
				intermGraphics.dispose();
				// Then finally transfer to Canvas
				canvasGraphics.drawImage(
					intermFrameImage, 
					0, 0, canvasEffectiveWidth, canvasEffectiveHeight, 
					0, 0, canvasEffectiveWidth, canvasEffectiveHeight,
					null);
			} else {
				// Renders directly to Canvas
				canvasGraphics.drawImage(
					frameImage, 
					0, 0, canvasEffectiveWidth, canvasEffectiveHeight, 
					0, 0, displayWidth, displayHeight,
					null);
				// If Scanlines mode 1, alpha-superimpose the prepared scanlines image
				if (scanlinesRendering == 1) 
					canvasGraphics.drawImage(
						scanlines1TextureImage, 
						0, 0, canvasEffectiveWidth, canvasEffectiveHeight, 
						0, 0, canvasEffectiveWidth, canvasEffectiveHeight, 
						null);
				paintOSD(canvasGraphics);
			}
			canvasFinish(canvasGraphics);
		}
	}
	
	@Override
	public void clockPulse() {
		synchOutput();
		// If in "On Demand" mode (fps < 0) then just wait for the next frame to interrupt the sleep, but no more than 2 frames
		if (fps < 0 && !Thread.interrupted()) try { Thread.sleep(1000 / 60 * 2,  0); } catch (InterruptedException e) { /* Awake! */ };
	}

	private void setDisplayDefaultSize() {
		setDisplaySize(DEFAULT_WIDTH, DEFAULT_HEIGHT_PCT);
		setDisplayOrigin(DEFAULT_ORIGIN_X, DEFAULT_ORIGIN_Y_PCT);
		setDisplayScale(DEFAULT_SCALE_X, DEFAULT_SCALE_Y);
	}

	public void setDisplayOrigin(int x, double yPct) {
		displayOriginX = x;
		if (displayOriginX < 0) displayOriginX = 0;
		else if (displayOriginX > signalWidth - displayWidth) displayOriginX = signalWidth - displayWidth;
		displayOriginYPct = yPct;
		if (displayOriginYPct < 0) displayOriginYPct = 0;
		else if ((displayOriginYPct / 100 * signalHeight) > signalHeight - displayHeight) displayOriginYPct = ((double)signalHeight - displayHeight) / signalHeight * 100; 
		displayOriginY = (int) (displayOriginYPct / 100 * signalHeight);
	}

	public void setDisplaySize(int width, double heightPct) {
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

	public void setDisplayScale(float x, float y) {
		displayScaleX = x;
		if (displayScaleX < 1) displayScaleX = 1;
		displayScaleY = y;
		if (displayScaleY < 1) displayScaleY = 1;
		canvasUpdateSize();
	}

	public void setDisplayScaleDefaultAspect(float y) {
		int scaleY = (int) y;
		if (scaleY < 1) scaleY = 1;
		setDisplayScale(scaleY * DEFAULT_SCALE_ASPECT_X, scaleY);
	}

	public void loadCartridge() {
		if (fullScreen) fullScreen(false);
		Cartridge cart = FileCartridgeReader.chooseFile();
		if (cart != null) cartridgeSocket.insert(cart);
	};

	private void fullScreen(boolean state) {
		synchronized (refreshMonitor) {
			if (state)
				openFullWindow();
			else
				openWindow();
		}
	}

	private void scanlinesToggleMode() {
		synchronized (refreshMonitor) {
			scanlinesRendering++; 
			if (scanlinesRendering > 2) scanlinesRendering = 0;
			canvasSetRenderingMode();
			showOSD(scanlinesRendering == 0 ? "Scanlines OFF" : "Scanlines " + scanlinesRendering);
		}
	}

	public void controlStateChanged(Control control, boolean state) {
		// Toggles
		if (!state) return;
		switch(control) {
			case LOAD_CARTRIDGE:
				loadCartridge();
				break;
			case FULL_SCREEN:
				fullScreen(!fullScreen);
				break;
			case QUALITY:
				qualityRendering = !qualityRendering;
				showOSD(qualityRendering ? "Filter ON" : "Filter OFF");
				break;
			case SCANLINES:
				scanlinesToggleMode();
				break;
			case VIDEO_STANDARD:
//				signalStandard = signalStandard.equals(VideoStandard.NTSC) ? VideoStandard.PAL : VideoStandard.NTSC;
//				adjustToVideoStandard(signalStandard);
				break;
			case DEBUG:
				debug++;
				if (debug > 4) debug = 0;
				break;
			case EXIT:
				if (fullScreen) {
					fullScreen(false);
					break;
				}
				System.out.println("<<<<<<<<<<<<  EXIT   >>>>>>>>>>>>>");
				System.exit(0);
				break;
			case HELP:
				window.consolePanelWindow.toggle();
				break;
			case ORIGIN_X_MINUS:
				setDisplayOrigin(displayOriginX + 1, displayOriginYPct);
				break;
			case ORIGIN_X_PLUS:		
				setDisplayOrigin(displayOriginX - 1, displayOriginYPct);
				break;
			case ORIGIN_Y_MINUS:
				setDisplayOrigin(displayOriginX, displayOriginYPct + 0.5);
				break;
			case ORIGIN_Y_PLUS:
				setDisplayOrigin(displayOriginX, displayOriginYPct - 0.5);
				break;
			case WIDTH_MINUS:
				setDisplaySize(displayWidth - 1, displayHeightPct);
				break;
			case WIDTH_PLUS:		
				setDisplaySize(displayWidth + 1, displayHeightPct);
				break;
			case HEIGHT_MINUS:
				setDisplaySize(displayWidth, displayHeightPct - 0.5);
				break;
			case HEIGHT_PLUS:
				setDisplaySize(displayWidth, displayHeightPct + 0.5);
				break;
			case SCALE_X_MINUS:
				setDisplayScale(displayScaleX - 0.5f, displayScaleY);
				break;
			case SCALE_X_PLUS:		
				setDisplayScale(displayScaleX + 0.5f, displayScaleY);
				break;
			case SCALE_Y_MINUS:
				setDisplayScale(displayScaleX, displayScaleY - 0.5f);
				break;
			case SCALE_Y_PLUS:
				setDisplayScale(displayScaleX, displayScaleY + 0.5f);
				break;
			case SIZE_MINUS:
				setDisplayScaleDefaultAspect(displayScaleY - 1);
				break;
			case SIZE_PLUS:
				setDisplayScaleDefaultAspect(displayScaleY + 1);
				break;
			case SIZE_DEFAULT:
				setDisplayDefaultSize();
				break;
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
	
	private final VideoSignal videoSignal;
	private final CartridgeSocket cartridgeSocket;
	private final double fps;
	
	private AWTConsoleControls toolkitControls;
	
	private VideoStandard signalStandard;
	private int signalWidth;
	private int signalHeight;

	private VideoStandard videoStandardDetected;
	private int videoStandardDetectionFrameCount;
	private int videoStandardDetectionLinesCount;

	private int[] backBuffer;
	private int[] frontBuffer;
	private int[] frameBuffer;

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
	private int scanlinesRendering = SCANLINES_RENDERING;

	private int debug = 0;
	
	private int line = 0;
	
	private ScreenWindow window;
	private ScreenFullWindow fullWindow;

	private DisplayCanvas canvas;

	private BufferedImage frameImage;
	
	private BufferedImage scanlines1TextureImage;
	private TVTriadComposite scanlines2Composite;
	private BufferedImage intermFrameImage;
	
	private Image logoIcon;
	
	private int VSYNCDetectionCount = 0;

	private String newDataMonitor = "nextLineMonitor";		// Used only for synchronization
	private String refreshMonitor = "refreshMonitor";		// Used only for synchronization

	private static final String BASE_TITLE = "Atari";

	private static final int VSYNC_DETECTION = 2;
	private static final int VSYNC_TOLERANCE = Parameters.SCREEN_VSYNC_TOLERANCE;
	
	public static final double DEFAULT_FPS = Parameters.SCREEN_DEFAULT_FPS;

	public static final int DEFAULT_ORIGIN_X = Parameters.SCREEN_DEFAULT_ORIGIN_X;
	public static final double DEFAULT_ORIGIN_Y_PCT = Parameters.SCREEN_DEFAULT_ORIGIN_Y_PCT;		// Percentage of height
	public static final int DEFAULT_WIDTH = Parameters.SCREEN_DEFAULT_WIDTH;
	public static final double DEFAULT_HEIGHT_PCT = Parameters.SCREEN_DEFAULT_HEIGHT_PCT;			// Percentage of height
	public static final float DEFAULT_SCALE_X = Parameters.SCREEN_DEFAULT_SCALE_X;
	public static final float DEFAULT_SCALE_Y = Parameters.SCREEN_DEFAULT_SCALE_Y;
	public static final float DEFAULT_SCALE_ASPECT_X = Parameters.SCREEN_DEFAULT_SCALE_ASPECT_X;
	public static final int OSD_FRAMES = Parameters.SCREEN_OSD_FRAMES;

	public static final boolean QUALITY_RENDERING = Parameters.SCREEN_QUALITY_RENDERING;
	public static final int SCANLINES_RENDERING = Parameters.SCREEN_SCANLINES_RENDERING;
	public static final float SCANLINES1_STRENGTH = Parameters.SCREEN_SCANLINES1_STRENGTH;
	public static final int MULTI_BUFFERING = Parameters.SCREEN_MULTI_BUFFERING;
	public static final boolean PAGE_FLIPPING = Parameters.SCREEN_PAGE_FLIPPING;
	public static final boolean VSYNC = Parameters.SCREEN_VSYNC;
	public static final float FRAME_ACCELERATION = Parameters.SCREEN_FRAME_ACCELERATION;
	public static final float IMTERM_FRAME_ACCELERATION = Parameters.SCREEN_INTERM_FRAME_ACCELERATION;
	public static final float SCANLINES1_ACCELERATION = Parameters.SCREEN_SCANLINES1_ACCELERATION; 

	
	public static final int a = 1;
	
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
		VIDEO_STANDARD,
		EXIT, LOAD_CARTRIDGE,
		FULL_SCREEN, QUALITY, SCANLINES, 
		HELP, DEBUG
	}

}


// Simulates the TV display at the sub pixel level (triads) 
class TVTriadComposite implements Composite {
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
				dstOut.setDataElements(dstOut.getMinX(), dstOut.getMinY(), w, 1, data);
			}
		}
	};
	@Override
	public CompositeContext createContext(ColorModel srcColorModel,	ColorModel dstColorModel, RenderingHints hints) {
		return context;
	}
}

