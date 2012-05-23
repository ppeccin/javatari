// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.screen;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import utils.GraphicsDeviceHelper;
import utils.SlickFrame;
import atari.controls.ConsoleControls;
import atari.controls.ConsoleControlsInput;
import atari.controls.ConsoleControlsRedefinitionListener;
import atari.controls.ConsoleControlsSocket;

public class ConsolePanel extends SlickFrame implements ConsoleControls, ConsoleControlsInput, ConsoleControlsRedefinitionListener {

	public ConsolePanel(JFrame masterWindow, Screen screen, ConsoleControlsSocket controlsSocket) {
		super(false);
		docked = true;
		this.masterWindow = masterWindow;
		this.screen = screen;
		this.consoleControlsSocket = controlsSocket;
		this.consoleControlsSocket.addForwardedInput(this);
		this.consoleControlsSocket.addRedefinitionListener(this);
		buildGUI();
		addHotspots();
	}

	@Override
	public void controlStateChanged(Control control, boolean state) {
		if (visibleControls.contains(control)) updateVisibleControlsState();
	}

	@Override
	public void controlStateChanged(ConsoleControls.Control control, int position) {
		if (visibleControls.contains(control)) updateVisibleControlsState();
	}

	@Override
	public void controlsStateReport(Map<Control, Boolean> report) {
		// No controls kept here
	}

	@Override
	public void controlsStatesRedefined() {
		updateVisibleControlsState();
	}

	private void buildGUI() {
		loadImages();
		setTitle("Atari Controls");
		setResizable(false);
		setSize(desiredSize());
		Toolkit tk = Toolkit.getDefaultToolkit();
		int x = (tk.getScreenSize().width - getWidth()) / 2;
		int y = (tk.getScreenSize().height - getHeight()) / 3;
		setLocation(x, y);
		masterWindow.addComponentListener(new ComponentAdapter() {
			public void componentMoved(ComponentEvent e) {
				trackMasterWindow();
			}});
		masterWindow.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				trackMasterWindow();
			}});
		masterWindow.addFocusListener(new FocusAdapter() {  
			public void focusGained(FocusEvent e) {
				setVisible(true);
			}});
		masterWindow.addWindowStateListener(new WindowStateListener() {
			public void windowStateChanged(WindowEvent e) {
				if (masterWindow.getState() == ICONIFIED && isVisible())
					setVisible(false);
				if (masterWindow.getState() == NORMAL && !isVisible())
					setVisible(true);
		}});
		paintBackBuffer = GraphicsDeviceHelper.defaultScreenDeviceConfiguration().createCompatibleImage(WIDTH, EXPANDED_HEIGHT);
	}

	private void loadImages() {
		try {
			panelImage = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/Panel.png");
			retractButtonImage = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/PanelRetractButton.png");
			expandButtonImage = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/PanelExpandButton.png");
			closeButtonImage = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/PanelCloseButton.png");
			powerDownImage = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/PowerDown.png");
			colorDownImage = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/ColorDown.png");
			selectDownImage = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/SelectDown.png");
			resetDownImage = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/ResetDown.png");
			p0DiffDownImage = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/P0DiffDown.png");
			p1DiffDownImage = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/P1DiffDown.png");
		} catch (IOException ex) {
			System.out.println("Console Panel: unable to load images\n" + ex);
		}
	}

	@Override
	public void setVisible(final boolean state) {
		SwingUtilities.invokeLater(new Runnable() {  @Override public void run() {
			if (!isVisible()) {
				setFocusable(false);
				setFocusableWindowState(false);
			}
			updateVisibleControlsState();
			ConsolePanel.super.setVisible(state);
			if (state) {
				setState(Frame.NORMAL);
				setSize(desiredSize());
				toFront(); toFront();
				masterWindow.toFront();
				masterWindow.requestFocus();
			}
		}});
	}

	public void toggle() {
		if (!isVisible())
			setVisible(true);
		toggleRetract();
	}

	private void addHotspots() {
		addHotspot(				
			new Rectangle(218, -13, 30, 15), 
			new Runnable() { @Override public void run() {
				toggleRetract();
			}});
		addHotspot(
			new Rectangle(446, 4 - 137, 14, 13), 
			new Runnable() { @Override public void run() { 
				if (!docked) {
					setVisible(false);
					SwingUtilities.invokeLater(new Runnable() {  @Override public void run() {
						toggleRetract();
					}});
				}
			}});
		addHotspot(
			new Rectangle(31, 52 - 137, 24, 46), 
			new Runnable() { @Override public void run() { 
				consoleControlsSocket.controlStateChanged(Control.POWER, true);
				updateVisibleControlsState(); 
			}});
		addHotspot(
			new Rectangle(95, 52 - 137, 24, 46), 
			new Runnable() { @Override public void run() { 
				consoleControlsSocket.controlStateChanged(Control.BLACK_WHITE, true);
				updateVisibleControlsState(); 
			}});
		addHotspot(
			new Rectangle(351, 52 - 137, 24, 46), 
			new Runnable() { @Override public void run() { 
				consoleControlsSocket.controlStateChanged(Control.SELECT, true);
				updateVisibleControlsState(); 
			}},
			new Runnable() { @Override public void run() { 
				consoleControlsSocket.controlStateChanged(Control.SELECT, false);
				updateVisibleControlsState(); 
			}});
		addHotspot(
			new Rectangle(414, 52 - 137, 24, 46), 
			new Runnable() { @Override public void run() { 
				consoleControlsSocket.controlStateChanged(Control.RESET, true);
				updateVisibleControlsState(); 
			}},
			new Runnable() { @Override public void run() { 
				consoleControlsSocket.controlStateChanged(Control.RESET, false);
				updateVisibleControlsState(); 
			}});
		addHotspot(
			new Rectangle(161, 4 - 137, 34, 21), 
			new Runnable() { @Override public void run() { 
				consoleControlsSocket.controlStateChanged(Control.DIFFICULTY0, true);
				updateVisibleControlsState(); 
			}});
		addHotspot(
			new Rectangle(274, 4 - 137, 34, 21), 
			new Runnable() { @Override public void run() { 
				consoleControlsSocket.controlStateChanged(Control.DIFFICULTY1, true);
				updateVisibleControlsState(); 
			}});
		addHotspot(
			new Rectangle(150, 52 - 137, 170, 45), 
			new Runnable() { @Override public void run() { 
				screen.controlStateChanged(pc.screen.Screen.Control.LOAD_CARTRIDGE_FILE, true);
				updateVisibleControlsState(); 
			}});
	}

	protected void movingTo(int x, int y) {
		if (retracted) return;
		docked = false;
		if (locationDockable(new Point(x, y)))
			goToDockedLocation();
		else
			setLocation(x, y);
	}

	protected void finishedMoving() {
		if (locationDockable(getLocation()))
			dock();
		else
			undock();
	}

	private void toggleRetract() {
		// Does not allow this operation if the last one is still happening
		if (sizeAdjustThread != null && sizeAdjustThread.isAlive()) return;
		retracted = !retracted;
		adjustSize();
		if (retracted) dock();
	}

	private boolean locationDockable(Point p) {
		if (dockedLocation == null) return false;
		if (Math.abs(p.y - dockedLocation.y) > 16) return false;
		return Math.abs(p.x - dockedLocation.x) < Math.max(Math.abs(masterWindow.getWidth() - getWidth()) / 2, 40);
	}
	
	private void trackMasterWindow() {
		dockedLocation = new Point(
			masterWindow.getLocation().x + (masterWindow.getWidth() - getWidth()) / 2, 
			masterWindow.getLocation().y + masterWindow.getHeight());
		if (docked)
			goToDockedLocation();
		else 
			if (locationDockable(getLocation()))
				dock();
		adjustSize();
	}
	
	private void dock() {
		goToDockedLocation();
		docked = true;
		if (isVisible()) repaint();
	}

	private void undock() {
		docked = false;
		if (isVisible()) repaint();
	}

	private void goToDockedLocation() {
		setLocation(dockedLocation);
	}

	private void adjustSize() {
		// Considers this window actual insets, they may not be zero if its still decorated for some reason
		final Dimension targetSize = desiredSize();
		if (getSize().equals(targetSize)) return;
		final int dir = targetSize.height > getHeight() ? -1 : 1;
		final int[] delta = { Math.abs(targetSize.height - getHeight()) }; 
		sizeAdjustThread = new Thread() { @Override public void run() {
			for (; delta[0] >= 0; delta[0] -= 10) {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {  @Override public void run() {
						setSize(targetSize.width, targetSize.height + delta[0] * dir);
					}});
					Thread.sleep(7);
				} catch (InterruptedException e1) {} catch (InvocationTargetException e1) {}
			}
			SwingUtilities.invokeLater(new Runnable() {  @Override public void run() {
				setSize(targetSize);
			}});
		}};
		sizeAdjustThread.start();
	}

	private Dimension desiredSize() {
		Insets ins = getInsets();
		int h = retracted ? ( masterWindow.getWidth() < WIDTH ? 0 : RETRACTED_HEIGHT ) : EXPANDED_HEIGHT;
		return new Dimension(WIDTH + ins.left + ins.right, h + ins.top + ins.bottom);
	}
	
	private void updateVisibleControlsState() {
		consoleControlsSocket.controlsStateReport(controlsStateReport);
		if (isVisible() && !retracted) repaint();
	}
	
	@Override
	public void paint(Graphics origGraphics) {
		Insets ins = getInsets();
		int initialHeight = EXPANDED_HEIGHT - (getHeight() - ins.top - ins.bottom);
		boolean retr = retracted;
		boolean dock = docked;
		Graphics2D g = paintBackBuffer.createGraphics();
		g.drawImage(
			panelImage, 
			0, initialHeight, WIDTH, EXPANDED_HEIGHT,
			0, initialHeight, WIDTH, EXPANDED_HEIGHT,
			null);
		if (retr)
			g.drawImage(expandButtonImage, 225, EXPANDED_HEIGHT - 10, null);
		else
			g.drawImage(retractButtonImage, 225, EXPANDED_HEIGHT - 10, null);
		if (!dock) g.drawImage(closeButtonImage, 448, EXPANDED_HEIGHT - 132, null);
		// Controls state
		if (!controlsStateReport.get(Control.POWER)) g.drawImage(powerDownImage, 33, EXPANDED_HEIGHT - 87, null);
		if (controlsStateReport.get(Control.BLACK_WHITE)) g.drawImage(colorDownImage, 97, EXPANDED_HEIGHT - 87, null);
		if (controlsStateReport.get(Control.SELECT)) g.drawImage(selectDownImage, 353, EXPANDED_HEIGHT - 87, null);
		if (controlsStateReport.get(Control.RESET)) g.drawImage(resetDownImage, 416, EXPANDED_HEIGHT - 87, null);
		if (controlsStateReport.get(Control.DIFFICULTY0)) g.drawImage(p0DiffDownImage, 164, EXPANDED_HEIGHT - 131, null);
		if (controlsStateReport.get(Control.DIFFICULTY1)) g.drawImage(p1DiffDownImage, 277, EXPANDED_HEIGHT - 131, null);
		g.dispose();
		origGraphics.drawImage(
			paintBackBuffer, 
			ins.left, ins.top, getWidth() - ins.right, getHeight() - ins.bottom,
			0, initialHeight, getWidth() - ins.left - ins.right, initialHeight + getHeight() - ins.top - ins.bottom,
			null);
	}


	private final JFrame masterWindow;
	private boolean retracted = false;
	private boolean docked = false;
	private Point dockedLocation;
	private Thread sizeAdjustThread;
	
	private BufferedImage panelImage;
	private BufferedImage retractButtonImage;
	private BufferedImage expandButtonImage;
	private BufferedImage closeButtonImage;
	private BufferedImage powerDownImage;
	private BufferedImage colorDownImage;
	private BufferedImage selectDownImage;
	private BufferedImage resetDownImage;
	private BufferedImage p0DiffDownImage;
	private BufferedImage p1DiffDownImage;
	private BufferedImage paintBackBuffer;
	
	private final Screen screen;
	private final ConsoleControlsSocket consoleControlsSocket;
	private Map<ConsoleControls.Control, Boolean> controlsStateReport = new HashMap<ConsoleControls.Control, Boolean>();
	
	public static final int WIDTH = 465;
	public static final int EXPANDED_HEIGHT = 137;
	public static final int RETRACTED_HEIGHT = 10;
	
	private static final Set<Control> visibleControls = new HashSet<Control>(
		Arrays.asList(new Control[] { Control.POWER, Control.BLACK_WHITE, Control.SELECT, Control.RESET, Control.DIFFICULTY0, Control.DIFFICULTY1 }));
	
	public static final long serialVersionUID = 1L;

	
}

