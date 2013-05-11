// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.screen;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeInfo;
import org.javatari.atari.cartridge.CartridgeInsertionListener;
import org.javatari.atari.cartridge.CartridgeSocket;
import org.javatari.atari.controls.ConsoleControls;
import org.javatari.atari.controls.ConsoleControlsInput;
import org.javatari.atari.controls.ConsoleControlsRedefinitionListener;
import org.javatari.atari.controls.ConsoleControlsSocket;
import org.javatari.parameters.Parameters;
import org.javatari.utils.Environment;
import org.javatari.utils.SwingHelper;
import org.javatari.utils.slickframe.HotspotPanel;
import org.javatari.utils.slickframe.MousePressAndMotionListener;

public final class ConsolePanel extends HotspotPanel implements ConsoleControls, ConsoleControlsInput, ConsoleControlsRedefinitionListener, CartridgeInsertionListener {

	public ConsolePanel(Monitor screen, MousePressAndMotionListener forwardListener) {
		super();
		this.screen = screen;
		setup();
		setForwardListener(forwardListener);
		addHotspots();
		cartridgeInserted(null);
	}

	public void connect(ConsoleControlsSocket controlsSocket, CartridgeSocket cartridgeSocket) {
		consoleControlsSocket = controlsSocket;
		consoleControlsSocket.addForwardedInput(this);
		consoleControlsSocket.addRedefinitionListener(this);	// Will fire a redefinition event
		cartridgeSocket.addInsertionListener(this);				// Will fire a insertion event
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
	
	@Override
	public void cartridgeInserted(Cartridge cartridge) {
		cartridgeInserted = cartridge != null;
		String label = null;
		int fore = -1, back = -1, bord = -1;
		Color b;
		if (cartridgeInserted) {
			CartridgeInfo info = cartridge.getInfo();
			label = info.label;
			fore = info.labelColor;
			back = info.labelBackColor;
			bord = info.labelBorderColor;
		}
		if (label == null) label = DEFAULT_CARTRIDGE_LABEL;
		if (fore == -1) fore = DEFAULT_CARTRIDGE_LABEL_COLOR;
		if (back == -1) back = DEFAULT_CARTRIDGE_BACK_COLOR;
		cartridgeLabelComponent.setText(label);
		cartridgeLabelComponent.setForeground(new Color(fore));
		cartridgeLabelComponent.setBackground(b = new Color(back));
		float bf = .9f;
		cartridgeLabelComponent.setBorder(bord == -1 
				? new LineBorder(new Color((int)(b.getRed() * bf), (int)(b.getGreen() * bf), (int)(b.getBlue() * bf)), 1) 
				: new LineBorder(new Color(bord), 1));

		removeHotspot(cartridgeInsertedHotspot);
		removeHotspot(cartridgeMissingHotspot);
		addHotspot(cartridgeInserted ? cartridgeInsertedHotspot : cartridgeMissingHotspot);
		removeHotspot(fileHotspot);
		removeHotspot(urlHotspot);
		if (screen.isCartridgeChangeEnabled()) {
			addHotspot(fileHotspot);
			addHotspot(urlHotspot);
		}
		if (isVisible()) repaint();
	}

	private void setup() {
		prepareResources();
		Dimension size = desiredSize();
		setSize(size);
		setPreferredSize(size);
		setMaximumSize(size);
	}

	private void prepareResources() {
		try {
			// Images
			panelImage = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/Panel.png");
			cartridgeImage = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/Cartridge.png");
			changeCartKeysImage = SwingHelper.loadAsCompatibleTranslucentImage("org/javatari/pc/screen/images/CartridgeChangeKeys.png");
			powerDownImage = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/PowerDown.png");
			colorDownImage = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/ColorDown.png");
			selectDownImage = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/SelectDown.png");
			resetDownImage = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/ResetDown.png");
			p0DiffDownImage = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/P0DiffDown.png");
			p1DiffDownImage = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/P1DiffDown.png");
			// Component to render Cartridge Labels
			cartridgeLabelComponent = new JLabel();
			if (Environment.cartridgeLabelFont != null) cartridgeLabelComponent.setFont(Environment.cartridgeLabelFont);
			else if (Environment.LIBERATION_FONT) cartridgeLabelComponent.setFont(new Font("Liberation Sans", Font.BOLD, 15));
			else if (Environment.ARIAL_FONT) cartridgeLabelComponent.setFont(new Font("Arial", Font.BOLD, 14));
			else cartridgeLabelComponent.setFont(new Font("SansSerif", Font.BOLD, 14));
			cartridgeLabelComponent.setOpaque(true);
			cartridgeLabelComponent.setHorizontalAlignment(SwingConstants.CENTER);
			cartridgeLabelComponent.setVerticalAlignment(SwingConstants.CENTER);
		} catch (IOException ex) {
			System.out.println("Console Panel: unable to load images\n" + ex);
		}
	}

	private void addHotspots() {
		addHotspot(
			new Rectangle(31, 52 - 137, 24, 46),
			new Runnable() { @Override public void run() {
				if (consoleControlsSocket != null) consoleControlsSocket.controlStateChanged(Control.POWER, true);
			}});
		addHotspot(
			new Rectangle(95, 52 - 137, 24, 46), 
			new Runnable() { @Override public void run() { 
				if (consoleControlsSocket != null) consoleControlsSocket.controlStateChanged(Control.BLACK_WHITE, true);
			}});
		addHotspot(
			new Rectangle(351, 52 - 137, 24, 46),
			new Runnable() { @Override public void run() {
				if (consoleControlsSocket != null) consoleControlsSocket.controlStateChanged(Control.SELECT, true);
			}},
			new Runnable() { @Override public void run() {
				if (consoleControlsSocket != null) consoleControlsSocket.controlStateChanged(Control.SELECT, false);
			}});
		addHotspot(
			new Rectangle(414, 52 - 137, 24, 46),
			new Runnable() { @Override public void run() {
				if (consoleControlsSocket != null) consoleControlsSocket.controlStateChanged(Control.RESET, true);
			}},
			new Runnable() { @Override public void run() {
				if (consoleControlsSocket != null) consoleControlsSocket.controlStateChanged(Control.RESET, false);
			}});
		addHotspot(
			new Rectangle(161, 4 - 137, 34, 21),
			new Runnable() { @Override public void run() {
				if (consoleControlsSocket != null) consoleControlsSocket.controlStateChanged(Control.DIFFICULTY0, true);
			}});
		addHotspot(
			new Rectangle(274, 4 - 137, 34, 21),
			new Runnable() { @Override public void run() {
				if (consoleControlsSocket != null) consoleControlsSocket.controlStateChanged(Control.DIFFICULTY1, true);
			}});
		cartridgeInsertedHotspot = new HotspotAction(
				new Rectangle(145, 52 - 144, 180, 46),
				new Runnable() { @Override public void run() {
					screen.controlActivated(org.javatari.pc.screen.Monitor.Control.LOAD_CARTRIDGE_FILE);
			}});
		cartridgeMissingHotspot = new HotspotAction(
				new Rectangle(153, 52 - 135, 164, 42),
				new Runnable() { @Override public void run() {
					screen.controlActivated(org.javatari.pc.screen.Monitor.Control.LOAD_CARTRIDGE_FILE);
			}});
		fileHotspot = new HotspotAction(
			new Rectangle(171, 52 - 86, 30, 29),
			new Runnable() { @Override public void run() {
				if (getHeight() >= HEIGHT)
					screen.controlActivated(org.javatari.pc.screen.Monitor.Control.LOAD_CARTRIDGE_FILE);
			}});
		urlHotspot = new HotspotAction(
			new Rectangle(267, 52 - 86, 30, 29),
			new Runnable() { @Override public void run() {
				if (getHeight() >= HEIGHT)
					screen.controlActivated(org.javatari.pc.screen.Monitor.Control.LOAD_CARTRIDGE_URL);
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
		int panelBottom = getHeight() - ins.bottom;
		// Inserted Cartridge
		if (cartridgeInserted) {
			g.drawImage(cartridgeImage, ins.left + 141, panelBottom - 94, null);
			// Cartridge Label
			((Graphics2D) g).setComposite(AlphaComposite.SrcOver);
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			SwingUtilities.paintComponent(
				g, cartridgeLabelComponent, this, 
				ins.left + 158, panelBottom - 85, 	
				154, 27
			);
		}
		// Cartridge change keys
		if (screen.isCartridgeChangeEnabled() && initialHeight < HEIGHT - 10) g.drawImage(changeCartKeysImage, ins.left + 173, panelBottom - 32, null);

		paintHotspots(g);
		
		// Controls state
		if (controlsStateReport.isEmpty()) return;
		if (!controlsStateReport.get(Control.POWER)) g.drawImage(powerDownImage, ins.left + 33, panelBottom - 87, null);
		if (controlsStateReport.get(Control.BLACK_WHITE)) g.drawImage(colorDownImage, ins.left + 97, panelBottom - 87, null);
		if (controlsStateReport.get(Control.SELECT)) g.drawImage(selectDownImage, ins.left + 353, panelBottom - 87, null);
		if (controlsStateReport.get(Control.RESET)) g.drawImage(resetDownImage, ins.left + 416, panelBottom - 87, null);
		if (controlsStateReport.get(Control.DIFFICULTY0)) g.drawImage(p0DiffDownImage, ins.left + 164, panelBottom - 131, null);
		if (controlsStateReport.get(Control.DIFFICULTY1)) g.drawImage(p1DiffDownImage, ins.left + 277, panelBottom - 131, null);
	}


	private BufferedImage panelImage;
	private BufferedImage powerDownImage;
	private BufferedImage colorDownImage;
	private BufferedImage selectDownImage;
	private BufferedImage resetDownImage;
	private BufferedImage p0DiffDownImage;
	private BufferedImage p1DiffDownImage;
	private BufferedImage changeCartKeysImage;
	private BufferedImage cartridgeImage;
	private JLabel cartridgeLabelComponent;
	private boolean cartridgeInserted = false;
	private HotspotAction cartridgeInsertedHotspot, cartridgeMissingHotspot, fileHotspot, urlHotspot;

	private final Monitor screen;
	private ConsoleControlsSocket consoleControlsSocket;
	private final Map<ConsoleControls.Control, Boolean> controlsStateReport = new HashMap<ConsoleControls.Control, Boolean>();
	

	public static final int WIDTH = 465;
	public static final int HEIGHT = 137;
	
	private static final Set<Control> visibleControls = new HashSet<Control>(
		Arrays.asList(new Control[] { Control.POWER, Control.BLACK_WHITE, Control.SELECT, Control.RESET, Control.DIFFICULTY0, Control.DIFFICULTY1 }));
	
	private static final String DEFAULT_CARTRIDGE_LABEL = Parameters.DEFAULT_CARTRIDGE_LABEL;
	private static final int DEFAULT_CARTRIDGE_LABEL_COLOR = Parameters.DEFAULT_CARTRIDGE_LABEL_COLOR;
	private static final int DEFAULT_CARTRIDGE_BACK_COLOR = Parameters.DEFAULT_CARTRIDGE_BACK_COLOR;
	
	public static final long serialVersionUID = 1L;

}

