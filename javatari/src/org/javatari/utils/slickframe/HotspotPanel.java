// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.utils.slickframe;

import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

public class HotspotPanel extends JPanel {

	public HotspotPanel() throws HeadlessException {
		init();
	}

	public void setForwardListener(MousePressAndMotionListener forwardListener) {
		this.forwardListener = forwardListener;
	}
	
	public HotspotAction addHotspot(Rectangle area, Runnable activationAction) {
		return addHotspot(area, (String)null, activationAction);
	}
	public HotspotAction addHotspot(Rectangle area, String tooltip, Runnable activationAction) {
		HotspotAction h = new HotspotAction(area, activationAction);
		h.setTooltip(tooltip);
		return addHotspot(h);
	}

	public HotspotAction addHotspot(Rectangle area, Runnable activationAction, Runnable deactivationAction) {
		return addHotspot(area, (String) null, activationAction, deactivationAction);
	}
	public HotspotAction addHotspot(Rectangle area, String tooltip, Runnable activationAction, Runnable deactivationAction) {
		HotspotAction h = new HotspotAction(area, activationAction, deactivationAction);
		h.setTooltip(tooltip);
		return addHotspot(h);
	}

	public HotspotAction addHotspot(HotspotAction h) {
		if (!hotspots.contains(h)) {
			hotspots.add(h);
			updateHotspotEffectiveArea(h);
			if (h.tooltip != null) {
				hotspotsWithTooltip.add(h);
				ToolTipManager.sharedInstance().registerComponent(this);
			}
		}
		return h;
	}

	public void removeHotspot(HotspotAction hotspot) {
		hotspots.remove(hotspot);
		hotspotsWithTooltip.remove(hotspot);
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		if (!hotspotsWithTooltip.isEmpty()) {
			// Check for Hotspots
			updateHotspotsEffectiveAreas();
			Point point = event.getPoint();
			for (HotspotAction hotspot : hotspotsWithTooltip)
				if (hotspot.effectiveArea.contains(point)) return hotspot.tooltip;
		}
		return null;
	}
	
	public MousePressAndMotionListener detachMouseListener() {
		removeMouseListener(mouseListener);
		removeMouseMotionListener(mouseListener);
		return mouseListener;
	}
	
	private void init() {
		mouseListener = buildMouseListener();
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (hotspotsEffectiveAreasValid && e.getComponent() == HotspotPanel.this)
					hotspotsEffectiveAreasValid = false;
			}
		});
	}

	private MousePressAndMotionListener buildMouseListener() {
		return new MousePressAndMotionListener() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == 1) {
					// Check for Hotspots
					updateHotspotsEffectiveAreas();
					Point point = e.getPoint();
					for (HotspotAction hotspot : hotspots) {
						if (hotspot.effectiveArea.contains(point)) {
							activeHotspot = hotspot;
							hotspot.activationAction.run();
							return;
						}
					}
					activeHotspot = null;
				}
				if (forwardListener != null) forwardListener.mousePressed(e);
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
				if (forwardListener != null) forwardListener.mouseReleased(e);
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				if (activeHotspot == null)
					if (forwardListener != null) forwardListener.mouseDragged(e);
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (forwardListener != null) forwardListener.mouseClicked(e);
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				if (forwardListener != null) forwardListener.mouseEntered(e);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				if (forwardListener != null) forwardListener.mouseExited(e);
			}
			@Override
			public void mouseMoved(MouseEvent e) {
				if (forwardListener != null) forwardListener.mouseMoved(e);
			}
		};
	}
	
	// Used for debugging
	public void paintHotspots(Graphics g) {
//		g.setColor(Color.YELLOW);
//		for (HotspotAction hotspot : hotspots) {
//			Rectangle rect = hotspot.effectiveArea;
//			g.drawRect(rect.x, rect.y, rect.width, rect.height);	
//		}
	}
	
	private void updateHotspotsEffectiveAreas() {
		if (hotspotsEffectiveAreasValid) return;
		for (HotspotAction hotspot : hotspots)
			updateHotspotEffectiveArea(hotspot); 
		hotspotsEffectiveAreasValid = true;
	}

	private void updateHotspotEffectiveArea(HotspotAction hotspot) {
		Rectangle area = hotspot.area;
		Rectangle effArea = hotspot.effectiveArea;
		effArea.x = area.x == CENTER_HOTSPOT ? (getWidth() - area.width) / 2 : area.x < 0 ? getWidth() + area.x : area.x;
		effArea.y = area.y == CENTER_HOTSPOT ? (getHeight() - area.height) / 2 : area.y < 0 ? getHeight() + area.y : area.y;
	}


	private MousePressAndMotionListener forwardListener;
	private MousePressAndMotionListener mouseListener;
	private boolean hotspotsEffectiveAreasValid = false;
	
	private List<HotspotAction> hotspots = new ArrayList<HotspotAction>();
	private List<HotspotAction> hotspotsWithTooltip = new ArrayList<HotspotAction>();
	private HotspotAction activeHotspot;
	
	public static final int CENTER_HOTSPOT = -10000;

	public static final long serialVersionUID = 1L;

	public class HotspotAction {
		public HotspotAction(Rectangle area, Runnable activationAction) {
			this(area, activationAction, null);
		}
		public HotspotAction(Rectangle area, Runnable activationAction, Runnable deactivationAction) {
			this.area = area;
			this.effectiveArea = new Rectangle(area);
			this.activationAction = activationAction;
			this.deactivationAction = deactivationAction;
		}
		public void setTooltip(String tooltip) {
			this.tooltip = tooltip;
		}
		public Rectangle area, effectiveArea;
		public Runnable activationAction;
		public Runnable deactivationAction;
		public String tooltip;
	}

}
