// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.utils.slickframe;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;

public class SlickFrame extends JFrame {

	public SlickFrame() throws HeadlessException {
		this(true);
	}

	public SlickFrame(boolean resizable) throws HeadlessException {
		super();
		init();
		this.resizable = resizable;
	}

	public void minimunResize(Dimension minSize) {
		this.minimumSize = minSize;
	}

	public MousePressAndMotionListener detachMouseListener() {
		removeMouseListener(mouseListener);
		removeMouseMotionListener(mouseListener);
		return mouseListener;
	}
	
	protected void init() {
		setUndecorated(true);
		setResizable(false);
		mouseListener = buildMouseListener();
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
	}

	protected void movingTo(int x, int y) {
		setLocation(x, y);
	}

	protected void resizingTo(int width, int height) {
		setSize(width, height);
	}

	protected void finishedMoving() {
	}

	protected void finishedResizing() {
	}

	private MousePressAndMotionListener buildMouseListener() {
		return new MousePressAndMotionAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() != 1) return;
				clickPosition = e.getLocationOnScreen();
				if (!resizable) {
					startingLocation = getLocation();
					return;
				}
				Rectangle resizeArea = new Rectangle(getWidth() - RESIZE_CORNER_SIZE, getHeight() - RESIZE_CORNER_SIZE, getWidth(), getHeight());
				if (resizeArea.contains(e.getPoint()))
					startingSize = getSize();
				else
					startingLocation = getLocation();
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() != 1) return;
				clickPosition = null;
				if (startingLocation != null)
					finishedMoving();
				else 
					if (startingSize != null)
						finishedResizing();
				startingLocation = null;
				startingSize = null;
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				if (clickPosition == null) return;
				Point p = e.getLocationOnScreen();
				if (startingLocation != null)
					movingTo(startingLocation.x + p.x - clickPosition.x,	startingLocation.y + p.y - clickPosition.y);
				else
					resizingTo(
						Math.max(startingSize.width + p.x - clickPosition.x, minimumSize.width), 
						Math.max(startingSize.height + p.y - clickPosition.y, minimumSize.height
					));
			}
		};
	}
	
	
	private MousePressAndMotionListener mouseListener;

	private final boolean resizable;
	private Point clickPosition = null;
	private Point startingLocation = null;
	private Dimension startingSize = null;
	private Dimension minimumSize = new Dimension(80, 40);
	
	private static final int RESIZE_CORNER_SIZE = 18;

	public static final long serialVersionUID = 1L;

}
