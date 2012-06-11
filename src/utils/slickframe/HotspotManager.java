// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package utils.slickframe;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HotspotManager {

	public HotspotManager(Component masterComponent) throws HeadlessException {
		this(masterComponent, new MousePressAndMotionAdapter() {});
	}

	public HotspotManager(Component masterComponent, MousePressAndMotionListener forwardListener) throws HeadlessException {
		this.masterComponent = masterComponent;
		this.forwardListener = forwardListener;
		init();
	}

	public void addHotspot(Rectangle area, Runnable activationAction) {
		hotspots.add(new HotspotAction(area, activationAction));
	}
	
	public void addHotspot(Rectangle area, Runnable activationAction, Runnable deactivationAction) {
		hotspots.add(new HotspotAction(area, activationAction, deactivationAction));
	}

	public MousePressAndMotionListener detachMouseListener() {
		masterComponent.removeMouseListener(mouseListener);
		masterComponent.removeMouseMotionListener(mouseListener);
		return mouseListener;
	}
	
	private void init() {
		mouseListener = buildMouseListener();
		masterComponent.addMouseListener(mouseListener);
		masterComponent.addMouseMotionListener(mouseListener);
	}

	private MousePressAndMotionListener buildMouseListener() {
		return new MousePressAndMotionListener() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == 1) {
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
					activeHotspot = null;
				}
				forwardListener.mousePressed(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == 1) {
					if (activeHotspot != null) {
						if (activeHotspot.deactivationAction != null)
							activeHotspot.deactivationAction.run();
						activeHotspot = null;
						return;
					}
				}
				forwardListener.mouseReleased(e);
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				if (activeHotspot == null)
					forwardListener.mouseDragged(e);
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				forwardListener.mouseClicked(e);
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				forwardListener.mouseEntered(e);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				forwardListener.mouseExited(e);
			}
			@Override
			public void mouseMoved(MouseEvent e) {
				forwardListener.mouseMoved(e);
			}
		};
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
		return new Rectangle(
			area.x == CENTER_HOTSPOT ? (masterComponent.getWidth() - area.width) / 2 : area.x < 0 ? masterComponent.getWidth() + area.x : area.x, 
			area.y == CENTER_HOTSPOT ? (masterComponent.getHeight() - area.height) / 2 : area.y < 0 ? masterComponent.getHeight() + area.y : area.y, 
			area.width, area.height 
		);
	}
	
	private final Component masterComponent;
	private final MousePressAndMotionListener forwardListener;

	private MousePressAndMotionListener mouseListener;
	
	private List<HotspotAction> hotspots = new ArrayList<HotspotAction>();
	private HotspotAction activeHotspot;
	
	public static final int CENTER_HOTSPOT = -10000;

	public static final long serialVersionUID = 1L;

	private class HotspotAction {
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

}
