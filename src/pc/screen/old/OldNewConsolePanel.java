// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.screen.old;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import utils.GraphicsDeviceHelper;
import utils.slickframe.HotspotManager;
import utils.slickframe.MousePressAndMotionListener;
import atari.controls.ConsoleControls;
import atari.controls.ConsoleControlsInput;
import atari.controls.ConsoleControlsRedefinitionListener;
import atari.controls.ConsoleControlsSocket;

public class OldNewConsolePanel extends JPanel implements ConsoleControls, ConsoleControlsInput, ConsoleControlsRedefinitionListener {

	public OldNewConsolePanel(OldScreen screen, ConsoleControlsSocket controlsSocket) {
		this(screen, controlsSocket, null);
	}
	
	public OldNewConsolePanel(OldScreen screen, ConsoleControlsSocket controlsSocket, MousePressAndMotionListener forwardListener) {
		super();
		this.screen = screen;
		this.consoleControlsSocket = controlsSocket;
		this.consoleControlsSocket.addForwardedInput(this);
		this.consoleControlsSocket.addRedefinitionListener(this);
		buildGUI();
		addHotspots(forwardListener);
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
		setSize(desiredSize());
		setPreferredSize(desiredSize());
	}

	private void loadImages() {
		try {
			panelImage = GraphicsDeviceHelper.loadAsCompatibleImage("pc/screen/images/Panel.png");
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
			}
			updateVisibleControlsState();
			OldNewConsolePanel.super.setVisible(state);
			if (state) {
				setSize(desiredSize());
			}
		}});
	}

	private void addHotspots(MousePressAndMotionListener forwardListener) {
		hotspots = forwardListener != null ?  new HotspotManager(this, forwardListener) : new HotspotManager(this);
		hotspots.addHotspot(
			new Rectangle(31, 52 - 137, 24, 46),
			new Runnable() { @Override public void run() {
				consoleControlsSocket.controlStateChanged(Control.POWER, true);
				updateVisibleControlsState();
			}});
		hotspots.addHotspot(
			new Rectangle(95, 52 - 137, 24, 46), 
			new Runnable() { @Override public void run() { 
				consoleControlsSocket.controlStateChanged(Control.BLACK_WHITE, true);
				updateVisibleControlsState(); 
			}});
		hotspots.addHotspot(
			new Rectangle(351, 52 - 137, 24, 46),
			new Runnable() { @Override public void run() {
				consoleControlsSocket.controlStateChanged(Control.SELECT, true);
				updateVisibleControlsState();
			}},
			new Runnable() { @Override public void run() {
				consoleControlsSocket.controlStateChanged(Control.SELECT, false);
				updateVisibleControlsState();
			}});
		hotspots.addHotspot(
			new Rectangle(414, 52 - 137, 24, 46),
			new Runnable() { @Override public void run() {
				consoleControlsSocket.controlStateChanged(Control.RESET, true);
				updateVisibleControlsState();
			}},
			new Runnable() { @Override public void run() {
				consoleControlsSocket.controlStateChanged(Control.RESET, false);
				updateVisibleControlsState();
			}});
		hotspots.addHotspot(
			new Rectangle(161, 4 - 137, 34, 21),
			new Runnable() { @Override public void run() {
				consoleControlsSocket.controlStateChanged(Control.DIFFICULTY0, true);
				updateVisibleControlsState();
			}});
		hotspots.addHotspot(
			new Rectangle(274, 4 - 137, 34, 21),
			new Runnable() { @Override public void run() {
				consoleControlsSocket.controlStateChanged(Control.DIFFICULTY1, true);
				updateVisibleControlsState();
			}});
		hotspots.addHotspot(
			new Rectangle(160, 52 - 135, 74, 43),
			new Runnable() { @Override public void run() {
				screen.controlStateChanged(pc.screen.old.OldScreen.Control.LOAD_CARTRIDGE_FILE, true);
				updateVisibleControlsState();
			}});
		hotspots.addHotspot(
			new Rectangle(150 + 85, 52 - 135, 74, 43),
			new Runnable() { @Override public void run() {
				screen.controlStateChanged(pc.screen.old.OldScreen.Control.LOAD_CARTRIDGE_URL, true);
				updateVisibleControlsState();
			}});
	}

	private Dimension desiredSize() {
		Insets ins = getInsets();
		return new Dimension(WIDTH + ins.left + ins.right, HEIGHT + ins.top + ins.bottom);
	}
	
	private void updateVisibleControlsState() {
		consoleControlsSocket.controlsStateReport(controlsStateReport);
		if (isVisible()) repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Insets ins = getInsets();
		int initialHeight = HEIGHT - (getHeight() - ins.top - ins.bottom);
		g.drawImage(
			panelImage, 
			ins.left, ins.top, getWidth() - ins.right, getHeight() - ins.bottom,
			0, initialHeight, getWidth() - ins.left - ins.right, initialHeight + getHeight() - ins.top - ins.bottom,
			null);
		// Controls state
		int panelBottom = getHeight() - ins.bottom;
		if (!controlsStateReport.get(Control.POWER)) g.drawImage(powerDownImage, ins.left + 33, panelBottom - 87, null);
		if (controlsStateReport.get(Control.BLACK_WHITE)) g.drawImage(colorDownImage, ins.left + 97, panelBottom - 87, null);
		if (controlsStateReport.get(Control.SELECT)) g.drawImage(selectDownImage, ins.left + 353, panelBottom - 87, null);
		if (controlsStateReport.get(Control.RESET)) g.drawImage(resetDownImage, ins.left + 416, panelBottom - 87, null);
		if (controlsStateReport.get(Control.DIFFICULTY0)) g.drawImage(p0DiffDownImage, ins.left + 164, panelBottom - 131, null);
		if (controlsStateReport.get(Control.DIFFICULTY1)) g.drawImage(p1DiffDownImage, ins.left + 277, panelBottom - 131, null);
	}


	private HotspotManager hotspots;
	
	private BufferedImage panelImage;
	private BufferedImage powerDownImage;
	private BufferedImage colorDownImage;
	private BufferedImage selectDownImage;
	private BufferedImage resetDownImage;
	private BufferedImage p0DiffDownImage;
	private BufferedImage p1DiffDownImage;
	
	private final OldScreen screen;
	private final ConsoleControlsSocket consoleControlsSocket;
	private Map<ConsoleControls.Control, Boolean> controlsStateReport = new HashMap<ConsoleControls.Control, Boolean>();
	
	public static final int WIDTH = 465;
	public static final int HEIGHT = 137;
	
	private static final Set<Control> visibleControls = new HashSet<Control>(
		Arrays.asList(new Control[] { Control.POWER, Control.BLACK_WHITE, Control.SELECT, Control.RESET, Control.DIFFICULTY0, Control.DIFFICULTY1 }));
	
	public static final long serialVersionUID = 1L;

	
}

