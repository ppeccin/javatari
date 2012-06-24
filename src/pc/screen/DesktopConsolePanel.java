// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.screen;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import utils.GraphicsDeviceHelper;
import utils.slickframe.HotspotManager;
import utils.slickframe.MousePressAndMotionListener;
import utils.slickframe.SlickFrame;
import atari.controls.ConsoleControlsSocket;

public class DesktopConsolePanel extends SlickFrame {

	public DesktopConsolePanel(DesktopScreenWindow masterWindow, Screen screen) {
		super(false);
		docked = true;
		this.masterWindow = masterWindow;
		addHotspots(detachMouseListener());
		consolePanel = new ConsolePanel(screen, hotspots.detachMouseListener());
		buildGUI();
	}

	public void connect(ConsoleControlsSocket controlsSocket) {
		consolePanel.connect(controlsSocket);
	}

	@Override
	public void setVisible(final boolean state) {
		SwingUtilities.invokeLater(new Runnable() {  @Override public void run() {
			if (!isVisible()) {
				setFocusable(false);
				setFocusableWindowState(false);
			}
			DesktopConsolePanel.super.setVisible(state);
			if (state) {
				userClosed = false;
				setState(Frame.NORMAL);
				setSize(desiredSize());
				toFront();
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

	private void buildGUI() {
		loadImages();
		setLayout(new BorderLayout());
		setTitle(DesktopScreenWindow.BASE_TITLE + " - Console Panel");
		setIconImages(Arrays.asList(new Image[] { masterWindow.icon64, masterWindow.icon32, masterWindow.favicon }));
		addGlassPane();
		setSize(desiredSize());
		setResizable(false);
		add(consolePanel, BorderLayout.CENTER);
		Toolkit tk = Toolkit.getDefaultToolkit();
		int x = (tk.getScreenSize().width - getWidth()) / 2;
		int y = (tk.getScreenSize().height - getHeight()) / 2;
		setLocation(x, y);
		addMasterWindowListeners();
	}

	private void addGlassPane() {
		TopControls gp = new TopControls();
		setGlassPane(gp);
		gp.setOpaque(false);
		gp.setVisible(true);
	}

	private void addMasterWindowListeners() {
		masterWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowIconified(WindowEvent e) {
				if (isVisible()) setVisible(false);
			}
			@Override
			public void windowActivated(WindowEvent e) {
				if (!userClosed) setVisible(true); 	// Will ring to front
			}
		});
		masterWindow.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				trackMasterWindow();
			}
			@Override
			public void componentResized(ComponentEvent e) {
				trackMasterWindow();
			}
		});
	}

	private void loadImages() {
		try {
			retractButtonImage = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/PanelRetractButton.png");
			expandButtonImage = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/PanelExpandButton.png");
			closeButtonImage = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/PanelCloseButton.png");
		} catch (IOException ex) {
			System.out.println("Console Panel: unable to load images\n" + ex);
		}
	}

	private void addHotspots(MousePressAndMotionListener forwardListener) {
		hotspots = new HotspotManager(this, forwardListener);
		hotspots.addHotspot(
			new Rectangle(218, -13, 30, 15),
			new Runnable() { @Override public void run() {
				toggleRetract();
			}});
		hotspots.addHotspot(
			new Rectangle(446, 4 - 133, 14, 13),
			new Runnable() { @Override public void run() {
				if (!docked) {
					setVisible(false);
					userClosed = true;
					SwingUtilities.invokeLater(new Runnable() {  @Override public void run() {
						toggleRetract();
					}});
				}
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
			for (; delta[0] >= 0; delta[0] -= 8) {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {  @Override public void run() {
						setSize(targetSize.width, targetSize.height + delta[0] * dir);
					}});
					Thread.sleep(8);
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
	

	private final DesktopScreenWindow masterWindow;
	private final ConsolePanel consolePanel;
	
	private boolean retracted = false;
	private boolean docked = false;
	private boolean userClosed = true;
	private Point dockedLocation;
	private Thread sizeAdjustThread;
	private HotspotManager hotspots;
	
	private BufferedImage retractButtonImage;
	private BufferedImage expandButtonImage;
	private BufferedImage closeButtonImage;
	
	public static final int WIDTH = 465;
	public static final int EXPANDED_HEIGHT = 137;
	public static final int RETRACTED_HEIGHT = 10;
	
	public static final long serialVersionUID = 1L;

	
	class TopControls extends JPanel {
		@Override
		public void paintComponent(Graphics g) {
			Insets ins = getInsets();
			int initialHeight = /* EXPANDED_HEIGHT - */ (getHeight() /* - ins.top */ - ins.bottom);
			boolean retr = retracted;
			boolean dock = docked;
			if (retr)
				g.drawImage(expandButtonImage, 225, initialHeight - 10, null);
			else
				g.drawImage(retractButtonImage, 225, initialHeight - 10, null);
			if (!dock) g.drawImage(closeButtonImage, 448, initialHeight - 132, null);
		}
		private static final long serialVersionUID = 1L;
	}
	
}

