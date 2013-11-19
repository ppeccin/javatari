// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.screen;

import java.awt.BufferCapabilities;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.image.BufferStrategy;
import java.lang.reflect.Constructor;
import java.util.Arrays;

import javax.swing.JFrame;

import org.javatari.utils.SwingHelper;

public final class DesktopScreenFullWindow extends JFrame implements MonitorDisplay {

	public DesktopScreenFullWindow(DesktopScreenWindow window) {
		// Creates our Window at the correct GraphicsConfig from the owner's location 
		super(SwingHelper.getGraphicsConfigurationForCurrentLocation(window));
		this.window = window;
		init();
	}

	private void init() {
		setLayout(null);
		setUndecorated(true);
		setBackground(Color.BLACK);
		setIgnoreRepaint(true);
		getContentPane().setBackground(Color.BLACK);
		getContentPane().setIgnoreRepaint(true);
		setIconImages(Arrays.asList(new Image[] { window.icon64, window.icon32, window.favicon }));
		setTitle(DesktopScreenWindow.BASE_TITLE + " - Fullwindow");
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			canvasSetRenderingMode();
			SwingHelper.edtInvokeLater(new Runnable() {  @Override public void run() {
				repaint();
			}});
		}
	}

	@Override
	public void displaySize(Dimension size) {
		canvasOriginX = (getWidth() - size.width) / 2;
		canvasOriginY = (getHeight() - size.height) / 2;
		canvasEffectiveSize = size;
		repaint();
		if (bufferStrategy != null && bufferStrategy.getCapabilities().getFlipContents() != FlipContents.BACKGROUND) {
			clearBackgoundFrames = Monitor.MULTI_BUFFERING + 2;		// Extra 2 frames more than needed
		}
	}

	@Override
	public void displayMinimumSize(Dimension minSize) {
		// Full window is not manually resized
	}

	@Override
	public void displayCenter() {
		// Full window is always centered
	}

	@Override
	public Dimension displayEffectiveSize() {
		return canvasEffectiveSize;
	}

	@Override
	public Graphics2D displayGraphics() {
		Graphics2D canvasGraphics = (Graphics2D) (bufferStrategy != null ? bufferStrategy.getDrawGraphics() : getGraphics());
		// Clears the background when needed, but just for a few frames
		if (clearBackgoundFrames-- > 0) {
			canvasGraphics.setColor(Color.BLACK);
			canvasGraphics.clearRect(0, 0, getWidth(), getHeight());
		}
		canvasGraphics.translate(canvasOriginX, canvasOriginY);
		return canvasGraphics;
	}

	@Override
	public void displayFinishFrame(Graphics2D graphics) {
		graphics.dispose();
		if (bufferStrategy != null) bufferStrategy.show();
	}

	@Override
	public void displayClear() {
		Graphics2D canvasGraphics = displayGraphics();
		canvasGraphics.setColor(Color.BLACK);
		canvasGraphics.clearRect(0, 0, getWidth(), getHeight());
		displayFinishFrame(canvasGraphics);
	}

	@Override
	public Container displayContainer() {
		return this;
	}

	@Override
	// Gets the largest possible size at the default aspect
	public float displayDefaultOpenningScaleX(int displayWidth, int displayHeight) {
		int winW = getWidth();
		int winH = getHeight();
		float scaleX = winW / displayWidth;
		scaleX -= (scaleX % Monitor.DEFAULT_SCALE_ASPECT_X);		// Round multiple of the default X scale
		float h = scaleX / Monitor.DEFAULT_SCALE_ASPECT_X * displayHeight;
		while (h > winH + 35) {										// 35 is a little tolerance
			scaleX -= Monitor.DEFAULT_SCALE_ASPECT_X;				// Decrease one full default X scale
			h = scaleX / Monitor.DEFAULT_SCALE_ASPECT_X * displayHeight;
		}
		return scaleX;
	}

	@Override
	public void displayRequestFocus() {
		requestFocus();
	}

	@Override
	public void displayLeaveFullscreen() {
		window.displayLeaveFullscreen();
	}

	public void canvasSetRenderingMode() {
		if (Monitor.MULTI_BUFFERING <= 0) return;
		BufferCapabilities desiredCaps = new BufferCapabilities(
			new ImageCapabilities(true), new ImageCapabilities(true),
			Monitor.PAGE_FLIPPING ? FlipContents.BACKGROUND : null
		);
		Class<?> extBufCapClass = null;
		try {
			// Creates ExtendedBufferCapabilities via reflection to avoid problems with AccessControl
			extBufCapClass = Class.forName("sun.java2d.pipe.hw.ExtendedBufferCapabilities");
		} catch (Exception ex) {}
		// First try with vSync option
		if (extBufCapClass != null && Monitor.BUFFER_VSYNC != -1)
			try {
				// Creates ExtendedBufferCapabilities via reflection to avoid problems with AccessControl
				Class<?> vSyncTypeClass = Class.forName("sun.java2d.pipe.hw.ExtendedBufferCapabilities$VSyncType");
				Constructor<?> extBufCapConstructor = extBufCapClass.getConstructor(
					new Class[] { BufferCapabilities.class, vSyncTypeClass }
				);
	            Object vSyncType = vSyncTypeClass.getField(Monitor.BUFFER_VSYNC == 1 ? "VSYNC_ON" : "VSYNC_OFF").get(null);
	            BufferCapabilities extBuffCaps = (BufferCapabilities)extBufCapConstructor.newInstance(
	            	new Object[] { desiredCaps, vSyncType }
	            );
	            // Try creating the BufferStrategy
	            this.createBufferStrategy(Monitor.MULTI_BUFFERING, extBuffCaps);
			} catch (Exception ex) {}
		// Then try with remaining options (Flipping, etc)
		if (this.getBufferStrategy() == null)
			try {
				this.createBufferStrategy(Monitor.MULTI_BUFFERING, desiredCaps);
			} catch (Exception ex) {}
		// Last, use the default
		if (this.getBufferStrategy() == null) {
			System.out.println("Could not create desired BufferStrategy. Switching to default...");
			this.createBufferStrategy(Monitor.MULTI_BUFFERING);
		}
		bufferStrategy = this.getBufferStrategy();
		// Show info about the granted BufferStrategy
		if (bufferStrategy != null) System.out.println("Buffer Strategy: " + bufferStrategy.getClass().getSimpleName());
		BufferCapabilities grantedCaps = bufferStrategy.getCapabilities();
		System.out.println("Backbuffer Accelerated: " + grantedCaps.getBackBufferCapabilities().isAccelerated());
		System.out.println("PageFlipping Active: " + grantedCaps.isPageFlipping() + ", " + grantedCaps.getFlipContents());
		if (extBufCapClass != null && grantedCaps.getClass().equals(extBufCapClass))
			try {
				System.out.println("VSynch active: " + extBufCapClass.getMethod("getVSync",(Class<?>[])null).invoke(grantedCaps));
			} catch (Exception ex) {}
	}

	
	private final DesktopScreenWindow window;
	private int canvasOriginX;
	private int canvasOriginY;
	private Dimension canvasEffectiveSize;
	private int clearBackgoundFrames = 0;

	private BufferStrategy bufferStrategy;

	public static final long serialVersionUID = 1L;

}
