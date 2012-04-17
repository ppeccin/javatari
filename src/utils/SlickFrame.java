// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

	public void addHotspot(Rectangle area, Runnable activationAction) {
		hotspots.add(new HotspotAction(area, activationAction));
	}
	
	public void addHotspot(Rectangle area, Runnable activationAction, Runnable deactivationAction) {
		hotspots.add(new HotspotAction(area, activationAction, deactivationAction));
	}

	protected void init() {
		setUndecorated(true);
		setResizable(false);
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getButton() != 1) return;
				// Check for Hotspots
				Point point = e.getPoint();
				for (Iterator<HotspotAction> it = hotspots.iterator(); it.hasNext();) {
					HotspotAction hotspot = (HotspotAction) it.next();
					Rectangle efectiveArea = effectiveArea(hotspot.area);
					if (efectiveArea.contains(point)) {
						activeHotspot = hotspot;
						hotspot.activationAction.run();
						return;
					}
				}
				// No Hotspots clicked, proceed with move or resize
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
				if (activeHotspot != null && activeHotspot.deactivationAction != null)
					activeHotspot.deactivationAction.run();
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
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
		});
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

	// Used for debugging
	protected void paintHotspots(Graphics g) {
		g.setColor(Color.YELLOW);
		for (Iterator<HotspotAction> it = hotspots.iterator(); it.hasNext();) {
			HotspotAction hotspot = (HotspotAction) it.next();
			Rectangle rect = effectiveArea(hotspot.area);
			g.drawRect(rect.x, rect.y, rect.width, rect.height);	
		}
	}
	
	private Rectangle effectiveArea(Rectangle area) {
		Insets ins = getInsets();
		return new Rectangle(
			area.x == CENTER_HOTSPOT ? (getWidth() - area.width) / 2 : area.x < 0 ? getWidth() - ins.right + area.x : area.x + ins.left, 
			area.y == CENTER_HOTSPOT ? (getHeight() - area.height) / 2 : area.y < 0 ? getHeight() - ins.bottom + area.y : area.y + ins.top, 
			area.width, area.height 
		);
	}
	
	private final boolean resizable;
	
	private Point clickPosition = null;
	private Point startingLocation = null;
	private Dimension startingSize = null;
	
	private List<HotspotAction> hotspots = new ArrayList<HotspotAction>();
	private HotspotAction activeHotspot;
	
	private Dimension minimumSize = new Dimension(80,40);
	
	public static final int CENTER_HOTSPOT = -10000;

	private static final int RESIZE_CORNER_SIZE = 18;

	private static final long serialVersionUID = 1L;

}

class HotspotAction {
	public HotspotAction(Rectangle area, Runnable activationAction) {
		this(area, activationAction, null);
	}
	public HotspotAction(Rectangle area, Runnable activationAction, Runnable deactivationAction) {
		this.area = area;
		this.activationAction = activationAction;
		this.deactivationAction = deactivationAction;
	}
	public Rectangle area;
	public Runnable activationAction;
	public Runnable deactivationAction;
}