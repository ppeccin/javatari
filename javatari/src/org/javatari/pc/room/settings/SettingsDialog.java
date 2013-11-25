// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.room.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeDatabase;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.console.Console;
import org.javatari.atari.controls.ConsoleControls.Control;
import org.javatari.atari.network.ConnectionStatusListener;
import org.javatari.atari.network.RemoteReceiver;
import org.javatari.atari.network.RemoteTransmitter;
import org.javatari.parameters.Parameters;
import org.javatari.pc.controls.AWTConsoleControls;
import org.javatari.pc.controls.JoystickConsoleControls.DeviceOption;
import org.javatari.pc.controls.JoystickConsoleControls.JoystickButtonDetectionListener;
import org.javatari.pc.room.Room;
import org.javatari.utils.Environment;
import org.javatari.utils.SwingHelper;

public final class SettingsDialog extends JDialog implements ConnectionStatusListener, JoystickButtonDetectionListener {

	public SettingsDialog(Room room) {
		this.room = room;
		loadImages();
		buildGUI();
		pack();
		setEscActionListener();
		buildControlsFieldsLists();
		setControlsFieldsKeyListeners();
		setMultiplayerDefaults();
		vmInfo.setText(Environment.vmInfo());
		mainTabbedPane.setSelectedIndex(5);
		mainTabbedPane.setEnabledAt(0, Parameters.MULTIPLAYER_UI);
		GlassPane gp = new GlassPane();
		gp.setOpaque(false);
		setGlassPane(gp);
	}

	public void open(Component parent) {
		setLocationRelativeTo(parent);
		setupConnectionStatusListeners();
		mainTabbedPaneChanged();
		setVisible(true);
	}
	
	@Override
	public void connectionStatusChanged() {
		if (isVisible()) refreshMultiplayerImages();
	}
	
	private void refreshKeyboard() {
		if (refreshInProgress) return;
		refreshInProgress = true;
		keyP0Up.setText(KeyNames.get(Parameters.KEY_P0_UP));
		keyP0Down.setText(KeyNames.get(Parameters.KEY_P0_DOWN));
		keyP0Left.setText(KeyNames.get(Parameters.KEY_P0_LEFT));
		keyP0Right.setText(KeyNames.get(Parameters.KEY_P0_RIGHT));
		keyP0Button.setText(KeyNames.get(Parameters.KEY_P0_BUTTON));
		keyP0Button2.setText(KeyNames.get(Parameters.KEY_P0_BUTTON2));
		keyP1Up.setText(KeyNames.get(Parameters.KEY_P1_UP));
		keyP1Down.setText(KeyNames.get(Parameters.KEY_P1_DOWN));
		keyP1Left.setText(KeyNames.get(Parameters.KEY_P1_LEFT));
		keyP1Right.setText(KeyNames.get(Parameters.KEY_P1_RIGHT));
		keyP1Button.setText(KeyNames.get(Parameters.KEY_P1_BUTTON));
		keyP1Button2.setText(KeyNames.get(Parameters.KEY_P1_BUTTON2));	
		for (JTextField field : keyboardFieldsList)
			if (field == keyboardEditField) {
				field.setBackground(EDDITING); field.setSelectionStart(0); field.setSelectionEnd(0);
			} else field.setBackground(field.getText().trim().isEmpty() ? UNMAPPED : Color.WHITE);
		keyboardEdittingL.setText(keyboardEditField != null ? "- press key -" : "");
		defaultsB.setText("Defaults");
		defaultsB.setVisible(true);
		if (room.awtControls().paddleMode) {
			keyP0UpL.setText("+ Speed "); keyP0DownL.setText("- Speed "); keyP1UpL.setText("+ Speed "); keyP1DownL.setText("- Speed ");
			keyboard0Icon.setIcon(paddleIcon); keyboard1Icon.setIcon(paddleIcon);
		} else {
			keyP0UpL.setText("Up"); keyP0DownL.setText("Down"); keyP1UpL.setText("Up"); keyP1DownL.setText("Down");
			keyboard0Icon.setIcon(joystickIcon); keyboard1Icon.setIcon(joystickIcon);
		}
		String message;
		if (room.awtControls().p1ControlsMode) {
			keyboardPlayer1Lb.setText("Player 2"); keyboardPlayer2Lb.setText("Player 1");
			message = "Swapped";
		} else {
			keyboardPlayer1Lb.setText("Player 1"); keyboardPlayer2Lb.setText("Player 2");
			message = "";
		}
		keyboardMessageL.setText(message);
		refreshInProgress = false;
	}

	private void refreshJoysticks() {
		if (refreshInProgress) return;
		refreshInProgress = true;
		String pre = "But ";
		String empty = "     ";
		Vector<DeviceOption> joyP0Opts = room.joystickControls().getJoystick0DeviceOptions();
		if (!joyP0Opts.isEmpty()) {
			joyP0Device.setModel(new DefaultComboBoxModel(joyP0Opts));
			joyP0Device.setSelectedItem(room.joystickControls().getJoystick0DeviceOption());
			joyP0Device.setEnabled(true);
		} else {
			joyP0Device.setModel(new DefaultComboBoxModel(new Vector<String>(Arrays.asList(new String[] {"Not Supported"}))));
			joyP0Device.setSelectedIndex(0);
			joyP0Device.setEnabled(false);
		}
		Vector<DeviceOption> joyP1Opts = room.joystickControls().getJoystick1DeviceOptions();
		if (!joyP1Opts.isEmpty()) {
			joyP1Device.setModel(new DefaultComboBoxModel(room.joystickControls().getJoystick1DeviceOptions()));
			joyP1Device.setSelectedItem(room.joystickControls().getJoystick1DeviceOption());
			joyP1Device.setEnabled(true);
		} else {
			joyP1Device.setModel(new DefaultComboBoxModel(new Vector<String>(Arrays.asList(new String[] {"Not Supported"}))));
			joyP1Device.setSelectedIndex(0);
			joyP1Device.setEnabled(false);
		}
		boolean joy0Present = room.joystickControls().isStarted() && room.joystickControls().getJoystick0() != null;
		boolean joy1Present = room.joystickControls().isStarted() && room.joystickControls().getJoystick1() != null;
		joyP0Button.setText(Parameters.JOY_P0_BUTTON == CONTROL_UNSET ? empty : pre + (Parameters.JOY_P0_BUTTON + 1));
		joyP0Button2.setText(Parameters.JOY_P0_BUTTON2 == CONTROL_UNSET ? empty : pre + (Parameters.JOY_P0_BUTTON2 + 1));
		joyP0Reset.setText(Parameters.JOY_P0_RESET == CONTROL_UNSET ? empty : pre + (Parameters.JOY_P0_RESET + 1));
		joyP0Select.setText(Parameters.JOY_P0_SELECT == CONTROL_UNSET ? empty : pre + (Parameters.JOY_P0_SELECT + 1));
		joyP0Pause.setText(Parameters.JOY_P0_PAUSE == CONTROL_UNSET ? empty : pre + (Parameters.JOY_P0_PAUSE + 1));
		joyP0FastSpeed.setText(Parameters.JOY_P0_FAST_SPPED == CONTROL_UNSET ? empty : pre + (Parameters.JOY_P0_FAST_SPPED + 1));
		joyP1Button.setText(Parameters.JOY_P1_BUTTON == CONTROL_UNSET ? empty : pre + (Parameters.JOY_P1_BUTTON + 1));
		joyP1Button2.setText(Parameters.JOY_P1_BUTTON2 == CONTROL_UNSET ? empty : pre + (Parameters.JOY_P1_BUTTON2 + 1));
		joyP1Reset.setText(Parameters.JOY_P1_RESET == CONTROL_UNSET ? empty : pre + (Parameters.JOY_P1_RESET + 1));
		joyP1Select.setText(Parameters.JOY_P1_SELECT == CONTROL_UNSET ? empty : pre + (Parameters.JOY_P1_SELECT + 1));
		joyP1Pause.setText(Parameters.JOY_P1_PAUSE == CONTROL_UNSET ? empty : pre + (Parameters.JOY_P1_PAUSE + 1));
		joyP1FastSpeed.setText(Parameters.JOY_P1_FAST_SPPED == CONTROL_UNSET ? empty : pre + (Parameters.JOY_P1_FAST_SPPED + 1));
		joyP0Deadzone.setEnabled(joy0Present);
		joyP1Deadzone.setEnabled(joy1Present);
		joyP0XAxis.setEnabled(joy0Present);
		joyP1XAxis.setEnabled(joy1Present);
		if (room.joystickControls().paddleMode) {
			joyP0DeadzoneL.setText("Center");
			joyP0Deadzone.setModel(new DefaultComboBoxModel(PADDLE_CENTER_OPTIONS));
			joyP0Deadzone.setSelectedItem(Parameters.JOY_P0_PADDLE_CENTER == 0 ? PADDLE_CENTER_ZERO : String.format("%+d", Parameters.JOY_P0_PADDLE_CENTER));
			joyP0Sensitivity.setEnabled(joy0Present);
			joyP0Sensitivity.setSelectedItem(Parameters.JOY_P0_PADDLE_SENS == 0 ? PADDLE_SENS_DIGITAL : "" + Parameters.JOY_P0_PADDLE_SENS + "%");
			joyP0XAxisL.setText("Axis");
			joyP0XAxis.setSelectedIndex(Parameters.JOY_P0_PAD_AXIS + (Parameters.JOY_P0_PAD_AXIS_SIGNAL == -1 ? 6 : 0));
			joyP0YAxis.setEnabled(false);
			joyP0YAxis.setSelectedIndex(1);
			joyP1DeadzoneL.setText("Center");
			joyP1Deadzone.setModel(new DefaultComboBoxModel(PADDLE_CENTER_OPTIONS));
			joyP1Deadzone.setSelectedItem(Parameters.JOY_P1_PADDLE_CENTER == 0 ? PADDLE_CENTER_ZERO : String.format("%+d", Parameters.JOY_P1_PADDLE_CENTER));
			joyP1Sensitivity.setEnabled(joy1Present);
			joyP1Sensitivity.setSelectedItem(Parameters.JOY_P1_PADDLE_SENS == 0 ? PADDLE_SENS_DIGITAL : "" + Parameters.JOY_P1_PADDLE_SENS + "%");
			joyP1XAxisL.setText("Axis");
			joyP1XAxis.setSelectedIndex(Parameters.JOY_P1_PAD_AXIS + (Parameters.JOY_P1_PAD_AXIS_SIGNAL == -1 ? 6 : 0));
			joyP1YAxis.setEnabled(false);
			joyP1YAxis.setSelectedIndex(1);
			joystick0Icon.setIcon(joy0Present ? paddleIcon : paddleIconGrayed);
			joystick1Icon.setIcon(joy1Present ? paddleIcon : paddleIconGrayed);
		} else {
			joyP0DeadzoneL.setText("Dead zone");
			joyP0Deadzone.setModel(new DefaultComboBoxModel(JOY_DEADZONE_OPTIONS));
			joyP0Deadzone.setSelectedItem("" + Parameters.JOY_P0_DEADZONE + "%");
			joyP0Sensitivity.setEnabled(false);
			joyP0Sensitivity.setSelectedIndex(0);
			joyP0XAxisL.setText("X Axis");
			joyP0XAxis.setSelectedIndex(Parameters.JOY_P0_XAXIS + (Parameters.JOY_P0_XAXIS_SIGNAL == -1 ? 6 : 0));
			joyP0YAxis.setEnabled(joy0Present);
			joyP0YAxis.setSelectedIndex(Parameters.JOY_P0_YAXIS + (Parameters.JOY_P0_YAXIS_SIGNAL == -1 ? 6 : 0));
			joyP1DeadzoneL.setText("Dead zone");
			joyP1Deadzone.setModel(new DefaultComboBoxModel(JOY_DEADZONE_OPTIONS));
			joyP1Deadzone.setSelectedItem("" + Parameters.JOY_P1_DEADZONE + "%");
			joyP1Sensitivity.setEnabled(false);
			joyP1Sensitivity.setSelectedIndex(0);
			joyP1XAxisL.setText("X Axis");
			joyP1XAxis.setSelectedIndex(Parameters.JOY_P1_XAXIS + (Parameters.JOY_P1_XAXIS_SIGNAL == -1 ? 6 : 0));
			joyP1YAxis.setEnabled(joy1Present);
			joyP1YAxis.setSelectedIndex(Parameters.JOY_P1_YAXIS + (Parameters.JOY_P1_YAXIS_SIGNAL == -1 ? 6 : 0));
			joystick0Icon.setIcon(joy0Present ? joystickIcon : joystickIconGrayed);
			joystick1Icon.setIcon(joy1Present ? joystickIcon : joystickIconGrayed);
		}
		for (JTextField f : joystick0FieldsList)
			f.setEnabled(joy0Present);
		for (JTextField f : joystick1FieldsList)
			f.setEnabled(joy1Present);
		for (JTextField field : joysticksFieldsList)
			if (field == joystickEditField) {
				field.setBackground(EDDITING); field.setSelectionStart(0); field.setSelectionEnd(0);
			} else field.setBackground(field.getText().trim().isEmpty() ? UNMAPPED : Color.WHITE);
		joysticksEdittingL.setText(joystickEditField != null ? "- press button -" : "");
		defaultsB.setText("Defaults");
		defaultsB.setVisible(true);
		String message;
		if (room.awtControls().p1ControlsMode ^ room.joystickControls().swappedMode) {
			joysticksPlayer1Lb.setText("Player 2"); joysticksPlayer2Lb.setText("Player 1");
			message = "Swapped";
		} else {
			joysticksPlayer1Lb.setText("Player 1"); joysticksPlayer2Lb.setText("Player 2");
			message = "";
		}
		joysticksMessageL.setText(message);
		refreshInProgress = false;
	}

	private void refreshCartridge() {
		if (refreshInProgress) return;
		refreshInProgress = true;
		if (room.currentConsole().cartridgeSocket().inserted() == null) {
			romNameTf.setText("<NO CARTRIDGE INSERTED>");
			romFormatLb.setListData(new Object[0]);
			romFormatLb.setEnabled(false);
			defaultsB.setVisible(false);
		} else {
			Cartridge cart = room.currentConsole().cartridgeSocket().inserted();
			romNameTf.setText(cart.rom().info.name);
			romNameTf.setCaretPosition(0);
			ArrayList<CartridgeFormatOption> formatOptions = CartridgeDatabase.getFormatOptions(cart.rom());
			ArrayList<CartridgeFormat> formats = new ArrayList<CartridgeFormat>();
			for (CartridgeFormatOption option : formatOptions)
				formats.add(option.format);
			if (!formats.contains(cart.format())) formats.add(0, cart.format());
			romFormatLb.setListData(formats.toArray());
			romFormatLb.setSelectedValue(cart.format(), true);
			romFormatLb.setEnabled(!formats.isEmpty() && !room.isClientMode());
			defaultsB.setText("Auto Detect");
			defaultsB.setVisible(!room.isClientMode());
		}
		refreshInProgress = false;
	}

	private void refreshMultiplayer() {
		if (refreshInProgress) return;
		refreshInProgress = true;
		boolean serverMode = room.isServerMode();
		boolean clientMode = room.isClientMode();
		multiplayerModeL.setText(serverMode ? "P1 SERVER MODE" : clientMode ? "P2 CLIENT MODE" : "STANDALONE MODE");
		multiplayerModeL.setForeground(serverMode ? new Color(47,92,180) : clientMode ? new Color(169,46,34) : new Color(0, 100, 30));
		serverStartB.setText(serverMode ? "STOP" : "START");
		serverStartB.setEnabled(!clientMode);
		serverPortTf.setEditable(!serverMode && !clientMode);
		clientConnectB.setText(clientMode ? "LEAVE" : "JOIN");
		clientConnectB.setEnabled(!serverMode);
		clientServerAddressTf.setEditable(!serverMode && !clientMode);
		refreshMultiplayerImages();
		defaultsB.setText("Defaults");
		defaultsB.setVisible(!serverMode && !clientMode);
		refreshInProgress = false;
	}

	private void refreshMultiplayerImages() {
		boolean serverMode = room.isServerMode();
		boolean clientMode = room.isClientMode();
		boolean clientConnected = serverMode && room.serverCurrentConsole().remoteTransmitter().isClientConnected();
		boolean connectedToServer = clientMode && room.clientCurrentConsole().remoteReceiver().isConnected();
		standaloneConsoleL.setVisible(!serverMode && !clientMode);
		networkL.setVisible(serverMode || clientMode);
		serverConsoleL.setVisible(serverMode || connectedToServer);
		clientConsoleL.setVisible(clientMode || clientConnected);
	}

	private void setControlsFieldsKeyListeners() {
		MouseAdapter kmLis = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) keyboardSetEditField(e);
			}
		};
		KeyAdapter kkLis = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				keyboardSetKey(e);
			}};
		for (JTextField field : keyboardFieldsList) {
			field.addKeyListener(kkLis);
			field.addMouseListener(kmLis);
		}
		MouseAdapter jmLis = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) joystickSetEditField(e);
			}
		};
		KeyAdapter jkLis = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_SPACE)
					joystickSetButton(CONTROL_UNSET);
			}};
		for (JTextField field : joysticksFieldsList) {
			field.addKeyListener(jkLis);
			field.addMouseListener(jmLis);
		}
	}

	private void keyboardSetEditField(MouseEvent e) {
		keyboardEditField = e.getSource();
		refreshKeyboard();
	}

	private void keyboardResetEditField() {
		keyboardEditField = null;
	}

	private void keyboardSetKey(KeyEvent e) {
		if (keyboardEditField == null) return;
		int code = e.getKeyCode();
		if (!KeyNames.hasName(code)) return;
		unsetKeyboardKey(code);
		if (keyboardEditField == keyP0Up) Parameters.KEY_P0_UP = code;
		else if (keyboardEditField == keyP0Down) Parameters.KEY_P0_DOWN = code;
		else if (keyboardEditField == keyP0Left) Parameters.KEY_P0_LEFT = code;
		else if (keyboardEditField == keyP0Right) Parameters.KEY_P0_RIGHT = code;
		else if (keyboardEditField == keyP0Button) Parameters.KEY_P0_BUTTON = code;
		else if (keyboardEditField == keyP0Button2) Parameters.KEY_P0_BUTTON2 = code;
		else if (keyboardEditField == keyP1Up) Parameters.KEY_P1_UP = code;
		else if (keyboardEditField == keyP1Down) Parameters.KEY_P1_DOWN = code;
		else if (keyboardEditField == keyP1Left) Parameters.KEY_P1_LEFT = code;
		else if (keyboardEditField == keyP1Right) Parameters.KEY_P1_RIGHT = code;
		else if (keyboardEditField == keyP1Button) Parameters.KEY_P1_BUTTON = code;
		else if (keyboardEditField == keyP1Button2) Parameters.KEY_P1_BUTTON2 = code;
		keyboardResetEditField();
		refreshKeyboard();
	}

	private void unsetKeyboardKey(int code) {
		if (Parameters.KEY_P0_UP == code) Parameters.KEY_P0_UP = CONTROL_UNSET;
		if (Parameters.KEY_P0_DOWN == code) Parameters.KEY_P0_DOWN = CONTROL_UNSET;
		if (Parameters.KEY_P0_LEFT == code) Parameters.KEY_P0_LEFT = CONTROL_UNSET;
		if (Parameters.KEY_P0_RIGHT == code) Parameters.KEY_P0_RIGHT = CONTROL_UNSET;
		if (Parameters.KEY_P0_BUTTON == code) Parameters.KEY_P0_BUTTON = CONTROL_UNSET;
		if (Parameters.KEY_P0_BUTTON2 == code) Parameters.KEY_P0_BUTTON2 = CONTROL_UNSET;
		if (Parameters.KEY_P1_UP == code) Parameters.KEY_P1_UP = CONTROL_UNSET;
		if (Parameters.KEY_P1_DOWN == code) Parameters.KEY_P1_DOWN = CONTROL_UNSET;
		if (Parameters.KEY_P1_LEFT == code) Parameters.KEY_P1_LEFT = CONTROL_UNSET;
		if (Parameters.KEY_P1_RIGHT == code) Parameters.KEY_P1_RIGHT = CONTROL_UNSET;
		if (Parameters.KEY_P1_BUTTON == code) Parameters.KEY_P1_BUTTON = CONTROL_UNSET;
		if (Parameters.KEY_P1_BUTTON2 == code) Parameters.KEY_P1_BUTTON2 = CONTROL_UNSET;
	}
	
	private void buildControlsFieldsLists() {
		keyboardFieldsList = Arrays.asList(new JTextField[] {
				keyP0Up, keyP0Down, keyP0Left, keyP0Right, keyP0Button, keyP0Button2,
				keyP1Up, keyP1Down,	keyP1Left, keyP1Right, keyP1Button, keyP1Button2
			});
		joystick0FieldsList = Arrays.asList(new JTextField[] {
				joyP0Button, joyP0Button2, joyP0Select, joyP0Reset, joyP0Pause, joyP0FastSpeed,
			});
		joystick1FieldsList = Arrays.asList(new JTextField[] {
				joyP1Button, joyP1Button2, joyP1Select, joyP1Reset, joyP1Pause, joyP1FastSpeed,
			});
		joysticksFieldsList = new ArrayList<JTextField>(joystick0FieldsList);
		joysticksFieldsList.addAll(joystick1FieldsList);
		List<JTextField> all = new ArrayList<JTextField>(keyboardFieldsList);
		all.addAll(joysticksFieldsList);
		for (JTextField field : all) {
			field.setHighlighter(null);
		}
	}

	private void keyboardSetKeysDefaults() {
		Parameters.setDefaultKeysPreferences();
		keyboardResetEditField();
		refreshKeyboard();
	}

	private void joysticksSetDefaultOptions() {
		Parameters.setDefaultJoystickPreferences();
		joystickResetEditField();
		room.joystickControls().start();
		refreshJoysticks();
	}

	private void joystickSetEditField(MouseEvent e) {
		if (!room.joystickControls().isStarted()) {
			refreshJoysticks();
			return;
		}
		Object field = e.getSource();
		if (!(field instanceof JTextField) || !((JTextField)field).isEnabled()) return;
		joystickEditField = e.getSource();
		if (joystick0FieldsList.contains(joystickEditField) && room.joystickControls().getJoystick0() != null)
			room.joystickControls().startButtonDetection(0, this);
		else if (joystick1FieldsList.contains(joystickEditField) && room.joystickControls().getJoystick1() != null)
			room.joystickControls().startButtonDetection(1, this);
		else return;
		refreshJoysticks();
	}

	private void joystickResetEditField() {
		room.joystickControls().stopButtonDetection();
		joystickEditField = null;
	}

	@Override
	public void joystickButtonDetected(int joystick, int button) {
		if (joystickEditField == null) return;
		if (joystick0FieldsList.contains(joystickEditField) && joystick == 0) joystickSetButton(button);
		else if (joystick1FieldsList.contains(joystickEditField) && joystick == 1) joystickSetButton(button);
		else joystickResetEditField();
	}

	private void joystickSetButton(int button) {
		if (joystickEditField == null) return;
		int joy;
		if (joystick0FieldsList.contains(joystickEditField)) joy = 0;
		else if (joystick1FieldsList.contains(joystickEditField)) joy = 1;
		else return;
		unsetJoystickButton(joy, button);
		if (joystickEditField == joyP0Button) Parameters.JOY_P0_BUTTON = button;
		else if (joystickEditField == joyP0Button2) Parameters.JOY_P0_BUTTON2 = button;
		else if (joystickEditField == joyP0Reset) Parameters.JOY_P0_RESET = button;
		else if (joystickEditField == joyP0Select) Parameters.JOY_P0_SELECT = button;
		else if (joystickEditField == joyP0Pause) Parameters.JOY_P0_PAUSE = button;
		else if (joystickEditField == joyP0FastSpeed) Parameters.JOY_P0_FAST_SPPED = button;
		else if (joystickEditField == joyP1Button) Parameters.JOY_P1_BUTTON = button;
		else if (joystickEditField == joyP1Button2) Parameters.JOY_P1_BUTTON2 = button;
		else if (joystickEditField == joyP1Reset) Parameters.JOY_P1_RESET = button;
		else if (joystickEditField == joyP1Select) Parameters.JOY_P1_SELECT = button;
		else if (joystickEditField == joyP1Pause) Parameters.JOY_P1_PAUSE = button;
		else if (joystickEditField == joyP1FastSpeed) Parameters.JOY_P1_FAST_SPPED = button;
		joystickResetEditField();
		refreshJoysticks();
	}

	private void unsetJoystickButton(int joy, int button) {
		if (joy == 0 && Parameters.JOY_P0_BUTTON == button) Parameters.JOY_P0_BUTTON = CONTROL_UNSET;
		if (joy == 0 && Parameters.JOY_P0_BUTTON2 == button) Parameters.JOY_P0_BUTTON2 = CONTROL_UNSET;
		if (joy == 0 && Parameters.JOY_P0_RESET == button) Parameters.JOY_P0_RESET = CONTROL_UNSET;
		if (joy == 0 && Parameters.JOY_P0_SELECT == button) Parameters.JOY_P0_SELECT = CONTROL_UNSET;
		if (joy == 0 && Parameters.JOY_P0_PAUSE == button) Parameters.JOY_P0_PAUSE = CONTROL_UNSET;
		if (joy == 0 && Parameters.JOY_P0_FAST_SPPED == button) Parameters.JOY_P0_FAST_SPPED = CONTROL_UNSET;
		if (joy == 1 && Parameters.JOY_P1_BUTTON == button) Parameters.JOY_P1_BUTTON = CONTROL_UNSET;
		if (joy == 1 && Parameters.JOY_P1_BUTTON2 == button) Parameters.JOY_P1_BUTTON2 = CONTROL_UNSET;
		if (joy == 1 && Parameters.JOY_P1_RESET == button) Parameters.JOY_P1_RESET = CONTROL_UNSET;
		if (joy == 1 && Parameters.JOY_P1_SELECT == button) Parameters.JOY_P1_SELECT = CONTROL_UNSET;
		if (joy == 1 && Parameters.JOY_P1_PAUSE == button) Parameters.JOY_P1_PAUSE = CONTROL_UNSET;
		if (joy == 1 && Parameters.JOY_P1_FAST_SPPED == button) Parameters.JOY_P1_FAST_SPPED = CONTROL_UNSET;
	}
	
	private void setMultiplayerDefaults() {
		serverPortTf.setText(String.valueOf(Parameters.SERVER_SERVICE_PORT));
	}

	private void setupConnectionStatusListeners() {
		if (room == null) return;
		if (room.isServerMode()) room.serverCurrentConsole().remoteTransmitter().addConnectionStatusListener(this);
		if (room.isClientMode()) room.clientCurrentConsole().remoteReceiver().addConnectionStatusListener(this);
	}
	
	private void setEscActionListener() {
		KeyStroke escKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		Action escapeAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeAction();
			}
 			private static final long serialVersionUID = 1L;
		};
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKeyStroke, "ESC");
		getRootPane().getActionMap().put("ESC", escapeAction);		
	}

	protected void joyP0P1DeviceAction() {
		if (refreshInProgress) return;
		if (joyP0Device.isEnabled()) {
			DeviceOption opt0 = (DeviceOption) joyP0Device.getSelectedItem();
			Parameters.JOY_P0_DEVICE = opt0 != null ? opt0.device : Parameters.JOY_DEVICE_AUTO;
		}
		if (joyP1Device.isEnabled()) {
			DeviceOption opt1 = (DeviceOption) joyP1Device.getSelectedItem();
			Parameters.JOY_P1_DEVICE = opt1 != null ? opt1.device : Parameters.JOY_DEVICE_AUTO;
		}
		room.joystickControls().start();
		refreshJoysticks();
	}

	protected void joyP0XAxisAction() {
		int i = joyP0XAxis.getSelectedIndex();
		if (room.joystickControls().paddleMode) {
			Parameters.JOY_P0_PAD_AXIS = JOY_AXIS_NUMBERS[i];
			Parameters.JOY_P0_PAD_AXIS_SIGNAL = JOY_AXIS_SIGNALS[i];
		} else {
			Parameters.JOY_P0_XAXIS = JOY_AXIS_NUMBERS[i];
			Parameters.JOY_P0_XAXIS_SIGNAL = JOY_AXIS_SIGNALS[i];
		}
		refreshJoysticks();
	}

	protected void joyP0YAxisAction() {
		if (room.joystickControls().paddleMode) return;
		int i = joyP0YAxis.getSelectedIndex();
		Parameters.JOY_P0_YAXIS = JOY_AXIS_NUMBERS[i];
		Parameters.JOY_P0_YAXIS_SIGNAL = JOY_AXIS_SIGNALS[i];
		refreshJoysticks();
	}

	protected void joyP0DeadzoneAction() {
		String val = joyP0Deadzone.getSelectedItem().toString().replaceAll("[^\\d-]", "");
		if (room.joystickControls().paddleMode) Parameters.JOY_P0_PADDLE_CENTER = Integer.parseInt(val);
		else Parameters.JOY_P0_DEADZONE = Integer.parseInt(val);
		refreshJoysticks();
	}

	protected void joyP0SensitivityAction() {
		if (!room.joystickControls().paddleMode) return;
		String val = joyP0Sensitivity.getSelectedItem().toString().replaceAll("[\\D]", "");
		Parameters.JOY_P0_PADDLE_SENS = val.isEmpty() ? 0 : Integer.parseInt(val);
		refreshJoysticks();
	}

	protected void joyP1XAxisAction() {
		int i = joyP1XAxis.getSelectedIndex();
		if (room.joystickControls().paddleMode) {
			Parameters.JOY_P1_PAD_AXIS = JOY_AXIS_NUMBERS[i];
			Parameters.JOY_P1_PAD_AXIS_SIGNAL = JOY_AXIS_SIGNALS[i];
		} else {
			Parameters.JOY_P1_XAXIS = JOY_AXIS_NUMBERS[i];
			Parameters.JOY_P1_XAXIS_SIGNAL = JOY_AXIS_SIGNALS[i];
		}
		refreshJoysticks();
	}

	protected void joyP1YAxisAction() {
		int i = joyP1YAxis.getSelectedIndex();
		Parameters.JOY_P1_YAXIS = JOY_AXIS_NUMBERS[i];
		Parameters.JOY_P1_YAXIS_SIGNAL = JOY_AXIS_SIGNALS[i];
		refreshJoysticks();
	}

	protected void joyP1DeadzoneAction() {
		String val = joyP1Deadzone.getSelectedItem().toString().replaceAll("[^\\d-]", "");
		if (room.joystickControls().paddleMode) Parameters.JOY_P1_PADDLE_CENTER = Integer.parseInt(val);
		else Parameters.JOY_P1_DEADZONE = Integer.parseInt(val);
		refreshJoysticks();
	}

	protected void joyP1SensitivityAction() {
		if (!room.joystickControls().paddleMode) return;
		String val = joyP1Sensitivity.getSelectedItem().toString().replaceAll("[\\D]", "");
		Parameters.JOY_P1_PADDLE_SENS = val.isEmpty() ? 0 : Integer.parseInt(val);
		refreshJoysticks();
	}

	private void mainTabbedPaneChanged() {
		if (!buildFinished) return;
		if (mainTabbedPane.getSelectedIndex() == 2) joysticksPageEntered();
		else notJoysticksPageEntered();
		refreshCurrentTab();
	}

	private void refreshCurrentTab() {
		keyboardResetEditField();
		joystickResetEditField();
		switch (mainTabbedPane.getSelectedIndex()) {
			case 0:
				refreshMultiplayer(); break;
			case 1:
				refreshKeyboard(); break;
			case 2:
				refreshJoysticks(); break;
			case 3:
				refreshCartridge(); break;
			default:
				defaultsB.setVisible(false);
		}
	}

	private void joysticksPageEntered() {
		room.joystickControls().start();
	}

	private void notJoysticksPageEntered() {
		room.joystickControls().stopButtonDetection();
	}

	private void officialWebPageAction() {
		if (!Desktop.isDesktopSupported()) return;
		try {
			Desktop desktop = Desktop.getDesktop();
			if (!desktop.isSupported(Desktop.Action.BROWSE)) return;
			closeAction();
			desktop.browse(new URI(Parameters.OFFICIAL_WEBSITE));
		} catch (Exception e) {
			// Give up
		}
	}

	private void twitterPageAction() {
		if (!Desktop.isDesktopSupported()) return;
		try {
			Desktop desktop = Desktop.getDesktop();
			if (!desktop.isSupported(Desktop.Action.BROWSE)) return;
			closeAction();
			desktop.browse(new URI(Parameters.TWITTER_WEBPAGE));
		} catch (Exception e) {
			// Give up
		}
	}
	
	private void serverStartAction() {
		if (!room.isServerMode()) {		// Will try to START
			gray(true);
			room.morphToServerMode();
			SwingHelper.edtInvokeLater(new Runnable() { @Override public void run() {
				setupConnectionStatusListeners();
				try {
					RemoteTransmitter transmitter = room.serverCurrentConsole().remoteTransmitter();
					String portString = serverPortTf.getText().trim();
					try {
						if (portString.isEmpty()) transmitter.start();
						else transmitter.start(Integer.valueOf(portString));
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("Invalid port number: " + portString);
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "Could not start Server:\n" + ex, "javatari P1 Server", JOptionPane.ERROR_MESSAGE);
					room.morphToStandaloneMode();
				}
				refreshMultiplayer();
				gray(false);
			}});
		} else {	// Will try to STOP
			try {
				RemoteTransmitter transmitter = room.serverCurrentConsole().remoteTransmitter();
				transmitter.stop();
				room.morphToStandaloneMode();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Error stopping Server:\n" + ex, "javatari P1 Server", JOptionPane.ERROR_MESSAGE);
			}
			refreshMultiplayer();
		}			
	}
	
	private void clientConnectAction() {
		if (!room.isClientMode()) {		// Will try to CONNECT
			gray(true);
			room.morphToClientMode();
			SwingHelper.edtInvokeLater(new Runnable() { @Override public void run() {
				setupConnectionStatusListeners();
				String serverAddress = "";
				try {
					RemoteReceiver receiver = room.clientCurrentConsole().remoteReceiver();
					serverAddress = clientServerAddressTf.getText().trim();
					receiver.connect(serverAddress);
					closeAction();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "Connection failed: " + serverAddress + "\n" + ex, "javatari P2 Client", JOptionPane.ERROR_MESSAGE);
					room.morphToStandaloneMode();
				}
				refreshMultiplayer();
				gray(false);
			}});
		} else {	// Will try to DISCONNECT 
			try {
				RemoteReceiver receiver = room.clientCurrentConsole().remoteReceiver();
				receiver.disconnect();
				room.morphToStandaloneMode();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Error disconnecting from Server:\n" + ex, "javatari P2 Client", JOptionPane.ERROR_MESSAGE);
			}
			refreshMultiplayer();
		}
	}
	
	private void gray(boolean state) {
		setEnabled(!state);
		getGlassPane().setVisible(state);
		repaint();
	}

	private void defaultsAction() {
		switch(mainTabbedPane.getSelectedIndex()) {
			case 0:
				setMultiplayerDefaults(); break;
			case 1:
				keyboardSetKeysDefaults(); break;
			case 2:
				joysticksSetDefaultOptions(); break;
			case 3:
				cartridgeAutoDetect(); break;
		}
	}

	private void closeAction() {
		Parameters.savePreferences();
		room.awtControls().initPreferences();
		room.joystickControls().initPreferences();
		setVisible(false);
	}

	private void romFormatLbAction() {
		Object sel = romFormatLb.getSelectedValue();
		if (sel == null || !(sel instanceof CartridgeFormat)) return;
		CartridgeFormat format = (CartridgeFormat) sel;
		Console console = room.currentConsole();
		Cartridge cart = console.cartridgeSocket().inserted();
		if (cart == null || cart.format().equals(format)) return;
		Cartridge newCart = format.createCartridge(cart.rom());
		console.cartridgeSocket().insert(newCart, true);
	}
	
	private void cartridgeAutoDetect() {
		Console console = room.currentConsole();
		Cartridge cart = console.cartridgeSocket().inserted();
		if (cart == null) return;
		ArrayList<CartridgeFormatOption> options = CartridgeDatabase.getFormatOptions(cart.rom());
		if (options.isEmpty()) return;
		Cartridge newCart = options.get(0).format.createCartridge(cart.rom());
		console.cartridgeSocket().insert(newCart, true);
		refreshCartridge();
	}

	private void loadImages() {
		try {
			BufferedImage image = SwingHelper.loadAsCompatibleTranslucentImage("org/javatari/pc/room/settings/images/Joystick.png");
			joystickIcon = new ImageIcon(image);
			joystickIconGrayed = new ImageIcon(GrayFilter.createDisabledImage(image));
			image = SwingHelper.loadAsCompatibleTranslucentImage("org/javatari/pc/room/settings/images/Paddle.png");
			paddleIcon = new ImageIcon(image);
			paddleIconGrayed = new ImageIcon(GrayFilter.createDisabledImage(image));
		} catch (IOException ex) {
			System.out.println("SettingsDialog: unable to load images\n" + ex);
		}
	}

	private void buildGUI() {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		setIconImage(Toolkit.getDefaultToolkit().getImage(SettingsDialog.class.getResource("/org/javatari/pc/screen/images/Favicon.png")));
		setTitle("javatari");
		setResizable(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeAction();
			}
		});
		
		final AbstractAction toggleP1ModeAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				room.awtControls().toggleP1ControlsMode();
				refreshCurrentTab();
			}
			private static final long serialVersionUID = 1L;
		};
		final AbstractAction togglePaddlesAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				room.awtControls().togglePaddleMode();
				refreshCurrentTab();
			}
			private static final long serialVersionUID = 1L;
		};
		final AbstractAction toggleJoystickAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				room.joystickControls().toggleMode();
				refreshCurrentTab();
			}
			private static final long serialVersionUID = 1L;
		};
		final AbstractAction cycleROMFormatAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				room.currentConsole().controlsSocket().controlStateChanged(Control.CARTRIDGE_FORMAT, true);
				refreshCurrentTab();
			}
			private static final long serialVersionUID = 1L;
		};

		String fontName = "SansSerif";
		int adjust = 0;
		if (Environment.ARIAL_FONT) fontName = "Arial";
		else if (Environment.LIBERATION_FONT) fontName = "Liberation Sans";
		else adjust = -1;
		
		Font fontFields = new Font(fontName, Font.PLAIN, 11);
		Font fontLabel = new Font(fontName, Font.PLAIN, 13 + adjust);
		Font fontLabelBold = new Font(fontName, Font.BOLD, 13 + adjust);
		Font fontLabelMedium = new Font(fontName, Font.PLAIN, 14 + adjust);
		Font fontLabelMediumBold = new Font(fontName, Font.BOLD, 14 + adjust);
		Font fontLabelLarge = new Font(fontName, Font.PLAIN, 16 + adjust);
		Font fontLabelLargeBold = new Font(fontName, Font.BOLD, 16 + adjust);
	

		Color noBackground = new Color(0, 0, 0, 0);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			mainTabbedPane = new JTabbedPane(JTabbedPane.TOP);
			if (Environment.NIMBUS_LAF) mainTabbedPane.setBorder(new DeepBorder(13, new Insets(7, 2, 6, 2)));
			mainTabbedPane.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					mainTabbedPaneChanged();
				}
			});
			mainTabbedPane.setFont(fontLabel);
			mainTabbedPane.setBackground(UIManager.getColor("TabbedPane.background"));
			contentPanel.add(mainTabbedPane, BorderLayout.CENTER);
			
			JPanel multiplayerPanel = new JPanel();
			mainTabbedPane.addTab("Multiplayer", null, multiplayerPanel, null);
			multiplayerPanel.setLayout(null);
			multiplayerPanel.setPreferredSize(INTERNAL_TAB_SIZE);
			
			clientConsoleL = new JLabel("");
			clientConsoleL.setIcon(new ImageIcon(SettingsDialog.class.getResource("/org/javatari/pc/room/settings/images/ServerClientConsole.png")));
			clientConsoleL.setBounds(316, 132, 139, 94);
			multiplayerPanel.add(clientConsoleL);
			
			serverConsoleL = new JLabel("");
			serverConsoleL.setIcon(new ImageIcon(SettingsDialog.class.getResource("/org/javatari/pc/room/settings/images/ServerClientConsole.png")));
			serverConsoleL.setBounds(12, 132, 139, 94);
			multiplayerPanel.add(serverConsoleL);
			
			networkL = new JLabel("");
			networkL.setIcon(new ImageIcon(SettingsDialog.class.getResource("/org/javatari/pc/room/settings/images/Network.png")));
			networkL.setBounds(116, 81, 237, 98);
			multiplayerPanel.add(networkL);
			
			JLabel lblNewLabel_1 = new JLabel("P1 Server");
			lblNewLabel_1.setFont(fontLabelLargeBold);
			lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
			lblNewLabel_1.setBounds(24, 13, 100, 20);
			multiplayerPanel.add(lblNewLabel_1);
			
			serverStartB = new JButton("START");
			serverStartB.setFont(fontLabel);
			serverStartB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					serverStartAction();
				}
			});
			serverStartB.setBounds(20, 36, 108, 26);
			multiplayerPanel.add(serverStartB);
			
			clientConnectB = new JButton("JOIN");
			clientConnectB.setFont(fontLabel);
			clientConnectB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clientConnectAction();
				}
			});
			clientConnectB.setBounds(341, 36, 108, 26);
			multiplayerPanel.add(clientConnectB);
			
			clientServerAddressTf = new JTextFieldNim();
			clientServerAddressTf.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clientConnectAction();
				}
			});
			clientServerAddressTf.setFont(fontLabel);
			clientServerAddressTf.setBounds(335, 85, 121, 27);
			multiplayerPanel.add(clientServerAddressTf);
			clientServerAddressTf.setColumns(10);
			
			JLabel lblServerAddressport = new JLabel("Server address [:port]");
			lblServerAddressport.setHorizontalAlignment(SwingConstants.CENTER);
			lblServerAddressport.setFont(fontLabel);
			lblServerAddressport.setBounds(323, 70, 144, 15);
			multiplayerPanel.add(lblServerAddressport);
			
			serverPortTf = new JTextFieldNim();
			serverPortTf.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					serverStartAction();
				}
			});
			serverPortTf.setFont(fontLabel);
			serverPortTf.setHorizontalAlignment(SwingConstants.RIGHT);
			serverPortTf.setColumns(10);
			serverPortTf.setBounds(41, 85, 66, 27);
			multiplayerPanel.add(serverPortTf);
			
			JLabel lblPort = new JLabel("Server port");
			lblPort.setHorizontalAlignment(SwingConstants.CENTER);
			lblPort.setFont(fontLabel);
			lblPort.setBounds(32, 70, 82, 15);
			multiplayerPanel.add(lblPort);
			
			JLabel lblPClient = new JLabel("P2 Client");
			lblPClient.setHorizontalAlignment(SwingConstants.CENTER);
			lblPClient.setFont(fontLabelLargeBold);
			lblPClient.setBounds(345, 13, 100, 20);
			multiplayerPanel.add(lblPClient);
			
			multiplayerModeL = new JLabel("P1 SERVER MODE");
			multiplayerModeL.setFont(fontLabelMediumBold);
			multiplayerModeL.setOpaque(false);
			multiplayerModeL.setBorder(new DeepBorder(10, new Insets(5, 5, 5, 5)));
			multiplayerModeL.setHorizontalAlignment(SwingConstants.CENTER);
			multiplayerModeL.setBounds(148, 34, 174, 30);
			multiplayerPanel.add(multiplayerModeL);
			
			standaloneConsoleL = new JLabel("");
			standaloneConsoleL.setIcon(new ImageIcon(SettingsDialog.class.getResource("/org/javatari/pc/room/settings/images/StandaloneConsole.png")));
			standaloneConsoleL.setBounds(120, 90, 202, 146);
			multiplayerPanel.add(standaloneConsoleL);
			{
				keyboardPanel = new JPanel();
				mainTabbedPane.addTab("Keyboard", null, keyboardPanel, null);
				keyboardPanel.setLayout(null);
				keyboardPanel.setPreferredSize(INTERNAL_TAB_SIZE);
				{
					keyboard0Icon = new JLabel("");
					keyboard0Icon.setIcon(joystickIcon);
					keyboard0Icon.setBounds(70, 91, 75, 90);
					keyboardPanel.add(keyboard0Icon);
				}
				
				keyP0Up = new JTextFieldNim();
				keyP0Up.setDisabledTextColor(Color.GRAY);
				keyP0Up.setBackground(Color.WHITE);
				keyP0Up.setEditable(false);
				keyP0Up.setFont(fontFields);
				keyP0Up.setHorizontalAlignment(SwingConstants.CENTER);
				keyP0Up.setBounds(85, 67, 44, 23);
				keyP0Up.setColumns(10);
				keyboardPanel.add(keyP0Up);
				
				keyP0Right = new JTextFieldNim();
				keyP0Right.setDisabledTextColor(Color.GRAY);
				keyP0Right.setBackground(Color.WHITE);
				keyP0Right.setEditable(false);
				keyP0Right.setFont(fontFields);
				keyP0Right.setHorizontalAlignment(SwingConstants.CENTER);
				keyP0Right.setColumns(10);
				keyP0Right.setBounds(151, 130, 45, 23);
				keyboardPanel.add(keyP0Right);
				
				keyP0Left = new JTextFieldNim();
				keyP0Left.setDisabledTextColor(Color.GRAY);
				keyP0Left.setBackground(Color.WHITE);
				keyP0Left.setEditable(false);
				keyP0Left.setFont(fontFields);
				keyP0Left.setHorizontalAlignment(SwingConstants.CENTER);
				keyP0Left.setColumns(10);
				keyP0Left.setBounds(19, 130, 45, 23);
				keyboardPanel.add(keyP0Left);
				
				keyP0Down = new JTextFieldNim();
				keyP0Down.setDisabledTextColor(Color.GRAY);
				keyP0Down.setBackground(Color.WHITE);
				keyP0Down.setEditable(false);
				keyP0Down.setFont(fontFields);
				keyP0Down.setHorizontalAlignment(SwingConstants.CENTER);
				keyP0Down.setColumns(10);
				keyP0Down.setBounds(85, 194, 45, 23);
				keyboardPanel.add(keyP0Down);
				
				JLabel lblRight = new JLabel("Right");
				lblRight.setFont(fontLabel);
				lblRight.setHorizontalAlignment(SwingConstants.CENTER);
				lblRight.setBounds(153, 116, 40, 15);
				keyboardPanel.add(lblRight);
				
				JLabel lblLeft = new JLabel("Left");
				lblLeft.setFont(fontLabel);
				lblLeft.setHorizontalAlignment(SwingConstants.CENTER);
				lblLeft.setBounds(21, 116, 40, 14);
				keyboardPanel.add(lblLeft);
				
				keyP0DownL = new JLabel("Down");
				keyP0DownL.setFont(fontLabel);
				keyP0DownL.setHorizontalAlignment(SwingConstants.CENTER);
				keyP0DownL.setBounds(78, 180, 58, 14);
				keyboardPanel.add(keyP0DownL);
				
				keyP0UpL = new JLabel("Up");
				keyP0UpL.setFont(fontLabel);
				keyP0UpL.setHorizontalAlignment(SwingConstants.CENTER);
				keyP0UpL.setBounds(78, 53, 58, 14);
				keyboardPanel.add(keyP0UpL);
				
				keyP0Button = new JTextFieldNim();
				keyP0Button.setDisabledTextColor(Color.GRAY);
				keyP0Button.setBackground(Color.WHITE);
				keyP0Button.setEditable(false);
				keyP0Button.setFont(fontFields);
				keyP0Button.setHorizontalAlignment(SwingConstants.CENTER);
				keyP0Button.setColumns(10);
				keyP0Button.setBounds(29, 79, 44, 23);
				keyboardPanel.add(keyP0Button);
				
				JLabel lblFire = new JLabel("Fire 1");
				lblFire.setFont(fontLabel);
				lblFire.setHorizontalAlignment(SwingConstants.CENTER);
				lblFire.setBounds(31, 65, 40, 14);
				keyboardPanel.add(lblFire);
				
				keyboardPlayer1Lb = new JLabel("Player 1");
				keyboardPlayer1Lb.setFont(fontLabelLargeBold);
				keyboardPlayer1Lb.setHorizontalAlignment(SwingConstants.CENTER);
				keyboardPlayer1Lb.setBounds(70, 22, 74, 20);
				keyboardPanel.add(keyboardPlayer1Lb);
				
				JLabel lblFire_1 = new JLabel("Fire 2");
				lblFire_1.setFont(fontLabel);
				lblFire_1.setHorizontalAlignment(SwingConstants.CENTER);
				lblFire_1.setBounds(143, 65, 40, 14);
				keyboardPanel.add(lblFire_1);
				
				keyP0Button2 = new JTextFieldNim();
				keyP0Button2.setDisabledTextColor(Color.GRAY);
				keyP0Button2.setBackground(Color.WHITE);
				keyP0Button2.setEditable(false);
				keyP0Button2.setFont(fontFields);
				keyP0Button2.setHorizontalAlignment(SwingConstants.CENTER);
				keyP0Button2.setColumns(10);
				keyP0Button2.setBounds(141, 79, 44, 23);
				keyboardPanel.add(keyP0Button2);
				
				JLabel txtpnAltK_1 = new JLabel();
				txtpnAltK_1.setHorizontalTextPosition(SwingConstants.CENTER);
				txtpnAltK_1.setHorizontalAlignment(SwingConstants.CENTER);
				txtpnAltK_1.setOpaque(false);
				txtpnAltK_1.setFont(fontLabel);
				txtpnAltK_1.setText("ALT + K: Swap Controls");
				txtpnAltK_1.setBounds(143, 215, 185, 21);
				keyboardPanel.add(txtpnAltK_1);
				txtpnAltK_1.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						toggleP1ModeAction.actionPerformed(null);
					}
				});
				
				keyboard1Icon = new JLabel("");
				keyboard1Icon.setIcon(joystickIcon);
				keyboard1Icon.setBounds(327, 91, 75, 90);
				keyboardPanel.add(keyboard1Icon);
				
				keyP1Up = new JTextFieldNim();
				keyP1Up.setDisabledTextColor(Color.GRAY);
				keyP1Up.setHorizontalAlignment(SwingConstants.CENTER);
				keyP1Up.setFont(fontFields);
				keyP1Up.setEditable(false);
				keyP1Up.setColumns(10);
				keyP1Up.setBackground(Color.WHITE);
				keyP1Up.setBounds(342, 67, 44, 23);
				keyboardPanel.add(keyP1Up);
				
				keyP1Right = new JTextFieldNim();
				keyP1Right.setDisabledTextColor(Color.GRAY);
				keyP1Right.setHorizontalAlignment(SwingConstants.CENTER);
				keyP1Right.setFont(fontFields);
				keyP1Right.setEditable(false);
				keyP1Right.setColumns(10);
				keyP1Right.setBackground(Color.WHITE);
				keyP1Right.setBounds(407, 130, 45, 23);
				keyboardPanel.add(keyP1Right);
				
				keyP1Left = new JTextFieldNim();
				keyP1Left.setDisabledTextColor(Color.GRAY);
				keyP1Left.setHorizontalAlignment(SwingConstants.CENTER);
				keyP1Left.setFont(fontFields);
				keyP1Left.setEditable(false);
				keyP1Left.setColumns(10);
				keyP1Left.setBackground(Color.WHITE);
				keyP1Left.setBounds(276, 130, 45, 23);
				keyboardPanel.add(keyP1Left);
				
				keyP1Down = new JTextFieldNim();
				keyP1Down.setDisabledTextColor(Color.GRAY);
				keyP1Down.setHorizontalAlignment(SwingConstants.CENTER);
				keyP1Down.setFont(fontFields);
				keyP1Down.setEditable(false);
				keyP1Down.setColumns(10);
				keyP1Down.setBackground(Color.WHITE);
				keyP1Down.setBounds(342, 194, 45, 23);
				keyboardPanel.add(keyP1Down);
				
				JLabel label_1 = new JLabel("Right");
				label_1.setHorizontalAlignment(SwingConstants.CENTER);
				label_1.setFont(fontLabel);
				label_1.setBounds(409, 116, 40, 15);
				keyboardPanel.add(label_1);
				
				JLabel label_2 = new JLabel("Left");
				label_2.setHorizontalAlignment(SwingConstants.CENTER);
				label_2.setFont(fontLabel);
				label_2.setBounds(278, 116, 40, 14);
				keyboardPanel.add(label_2);
				
				keyP1DownL = new JLabel("Down");
				keyP1DownL.setHorizontalAlignment(SwingConstants.CENTER);
				keyP1DownL.setFont(fontLabel);
				keyP1DownL.setBounds(335, 180, 58, 14);
				keyboardPanel.add(keyP1DownL);
				
				keyP1UpL = new JLabel("Up");
				keyP1UpL.setHorizontalAlignment(SwingConstants.CENTER);
				keyP1UpL.setFont(fontLabel);
				keyP1UpL.setBounds(335, 53, 58, 14);
				keyboardPanel.add(keyP1UpL);
				
				keyP1Button = new JTextFieldNim();
				keyP1Button.setDisabledTextColor(Color.GRAY);
				keyP1Button.setHorizontalAlignment(SwingConstants.CENTER);
				keyP1Button.setFont(fontFields);
				keyP1Button.setEditable(false);
				keyP1Button.setColumns(10);
				keyP1Button.setBackground(Color.WHITE);
				keyP1Button.setBounds(286, 79, 44, 23);
				keyboardPanel.add(keyP1Button);
				
				JLabel label_5 = new JLabel("Fire 1");
				label_5.setHorizontalAlignment(SwingConstants.CENTER);
				label_5.setFont(fontLabel);
				label_5.setBounds(288, 65, 40, 14);
				keyboardPanel.add(label_5);
				
				keyboardPlayer2Lb = new JLabel("Player 2");
				keyboardPlayer2Lb.setHorizontalAlignment(SwingConstants.CENTER);
				keyboardPlayer2Lb.setFont(fontLabelLargeBold);
				keyboardPlayer2Lb.setBounds(327, 22, 74, 20);
				keyboardPanel.add(keyboardPlayer2Lb);
				
				JLabel label_7 = new JLabel("Fire 2");
				label_7.setHorizontalAlignment(SwingConstants.CENTER);
				label_7.setFont(fontLabel);
				label_7.setBounds(399, 65, 40, 14);
				keyboardPanel.add(label_7);
				
				keyP1Button2 = new JTextFieldNim();
				keyP1Button2.setDisabledTextColor(Color.GRAY);
				keyP1Button2.setHorizontalAlignment(SwingConstants.CENTER);
				keyP1Button2.setFont(fontFields);
				keyP1Button2.setEditable(false);
				keyP1Button2.setColumns(10);
				keyP1Button2.setBackground(Color.WHITE);
				keyP1Button2.setBounds(397, 79, 44, 23);
				keyboardPanel.add(keyP1Button2);
				
				keyboardMessageL = new JLabel("");
				keyboardMessageL.setHorizontalAlignment(SwingConstants.CENTER);
				keyboardMessageL.setFont(fontLabel);
				keyboardMessageL.setBounds(165, 25, 141, 15);
				keyboardPanel.add(keyboardMessageL);
				
				JLabel txtpnAltL = new JLabel();
				txtpnAltL.setHorizontalTextPosition(SwingConstants.CENTER);
				txtpnAltL.setHorizontalAlignment(SwingConstants.CENTER);
				txtpnAltL.setText("ALT + L: Toggle Paddles");
				txtpnAltL.setOpaque(false);
				txtpnAltL.setFont(fontLabel);
				txtpnAltL.setBounds(143, 235, 185, 21);
				keyboardPanel.add(txtpnAltL);
				txtpnAltL.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						togglePaddlesAction.actionPerformed(null);
					}
				});
			}
			
			joysticksPanel = new JPanel();
			joysticksPanel.setLayout(null);
			joysticksPanel.setPreferredSize(INTERNAL_TAB_SIZE);
			mainTabbedPane.addTab("Joysticks", null, joysticksPanel, null);
			
			joystick0Icon = new JLabel("");
			joystick0Icon.setIcon(joystickIcon);
			joystick0Icon.setBounds(70, 91, 75, 90);
			joysticksPanel.add(joystick0Icon);
			
			joyP0Reset = new JTextFieldNim();
			joyP0Reset.setDisabledTextColor(Color.GRAY);
			joyP0Reset.setEnabled(false);
			joyP0Reset.setName("joyP0Reset");
			joyP0Reset.setHorizontalAlignment(SwingConstants.CENTER);
			joyP0Reset.setFont(fontFields);
			joyP0Reset.setEditable(false);
			joyP0Reset.setColumns(10);
			joyP0Reset.setBackground(Color.WHITE);
			joyP0Reset.setBounds(85, 194, 44, 23);
			joysticksPanel.add(joyP0Reset);
			
			joyP0Pause = new JTextFieldNim();
			joyP0Pause.setDisabledTextColor(Color.GRAY);
			joyP0Pause.setEnabled(false);
			joyP0Pause.setName("joyP0Pause");
			joyP0Pause.setHorizontalAlignment(SwingConstants.CENTER);
			joyP0Pause.setFont(fontFields);
			joyP0Pause.setEditable(false);
			joyP0Pause.setColumns(10);
			joyP0Pause.setBackground(Color.WHITE);
			joyP0Pause.setBounds(85, 232, 44, 23);
			joysticksPanel.add(joyP0Pause);
			
			JLabel lblSelect = new JLabel("Select");
			lblSelect.setHorizontalAlignment(SwingConstants.CENTER);
			lblSelect.setFont(fontLabel);
			lblSelect.setBounds(28, 180, 40, 15);
			joysticksPanel.add(lblSelect);
			
			JLabel lblFire_2 = new JLabel("Pause");
			lblFire_2.setHorizontalAlignment(SwingConstants.CENTER);
			lblFire_2.setFont(fontLabel);
			lblFire_2.setBounds(87, 218, 40, 14);
			joysticksPanel.add(lblFire_2);
			
			joyP0Button2 = new JTextFieldNim();
			joyP0Button2.setDisabledTextColor(Color.GRAY);
			joyP0Button2.setEnabled(false);
			joyP0Button2.setName("joyP0Button2");
			joyP0Button2.setHorizontalAlignment(SwingConstants.CENTER);
			joyP0Button2.setFont(fontFields);
			joyP0Button2.setEditable(false);
			joyP0Button2.setColumns(10);
			joyP0Button2.setBackground(Color.WHITE);
			joyP0Button2.setBounds(141, 79, 44, 23);
			joysticksPanel.add(joyP0Button2);
			
			JLabel lblFire_5 = new JLabel("Fire 2");
			lblFire_5.setHorizontalAlignment(SwingConstants.CENTER);
			lblFire_5.setFont(fontLabel);
			lblFire_5.setBounds(143, 65, 40, 14);
			joysticksPanel.add(lblFire_5);
			
			joysticksPlayer1Lb = new JLabel("Player 1");
			joysticksPlayer1Lb.setHorizontalAlignment(SwingConstants.CENTER);
			joysticksPlayer1Lb.setFont(fontLabelLargeBold);
			joysticksPlayer1Lb.setBounds(78, 6, 74, 20);
			joysticksPanel.add(joysticksPlayer1Lb);
			
			JLabel lblReset = new JLabel("Fire 1");
			lblReset.setHorizontalAlignment(SwingConstants.CENTER);
			lblReset.setFont(fontLabel);
			lblReset.setBounds(31, 65, 40, 15);
			joysticksPanel.add(lblReset);
			
			joyP0Button = new JTextFieldNim();
			joyP0Button.setDisabledTextColor(Color.GRAY);
			joyP0Button.setEnabled(false);
			joyP0Button.setName("joyP0Button");
			joyP0Button.setHorizontalAlignment(SwingConstants.CENTER);
			joyP0Button.setFont(fontFields);
			joyP0Button.setEditable(false);
			joyP0Button.setColumns(10);
			joyP0Button.setBackground(Color.WHITE);
			joyP0Button.setBounds(29, 79, 44, 23);
			joysticksPanel.add(joyP0Button);
			
			joystick1Icon = new JLabel("");
			joystick1Icon.setIcon(joystickIcon);
			joystick1Icon.setBounds(327, 91, 75, 90);
			joysticksPanel.add(joystick1Icon);
			
			joyP1Pause = new JTextFieldNim();
			joyP1Pause.setDisabledTextColor(Color.GRAY);
			joyP1Pause.setEnabled(false);
			joyP1Pause.setName("joyP1Pause");
			joyP1Pause.setHorizontalAlignment(SwingConstants.CENTER);
			joyP1Pause.setFont(fontFields);
			joyP1Pause.setEditable(false);
			joyP1Pause.setColumns(10);
			joyP1Pause.setBackground(Color.WHITE);
			joyP1Pause.setBounds(400, 232, 44, 23);
			joysticksPanel.add(joyP1Pause);
			
			JLabel lblFire_3 = new JLabel("Pause");
			lblFire_3.setHorizontalAlignment(SwingConstants.CENTER);
			lblFire_3.setFont(fontLabel);
			lblFire_3.setBounds(402, 218, 40, 14);
			joysticksPanel.add(lblFire_3);
			
			joysticksPlayer2Lb = new JLabel("Player 2");
			joysticksPlayer2Lb.setHorizontalAlignment(SwingConstants.CENTER);
			joysticksPlayer2Lb.setFont(fontLabelLargeBold);
			joysticksPlayer2Lb.setBounds(319, 6, 74, 20);
			joysticksPanel.add(joysticksPlayer2Lb);
			
			JLabel lblFire_4 = new JLabel("Fire 1");
			lblFire_4.setHorizontalAlignment(SwingConstants.CENTER);
			lblFire_4.setFont(fontLabel);
			lblFire_4.setBounds(288, 65, 40, 14);
			joysticksPanel.add(lblFire_4);
			
			joyP1Button = new JTextFieldNim();
			joyP1Button.setDisabledTextColor(Color.GRAY);
			joyP1Button.setEnabled(false);
			joyP1Button.setName("joyP1Button");
			joyP1Button.setHorizontalAlignment(SwingConstants.CENTER);
			joyP1Button.setFont(fontFields);
			joyP1Button.setEditable(false);
			joyP1Button.setColumns(10);
			joyP1Button.setBackground(Color.WHITE);
			joyP1Button.setBounds(286, 79, 44, 23);
			joysticksPanel.add(joyP1Button);
			
			joysticksMessageL = new JLabel("");
			joysticksMessageL.setHorizontalAlignment(SwingConstants.CENTER);
			joysticksMessageL.setFont(fontLabel);
			joysticksMessageL.setBounds(160, 9, 151, 15);
			joysticksPanel.add(joysticksMessageL);
			
			JLabel txtpnAltJ_1 = new JLabel();
			txtpnAltJ_1.setHorizontalTextPosition(SwingConstants.CENTER);
			txtpnAltJ_1.setHorizontalAlignment(SwingConstants.CENTER);
			txtpnAltJ_1.setText("ALT + J: Swap Joysticks");
			txtpnAltJ_1.setOpaque(false);
			txtpnAltJ_1.setFont(fontLabel);
			txtpnAltJ_1.setBounds(155, 215, 163, 21);
			joysticksPanel.add(txtpnAltJ_1);
			txtpnAltJ_1.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					toggleJoystickAction.actionPerformed(null);
				}
			});
			
			JLabel txtpnAltL_2 = new JLabel();
			txtpnAltL_2.setHorizontalTextPosition(SwingConstants.CENTER);
			txtpnAltL_2.setHorizontalAlignment(SwingConstants.CENTER);
			txtpnAltL_2.setText("ALT + L: Toggle Paddles");
			txtpnAltL_2.setOpaque(false);
			txtpnAltL_2.setFont(fontLabel);
			txtpnAltL_2.setBounds(146, 235, 179, 21);
			joysticksPanel.add(txtpnAltL_2);
			txtpnAltL_2.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					togglePaddlesAction.actionPerformed(null);
				}
			});
			
			JLabel lblFastSpeed = new JLabel("Reset");
			lblFastSpeed.setHorizontalAlignment(SwingConstants.CENTER);
			lblFastSpeed.setFont(fontLabel);
			lblFastSpeed.setBounds(87, 180, 40, 15);
			joysticksPanel.add(lblFastSpeed);
			
			joyP0Select = new JTextFieldNim();
			joyP0Select.setDisabledTextColor(Color.GRAY);
			joyP0Select.setEnabled(false);
			joyP0Select.setName("joyP0Select");
			joyP0Select.setHorizontalAlignment(SwingConstants.CENTER);
			joyP0Select.setFont(fontFields);
			joyP0Select.setEditable(false);
			joyP0Select.setColumns(10);
			joyP0Select.setBackground(Color.WHITE);
			joyP0Select.setBounds(26, 194, 44, 23);
			joysticksPanel.add(joyP0Select);
			
			joyP1Select = new JTextFieldNim();
			joyP1Select.setDisabledTextColor(Color.GRAY);
			joyP1Select.setEnabled(false);
			joyP1Select.setName("joyP1Select");
			joyP1Select.setHorizontalAlignment(SwingConstants.CENTER);
			joyP1Select.setFont(fontFields);
			joyP1Select.setEditable(false);
			joyP1Select.setColumns(10);
			joyP1Select.setBackground(Color.WHITE);
			joyP1Select.setBounds(342, 194, 44, 23);
			joysticksPanel.add(joyP1Select);
			
			JLabel lblSelect_1 = new JLabel("Reset");
			lblSelect_1.setHorizontalAlignment(SwingConstants.CENTER);
			lblSelect_1.setFont(fontLabel);
			lblSelect_1.setBounds(402, 180, 40, 15);
			joysticksPanel.add(lblSelect_1);
			
			joyP1Reset = new JTextFieldNim();
			joyP1Reset.setDisabledTextColor(Color.GRAY);
			joyP1Reset.setEnabled(false);
			joyP1Reset.setName("joyP1Reset");
			joyP1Reset.setHorizontalAlignment(SwingConstants.CENTER);
			joyP1Reset.setFont(fontFields);
			joyP1Reset.setEditable(false);
			joyP1Reset.setColumns(10);
			joyP1Reset.setBackground(Color.WHITE);
			joyP1Reset.setBounds(400, 194, 44, 23);
			joysticksPanel.add(joyP1Reset);
			
			joyP1Button2 = new JTextFieldNim();
			joyP1Button2.setEnabled(false);
			joyP1Button2.setDisabledTextColor(Color.GRAY);
			joyP1Button2.setName("joyP1Button2");
			joyP1Button2.setHorizontalAlignment(SwingConstants.CENTER);
			joyP1Button2.setFont(fontFields);
			joyP1Button2.setEditable(false);
			joyP1Button2.setColumns(10);
			joyP1Button2.setBackground(Color.WHITE);
			joyP1Button2.setBounds(397, 79, 44, 23);
			joysticksPanel.add(joyP1Button2);
			
			JLabel lblReset_1 = new JLabel("Select");
			lblReset_1.setHorizontalAlignment(SwingConstants.CENTER);
			lblReset_1.setFont(fontLabel);
			lblReset_1.setBounds(344, 180, 40, 14);
			joysticksPanel.add(lblReset_1);
			
			JLabel lblFastSpeed_1 = new JLabel("Fire 2");
			lblFastSpeed_1.setHorizontalAlignment(SwingConstants.CENTER);
			lblFastSpeed_1.setFont(fontLabel);
			lblFastSpeed_1.setBounds(399, 65, 40, 15);
			joysticksPanel.add(lblFastSpeed_1);
			
			joyP0Deadzone = new JComboBoxNim();
			joyP0Deadzone.setFont(fontFields);
			joyP0Deadzone.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					joyP0DeadzoneAction();
				}
			});
			joyP0Deadzone.setModel(new DefaultComboBoxModel(JOY_DEADZONE_OPTIONS));
			joyP0Deadzone.setBounds(152, 121, 64, 23);
			joysticksPanel.add(joyP0Deadzone);
			
			joyP0DeadzoneL = new JLabel("Dead zone");
			joyP0DeadzoneL.setHorizontalAlignment(SwingConstants.CENTER);
			joyP0DeadzoneL.setFont(fontLabel);
			joyP0DeadzoneL.setBounds(149, 107, 71, 15);
			joysticksPanel.add(joyP0DeadzoneL);
			
			joyP1Deadzone = new JComboBoxNim();
			joyP1Deadzone.setFont(fontFields);
			joyP1Deadzone.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					joyP1DeadzoneAction();
				}
			});
			joyP1Deadzone.setModel(new DefaultComboBoxModel(JOY_DEADZONE_OPTIONS));
			joyP1Deadzone.setBounds(257, 121, 64, 23);
			joysticksPanel.add(joyP1Deadzone);
			
			joyP1DeadzoneL = new JLabel("Dead zone");
			joyP1DeadzoneL.setHorizontalAlignment(SwingConstants.CENTER);
			joyP1DeadzoneL.setFont(fontLabel);
			joyP1DeadzoneL.setBounds(254, 107, 71, 15);
			joysticksPanel.add(joyP1DeadzoneL);
			
			joyP0FastSpeed = new JTextFieldNim();
			joyP0FastSpeed.setName("joyP0FastSpeed");
			joyP0FastSpeed.setHorizontalAlignment(SwingConstants.CENTER);
			joyP0FastSpeed.setFont(fontFields);
			joyP0FastSpeed.setEnabled(false);
			joyP0FastSpeed.setEditable(false);
			joyP0FastSpeed.setDisabledTextColor(Color.GRAY);
			joyP0FastSpeed.setColumns(10);
			joyP0FastSpeed.setBackground(Color.WHITE);
			joyP0FastSpeed.setBounds(26, 232, 44, 23);
			joysticksPanel.add(joyP0FastSpeed);
			
			JLabel lblFast = new JLabel("Fast");
			lblFast.setHorizontalAlignment(SwingConstants.CENTER);
			lblFast.setFont(fontLabel);
			lblFast.setBounds(28, 218, 40, 15);
			joysticksPanel.add(lblFast);
			
			joyP1FastSpeed = new JTextFieldNim();
			joyP1FastSpeed.setName("joyP1FastSpeed");
			joyP1FastSpeed.setHorizontalAlignment(SwingConstants.CENTER);
			joyP1FastSpeed.setFont(fontFields);
			joyP1FastSpeed.setEnabled(false);
			joyP1FastSpeed.setEditable(false);
			joyP1FastSpeed.setDisabledTextColor(Color.GRAY);
			joyP1FastSpeed.setColumns(10);
			joyP1FastSpeed.setBackground(Color.WHITE);
			joyP1FastSpeed.setBounds(342, 232, 44, 23);
			joysticksPanel.add(joyP1FastSpeed);
			
			JLabel lblFast_1 = new JLabel("Fast");
			lblFast_1.setHorizontalAlignment(SwingConstants.CENTER);
			lblFast_1.setFont(fontLabel);
			lblFast_1.setBounds(344, 218, 40, 15);
			joysticksPanel.add(lblFast_1);
			
			joyP0Sensitivity = new JComboBoxNim();
			joyP0Sensitivity.setFont(fontFields);
			joyP0Sensitivity.setModel(new DefaultComboBoxModel(PADDLE_SENS_OPTIONS));
			joyP0Sensitivity.setBounds(152, 163, 64, 23);
			joysticksPanel.add(joyP0Sensitivity);
			joyP0Sensitivity.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					joyP0SensitivityAction();
				}
			});
			
			JLabel lblPaddleSens = new JLabel("Sensitivity");
			lblPaddleSens.setHorizontalAlignment(SwingConstants.CENTER);
			lblPaddleSens.setFont(fontLabel);
			lblPaddleSens.setBounds(149, 149, 71, 15);
			joysticksPanel.add(lblPaddleSens);
			
			joyP1Sensitivity = new JComboBoxNim();
			joyP1Sensitivity.setFont(fontFields);
			joyP1Sensitivity.setModel(new DefaultComboBoxModel(PADDLE_SENS_OPTIONS));
			joyP1Sensitivity.setBounds(257, 163, 64, 23);
			joysticksPanel.add(joyP1Sensitivity);
			joyP1Sensitivity.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					joyP1SensitivityAction();
				}
			});

			JLabel label_3 = new JLabel("Sensitivity");
			label_3.setHorizontalAlignment(SwingConstants.CENTER);
			label_3.setFont(fontLabel);
			label_3.setBounds(254, 149, 71, 15);
			joysticksPanel.add(label_3);
			
			JPanel cartridgePanel = new JPanel();
			mainTabbedPane.addTab("Cartridge", null, cartridgePanel, null);
			cartridgePanel.setLayout(null);
			cartridgePanel.setPreferredSize(INTERNAL_TAB_SIZE);
			
			JLabel txtpnRomName = new JLabel();
			txtpnRomName.setBounds(15, 6, 153, 21);
			txtpnRomName.setText("Cartridge");
			txtpnRomName.setOpaque(false);
			txtpnRomName.setFont(fontLabelBold);
			cartridgePanel.add(txtpnRomName);
			
			romNameTf = new JTextFieldNim();
			romNameTf.setFont(fontLabel);
			romNameTf.setEditable(false);
			romNameTf.setBounds(14, 24, 445, 27);
			cartridgePanel.add(romNameTf);
			romNameTf.setColumns(10);
			
			JLabel txtpnRomFormat = new JLabel();
			txtpnRomFormat.setText("Cartridge Format");
			txtpnRomFormat.setOpaque(false);
			txtpnRomFormat.setFont(fontLabelBold);
			txtpnRomFormat.setBounds(15, 53, 447, 21);
			cartridgePanel.add(txtpnRomFormat);
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(14, 73, 285, 177);
			cartridgePanel.add(scrollPane);
			
			romFormatLb = new JList();
			romFormatLb.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					romFormatLbAction();
				}
			});
			romFormatLb.setFont(fontLabel);
			scrollPane.setViewportView(romFormatLb);
			
			JTextPane txtpnYouCanPlace = new JTextPane();
			txtpnYouCanPlace.setText("You can give Format Hints like (E0) or (3F) in ROMs filenames");
			txtpnYouCanPlace.setOpaque(false);
			txtpnYouCanPlace.setBackground(noBackground);
			txtpnYouCanPlace.setFont(fontLabel);
			txtpnYouCanPlace.setEditable(false);
			txtpnYouCanPlace.setBounds(306, 77, 165, 78);
			cartridgePanel.add(txtpnYouCanPlace);
			
			JTextPane txtpnAltbCycle = new JTextPane();
			txtpnAltbCycle.setFocusable(false);
			txtpnAltbCycle.setText("ALT + B: Cycle through compatible Formats\r\n");
			txtpnAltbCycle.setOpaque(false);
			txtpnAltbCycle.setBackground(noBackground);
			txtpnAltbCycle.setFont(fontLabel);
			txtpnAltbCycle.setEditable(false);
			txtpnAltbCycle.setBounds(306, 201, 165, 61);
			cartridgePanel.add(txtpnAltbCycle);
			
			JLabel lbLibInfo = new JLabel("(Based on Rom Hunter's collection)");
			lbLibInfo.setHorizontalAlignment(SwingConstants.TRAILING);
			lbLibInfo.setFont(fontLabel);
			lbLibInfo.setBounds(170, 6, 285, 20);
			cartridgePanel.add(lbLibInfo);
			txtpnAltbCycle.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					cycleROMFormatAction.actionPerformed(null);
				}
			});
			
			JPanel helpPanel = new JPanel();
			mainTabbedPane.addTab("Help", null, helpPanel, null);
			helpPanel.setLayout(null);
			helpPanel.setPreferredSize(INTERNAL_TAB_SIZE);
			
			JTextPane txtpnAltJ = new JTextPane();
			txtpnAltJ.setOpaque(false);
			txtpnAltJ.setBackground(noBackground);
			txtpnAltJ.setEditable(false);
			txtpnAltJ.setFont(fontLabel);
			txtpnAltJ.setBounds(12, 39, 121, 226);
			helpPanel.add(txtpnAltJ);
			txtpnAltJ.setText("CTR + 1-0:\r\nALT + 1-0:\r\n\r\nALT + ENT:\r\nALT + V:\r\nALT + R:\r\nALT + T:\r\nALT + G:\r\nALT + D:\r\nALT + C:\r\nALT + P:\r\nALT + F:\r\nTAB:");
			
			JTextPane txtpnFullscreenNtsc = new JTextPane();
			txtpnFullscreenNtsc.setOpaque(false);
			txtpnFullscreenNtsc.setBackground(noBackground);
			txtpnFullscreenNtsc.setEditable(false);
			txtpnFullscreenNtsc.setText("Save State\r\nLoad State\r\n\r\nFullscreen\r\nNTSC / PAL\r\nCRT Modes\r\nCRT Filter\r\nShow info\r\nDebug Modes\r\nCollisions\r\nPause\r\nNext Frame\r\nFast Speed");
			txtpnFullscreenNtsc.setFont(fontLabel);
			txtpnFullscreenNtsc.setBounds(95, 39, 145, 226);
			helpPanel.add(txtpnFullscreenNtsc);
			
			JTextPane txtpnAltF = new JTextPane();
			txtpnAltF.setOpaque(false);
			txtpnAltF.setBackground(noBackground);
			txtpnAltF.setEditable(false);
			txtpnAltF.setText("ALT + F1:\r\n\r\nALT + F5:\r\nALT + F6:\r\nF7:\r\n\r\nDrag/Drop or Copy/Paste of files and URLs\r\n\r\nSHIFT + Arrows:\r\nSHIFT-ALT + Arrows:\r\nCTR-SHIFT + Arrows:\r\nCTR-ALT + Arrows:\r\nBACKSPACE:");
			txtpnAltF.setFont(fontLabel);
			txtpnAltF.setBounds(201, 39, 309, 226);
			helpPanel.add(txtpnAltF);
			
			JTextPane txtpnFryConsoleLoad = new JTextPane();
			txtpnFryConsoleLoad.setOpaque(false);
			txtpnFryConsoleLoad.setBackground(noBackground);
			txtpnFryConsoleLoad.setEditable(false);
			txtpnFryConsoleLoad.setText("Fry Console\r\n\r\nLoad Cartridge\r\nwith no Power Cycle\r\nRemove Cartridge");
			txtpnFryConsoleLoad.setFont(fontLabel);
			txtpnFryConsoleLoad.setBounds(277, 39, 194, 117);
			helpPanel.add(txtpnFryConsoleLoad);
			
			JTextPane txtpnDisplayOriginDisplay = new JTextPane();
			txtpnDisplayOriginDisplay.setOpaque(false);
			txtpnDisplayOriginDisplay.setBackground(noBackground);
			txtpnDisplayOriginDisplay.setEditable(false);
			txtpnDisplayOriginDisplay.setText("\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\nScreen Size\r\nScreen Scale\r\nViewport Size\r\nViewport Origin\r\nScreen Defaults");
			txtpnDisplayOriginDisplay.setFont(fontLabel);
			txtpnDisplayOriginDisplay.setBounds(340, 39, 139, 226);
			helpPanel.add(txtpnDisplayOriginDisplay);
			
			JLabel lblHotKeys = new JLabel();
			lblHotKeys.setHorizontalAlignment(SwingConstants.CENTER);
			lblHotKeys.setText("Hot Keys");
			lblHotKeys.setOpaque(false);
			lblHotKeys.setFont(fontLabelBold);
			lblHotKeys.setBounds(11, 10, 446, 21);
			helpPanel.add(lblHotKeys);
			{
				JPanel aboutPanel = new JPanel();
				aboutPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				aboutPanel.setBackground(UIManager.getColor("Panel.background"));
				mainTabbedPane.addTab("About", null, aboutPanel, null);
				aboutPanel.setLayout(null);
				aboutPanel.setPreferredSize(INTERNAL_TAB_SIZE);

				{
					JLabel lblNewButton = new JLabel("");
					lblNewButton.setBounds(29, 27, 170, 166);
					aboutPanel.add(lblNewButton);
					lblNewButton.setIcon(new ImageIcon(SettingsDialog.class.getResource("/org/javatari/pc/room/settings/images/LogoAbout.png")));
					lblNewButton.setHorizontalAlignment(SwingConstants.CENTER);
					lblNewButton.setVerticalAlignment(SwingConstants.CENTER);
					lblNewButton.setBorder(new DeepBorder(6, new Insets(3, 3, 3, 3)));
				}
				
				JLabel lblVerion = new JLabel(Parameters.VERSION);
				lblVerion.setHorizontalAlignment(SwingConstants.CENTER);
				lblVerion.setFont(fontLabelMedium);
				lblVerion.setBounds(67, 202, 95, 14);
				aboutPanel.add(lblVerion);
				
				JLabel lblCreate = new JLabel("Paulo Augusto Peccin");
				lblCreate.setHorizontalAlignment(SwingConstants.CENTER);
				lblCreate.setFont(fontLabelLarge);
				lblCreate.setBounds(241, 67, 173, 19);
				aboutPanel.add(lblCreate);
				
				JLabel lblCreated = new JLabel("created by");
				lblCreated.setHorizontalAlignment(SwingConstants.CENTER);
				lblCreated.setFont(fontLabelMedium);
				lblCreated.setBounds(259, 44, 137, 21);
				aboutPanel.add(lblCreated);
				{
					JLabel lblOfficialHomepage = new JLabel("official homepage:");
					lblOfficialHomepage.setHorizontalAlignment(SwingConstants.CENTER);
					lblOfficialHomepage.setFont(fontLabelMedium);
					lblOfficialHomepage.setBounds(259, 124, 137, 21);
					aboutPanel.add(lblOfficialHomepage);
				}
				{
					JButton lblHttpjavatariorg = new JButton("http://javatari.org");
					lblHttpjavatariorg.setFocusPainted(false);
					lblHttpjavatariorg.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							officialWebPageAction();
						}
					});
					lblHttpjavatariorg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					lblHttpjavatariorg.setBorder(null);
					lblHttpjavatariorg.setContentAreaFilled(false);
					lblHttpjavatariorg.setBorderPainted(false);
					lblHttpjavatariorg.setHorizontalAlignment(SwingConstants.CENTER);
					lblHttpjavatariorg.setForeground(new Color(40,100,230));
					lblHttpjavatariorg.setFont(fontLabelLarge);
					lblHttpjavatariorg.setBounds(250, 146, 154, 19);
					aboutPanel.add(lblHttpjavatariorg);
				}
				{
					JButton lblppeccin = new JButton("@ppeccin");
					lblppeccin.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							twitterPageAction();
						}
					});
					lblppeccin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					lblppeccin.setContentAreaFilled(false);
					lblppeccin.setForeground(new Color(40,100,230));
					lblppeccin.setBorderPainted(false);
					lblppeccin.setBorder(null);
					lblppeccin.setHorizontalAlignment(SwingConstants.CENTER);
					lblppeccin.setFont(fontLabelMedium);
					lblppeccin.setBounds(259, 87, 137, 19);
					aboutPanel.add(lblppeccin);
				}
				
				vmInfo = new JLabel("");
				vmInfo.setHorizontalAlignment(SwingConstants.CENTER);
				vmInfo.setVerticalAlignment(SwingConstants.BOTTOM);
				vmInfo.setFont(fontFields);
				vmInfo.setBounds(11, 239, 450, 20);
				aboutPanel.add(vmInfo);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new BorderLayout(0, 0));
			{
				
				JPanel panel = new JPanel();
				FlowLayout flowLayout = (FlowLayout) panel.getLayout();
				flowLayout.setAlignment(FlowLayout.LEFT);
				buttonPane.add(panel, BorderLayout.WEST);
				
				defaultsB = new JButton("Defaults");
				defaultsB.setFont(fontLabel);
				defaultsB.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						defaultsAction();
					}
				});
				panel.add(defaultsB);
				
				JPanel panel_1 = new JPanel();
				buttonPane.add(panel_1, BorderLayout.EAST);
				{
					closeB = new JButton("Close");
					closeB.setFont(fontLabel);
					panel_1.add(closeB);
					closeB.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							closeAction();
						}
					});
					closeB.setActionCommand("Cancel");
				}
				// getRootPane().setDefaultButton(okButton);
			}
		}
		
		keyboardEdittingL = new JLabel("");
		keyboardEdittingL.setHorizontalAlignment(SwingConstants.CENTER);
		keyboardEdittingL.setFont(fontLabel);
		keyboardEdittingL.setBounds(192, 53, 86, 15);
		keyboardPanel.add(keyboardEdittingL);
		
		joyP0XAxis = new JComboBoxNim();
		joyP0XAxis.setFont(fontFields);
		joyP0XAxis.setModel(new DefaultComboBoxModel(JOY_AXIS_OPTIONS));
		joyP0XAxis.setBounds(19, 130, 44, 23);
		joysticksPanel.add(joyP0XAxis);
		joyP0XAxis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				joyP0XAxisAction();
			}
		});

		
		joyP0XAxisL = new JLabel("X Axis");
		joyP0XAxisL.setHorizontalAlignment(SwingConstants.CENTER);
		joyP0XAxisL.setFont(fontLabel);
		joyP0XAxisL.setBounds(12, 116, 58, 15);
		joysticksPanel.add(joyP0XAxisL);
		
		joyP0YAxis = new JComboBoxNim();
		joyP0YAxis.setFont(fontFields);
		joyP0YAxis.setModel(new DefaultComboBoxModel(JOY_AXIS_OPTIONS));
		joyP0YAxis.setBounds(85, 67, 44, 23);
		joysticksPanel.add(joyP0YAxis);
		joyP0YAxis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				joyP0YAxisAction();
			}
		});

		JLabel lblVAxis = new JLabel("Y Axis");
		lblVAxis.setHorizontalAlignment(SwingConstants.CENTER);
		lblVAxis.setFont(fontLabel);
		lblVAxis.setBounds(85, 53, 45, 15);
		joysticksPanel.add(lblVAxis);
		
		joyP1XAxis = new JComboBoxNim();
		joyP1XAxis.setFont(fontFields);
		joyP1XAxis.setModel(new DefaultComboBoxModel(JOY_AXIS_OPTIONS));
		joyP1XAxis.setBounds(407, 130, 44, 23);
		joysticksPanel.add(joyP1XAxis);
		joyP1XAxis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				joyP1XAxisAction();
			}
		});
		
		joyP1XAxisL = new JLabel("X Axis");
		joyP1XAxisL.setHorizontalAlignment(SwingConstants.CENTER);
		joyP1XAxisL.setFont(fontLabel);
		joyP1XAxisL.setBounds(400, 116, 58, 15);
		joysticksPanel.add(joyP1XAxisL);
		
		joyP1YAxis = new JComboBoxNim();
		joyP1YAxis.setFont(fontFields);
		joyP1YAxis.setModel(new DefaultComboBoxModel(JOY_AXIS_OPTIONS));
		joyP1YAxis.setBounds(342, 67, 44, 23);
		joysticksPanel.add(joyP1YAxis);
		joyP1YAxis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				joyP1YAxisAction();
			}
		});

		JLabel lblP1YAxis = new JLabel("Y Axis");
		lblP1YAxis.setHorizontalAlignment(SwingConstants.CENTER);
		lblP1YAxis.setFont(fontLabel);
		lblP1YAxis.setBounds(342, 53, 45, 15);
		joysticksPanel.add(lblP1YAxis);
		
		joysticksEdittingL = new JLabel("");
		joysticksEdittingL.setHorizontalAlignment(SwingConstants.CENTER);
		joysticksEdittingL.setFont(fontLabel);
		joysticksEdittingL.setBounds(186, 52, 100, 15);
		joysticksPanel.add(joysticksEdittingL);
		
		joyP0Device = new JComboBoxNim();
		joyP0Device.setFont(fontFields);
		joyP0Device.setBounds(13, 26, 213, 23);
		joysticksPanel.add(joyP0Device);
		joyP0Device.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				joyP0P1DeviceAction();
			}
		});

		joyP1Device = new JComboBoxNim();
		joyP1Device.setFont(fontFields);
		joyP1Device.setBounds(245, 26, 213, 23);
		joysticksPanel.add(joyP1Device);
		joyP1Device.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				joyP0P1DeviceAction();
			}
		});

		((JComponent)getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(AWTConsoleControls.KEY_TOGGLE_P1_MODE, KeyEvent.ALT_DOWN_MASK), "ToggleP1Mode");
		((JComponent)getContentPane()).getActionMap().put("ToggleP1Mode", toggleP1ModeAction);
		((JComponent)getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(AWTConsoleControls.KEY_TOGGLE_PADDLE, KeyEvent.ALT_DOWN_MASK), "TogglePaddles");
		((JComponent)getContentPane()).getActionMap().put("TogglePaddles", togglePaddlesAction);
		((JComponent)getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(AWTConsoleControls.KEY_TOGGLE_JOYSTICK, KeyEvent.ALT_DOWN_MASK), "ToggleJoystick");
		((JComponent)getContentPane()).getActionMap().put("ToggleJoystick", toggleJoystickAction);
		((JComponent)getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(AWTConsoleControls.KEY_CARTRIDGE_FORMAT, KeyEvent.ALT_DOWN_MASK), "CycleROMFormat");
		((JComponent)getContentPane()).getActionMap().put("CycleROMFormat", cycleROMFormatAction);

		buildFinished = true;
	}


	private final Room room;

	private boolean buildFinished = false;
	
	private final JPanel contentPanel = new JPanel();
	private JTabbedPane mainTabbedPane;

	private JPanel keyboardPanel, joysticksPanel;

	private JTextField
		keyP0Up, keyP0Down, keyP0Left, keyP0Right, keyP0Button, keyP0Button2, 
		keyP1Up, keyP1Down, keyP1Left, keyP1Right, keyP1Button, keyP1Button2;
	private List<JTextField> keyboardFieldsList;
	private Object keyboardEditField;
	private JLabel keyboardPlayer1Lb, keyP0UpL, keyP0DownL, keyboardPlayer2Lb, keyP1UpL,keyP1DownL, keyboardMessageL, keyboardEdittingL, keyboard0Icon, keyboard1Icon;

	private JTextField 
		joyP0Button, joyP0Button2, joyP0Reset, joyP0Select, joyP0Pause, joyP0FastSpeed,
		joyP1Button, joyP1Button2, joyP1Reset, joyP1Select, joyP1Pause, joyP1FastSpeed;
	private JComboBox 
		joyP0Device, joyP0Deadzone, joyP0Sensitivity, joyP0XAxis, joyP0YAxis, 
		joyP1Device, joyP1Deadzone, joyP1Sensitivity, joyP1XAxis, joyP1YAxis;
	private List<JTextField> joystick0FieldsList, joystick1FieldsList, joysticksFieldsList;
	private Object joystickEditField;
	private JLabel joyP0XAxisL, joyP0DeadzoneL, joyP1XAxisL, joyP1DeadzoneL, joysticksPlayer1Lb, joysticksPlayer2Lb, joysticksMessageL, joysticksEdittingL;
	private JLabel joystick0Icon, joystick1Icon;
	private ImageIcon joystickIcon, joystickIconGrayed, paddleIcon, paddleIconGrayed;

	private JLabel multiplayerModeL, standaloneConsoleL, serverConsoleL, clientConsoleL, networkL;;
	private JTextField serverPortTf, clientServerAddressTf;
	private JButton serverStartB, clientConnectB;

	private JTextField romNameTf;
	private JList romFormatLb;

	private JButton defaultsB, closeB;
	
	private boolean refreshInProgress = false;

	private static final int CONTROL_UNSET = Parameters.CONTROL_UNSET;
	private static final Color UNMAPPED = new Color(255, 255, 150);
	private static final Color EDDITING = new Color(130, 150, 255);
	private static final String PADDLE_CENTER_ZERO = "<0>";
	private static final String PADDLE_SENS_DIGITAL = "Digital";
	private static final String[] JOY_AXIS_OPTIONS = new String[] { "X", "Y", "Z", "R", "U", "V", "-X", "-Y", "-Z", "-R", "-U", "-V" };
	private static final int[] JOY_AXIS_NUMBERS = new int[] { 0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5 };
	private static final int[] JOY_AXIS_SIGNALS = new int[] { 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1 };
	private static final String[] JOY_DEADZONE_OPTIONS = new String[] { "0%", "5%", "10%", "15%", "20%", "25%", "30%", "35%", "40%", "45%", "50%", "60%", "70%"};
	private static final String[] PADDLE_CENTER_OPTIONS = new String[] { "-60", "-50", "-40", "-30", "-20", "-10", PADDLE_CENTER_ZERO, "+10", "+20", "+30", "+40", "+50", "+60"};
	private static final String[] PADDLE_SENS_OPTIONS = new String[] { PADDLE_SENS_DIGITAL, "30%", "35%", "40%", "45%", "50%", "55%", "60%", "65%", "70%", "75%", "80%", "90%", "100%"};
	
	private static final Dimension INTERNAL_TAB_SIZE = new Dimension(472, 258 + (Environment.NIMBUS_LAF ? 0 : 5));
	
	private static final long serialVersionUID = 1L;
	private JLabel vmInfo;

}

class GlassPane extends JPanel {
	public void paintComponent(Graphics g) {
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(0, 0, getWidth(), getHeight());
	}
	private static final long serialVersionUID = 1L;
}

class JTextFieldNim extends JTextField {
	@Override
	public void setBounds(int x, int y, int width, int height) {
		if (Environment.NIMBUS_LAF) super.setBounds(x, y, width, height);
		else super.setBounds(x + 2, y + 2, width - 4, height - 3);
	}
	private static final long serialVersionUID = 1L;
}

class JComboBoxNim extends JComboBox {
	@Override
	public void setBounds(int x, int y, int width, int height) {
		if (Environment.NIMBUS_LAF) super.setBounds(x, y, width, height);
		else super.setBounds(x + 2, y + 2, width - 4, height - 3);
	}
	private static final long serialVersionUID = 1L;
}
