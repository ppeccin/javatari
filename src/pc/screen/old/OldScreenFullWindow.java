// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.screen.old;

import java.awt.BufferCapabilities;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.ImageCapabilities;
import java.awt.image.BufferStrategy;
import java.lang.reflect.Constructor;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import pc.screen.DisplayCanvas;

public class OldScreenFullWindow extends JFrame implements DisplayCanvas {

	public OldScreenFullWindow() throws HeadlessException {
		super();
		init();
	}

	private void init() {
		setUndecorated(true);
		setBackground(Color.BLACK);
		setIgnoreRepaint(true);
		getContentPane().setBackground(Color.BLACK);
		getContentPane().setIgnoreRepaint(true);
		setLayout(null);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			canvasSetRenderingMode();
			SwingUtilities.invokeLater(new Runnable() {  @Override public void run() {
				repaint();
			}});
		}
	}

	@Override
	public void canvasSize(Dimension size) {
		canvasOriginX = (getWidth() - size.width) / 2;
		canvasOriginY = (getHeight() - size.height) / 2;
		canvasEffectiveSize = size;
		repaint();
		if (bufferStrategy != null && bufferStrategy.getCapabilities().getFlipContents() != FlipContents.BACKGROUND) {
			clearBackgoundFrames = OldScreen.MULTI_BUFFERING + 2;		// Extra 2 frames more than needed
		}
	}

	@Override
	public void canvasMinimumSize(Dimension minSize) {
	}

	@Override
	public void canvasCenter() {
		// Full window is always centered
	}

	@Override
	public Dimension canvasEffectiveSize() {
		return canvasEffectiveSize;
	}

	@Override
	public Graphics2D canvasGraphics() {
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
	public void canvasFinishFrame(Graphics2D graphics) {
		graphics.dispose();
		if (bufferStrategy != null) bufferStrategy.show();
		// Toolkit.getDefaultToolkit().sync();		 // Really needed?
	}

	@Override
	public void canvasClear() {
		Graphics2D canvasGraphics = canvasGraphics();
		canvasGraphics.setColor(Color.BLACK);
		canvasGraphics.clearRect(0, 0, getWidth(), getHeight());
		canvasFinishFrame(canvasGraphics);
	}

	@Override
	public Container canvasContainer() {
		return this;
	}

	@Override
	// Gets the largest possible size at the default aspect
	public float canvasDefaultOpenningScaleX(int displayWidth, int displayHeight) {
		float scaleX = getWidth() / displayWidth;
		scaleX -= (scaleX % OldScreen.DEFAULT_SCALE_ASPECT_X);				// Round multiple of the default X scale
		float h = scaleX / OldScreen.DEFAULT_SCALE_ASPECT_X * displayHeight;
		while (h > getHeight() + 20) {									// 20 is a little tolerance
			scaleX -= OldScreen.DEFAULT_SCALE_ASPECT_X;					// Decrease one full default X scale
			h = scaleX / OldScreen.DEFAULT_SCALE_ASPECT_X * displayHeight;
		}
		return scaleX;
	}

	@Override
	public void canvasLeaveFullscreen() {
	}

	public void canvasSetRenderingMode() {
		if (OldScreen.MULTI_BUFFERING <= 0) return;
		BufferCapabilities desiredCaps = new BufferCapabilities(
			new ImageCapabilities(true), new ImageCapabilities(true),
			OldScreen.PAGE_FLIPPING ? FlipContents.BACKGROUND : null
		);
		// First try with vSync option
		Class<?> extBufCapClass = null;
		if (OldScreen.BUFFER_VSYNC != -1)
			try {
				// Creates ExtendedBufferCapabilities via reflection to avoid problems with AccessControl
				extBufCapClass = Class.forName("sun.java2d.pipe.hw.ExtendedBufferCapabilities");
				Class<?> vSyncTypeClass = Class.forName("sun.java2d.pipe.hw.ExtendedBufferCapabilities$VSyncType");
				Constructor<?> extBufCapConstructor = extBufCapClass.getConstructor(
					new Class[] { BufferCapabilities.class, vSyncTypeClass }
				);
	            Object vSyncType = vSyncTypeClass.getField(OldScreen.BUFFER_VSYNC == 1 ? "VSYNC_ON" : "VSYNC_OFF").get(null);
	            BufferCapabilities extBuffCaps = (BufferCapabilities)extBufCapConstructor.newInstance(
	            	new Object[] { desiredCaps, vSyncType }
	            );
	            // Try creating the BufferStrategy
	            createBufferStrategy(OldScreen.MULTI_BUFFERING, extBuffCaps);
			} catch (Exception ex) {}
		// Then try with remaining options (Flipping, etc)
		if (getBufferStrategy() == null)
			try {
				createBufferStrategy(OldScreen.MULTI_BUFFERING, desiredCaps);
			} catch (Exception ex) {}
		// Last, use the default
		if (getBufferStrategy() == null) {
			System.out.println("Could not create desired BufferStrategy. Switching to default...");
			createBufferStrategy(OldScreen.MULTI_BUFFERING);
		}
		bufferStrategy = getBufferStrategy();
		// Show info about the granted BufferStrategy
		BufferCapabilities grantedCaps = bufferStrategy.getCapabilities();
		System.out.println("Backbuffer accelerated: " + grantedCaps.getBackBufferCapabilities().isAccelerated());
		System.out.println("PageFlipping active: " + grantedCaps.isPageFlipping() + ", " + grantedCaps.getFlipContents());
		if (extBufCapClass != null && grantedCaps.getClass().equals(extBufCapClass))
			try {
				System.out.println("VSynch active: " + extBufCapClass.getMethod("getVSync",(Class<?>[])null).invoke(grantedCaps));
			} catch (Exception ex) {}
	}

	
	private int canvasOriginX;
	private int canvasOriginY;
	private Dimension canvasEffectiveSize;
	private int clearBackgoundFrames = 0;

	private BufferStrategy bufferStrategy;

	public static final long serialVersionUID = 1L;

}
