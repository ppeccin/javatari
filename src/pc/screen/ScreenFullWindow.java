// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.screen;

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.ImageCapabilities;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

import sun.java2d.pipe.hw.ExtendedBufferCapabilities;
import sun.java2d.pipe.hw.ExtendedBufferCapabilities.VSyncType;

public class ScreenFullWindow extends JFrame implements DisplayCanvas {

	public ScreenFullWindow() throws HeadlessException {
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
	public void canvasSize(Dimension size) {
		canvasOriginX = (getWidth() - size.width) / 2;
		canvasOriginY = (getHeight() - size.height) / 2;
		canvasEffectiveSize = size;
		repaint();
		if (bufferStrategy != null && bufferStrategy.getCapabilities().getFlipContents() != FlipContents.BACKGROUND) {
			clearBackgoundFrames = Screen.MULTI_BUFFERING + 2;		// Extra 2 frames more than needed
		}
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
	public void canvasSetRenderingMode() {
		if (Screen.MULTI_BUFFERING > 0) {
			try {
				createBufferStrategy(Screen.MULTI_BUFFERING, new ExtendedBufferCapabilities(
					new ImageCapabilities(true), new ImageCapabilities(true),
					Screen.PAGE_FLIPPING ? FlipContents.BACKGROUND : null,
					Screen.VSYNC ? VSyncType.VSYNC_ON : VSyncType.VSYNC_OFF
				));
			} catch (AWTException e) {
				try {
					createBufferStrategy(Screen.MULTI_BUFFERING, new BufferCapabilities(
						new ImageCapabilities(true), new ImageCapabilities(true),
						Screen.PAGE_FLIPPING ? FlipContents.BACKGROUND : null
					));
				} catch (AWTException e2) {
					System.out.println("Could not create desired BufferStrategy. Switching to default...");
					createBufferStrategy(Screen.MULTI_BUFFERING);
				}
			}
			bufferStrategy = getBufferStrategy();
			BufferCapabilities caps = bufferStrategy.getCapabilities();
			System.out.println("Backbuffer accelerated: " + caps.getBackBufferCapabilities().isAccelerated());
			System.out.println("PageFlipping active: " + caps.isPageFlipping());
			if (caps instanceof ExtendedBufferCapabilities)
				System.out.println("VSynch active: " + ((ExtendedBufferCapabilities)caps).getVSync());
		}
	}

	@Override
	public Graphics2D canvasGraphics() {
		try {
			Graphics2D canvasGraphics = (Graphics2D) (bufferStrategy != null ? bufferStrategy.getDrawGraphics() : getGraphics());
			// Clears the background when needed, but just for a few frames
			if (clearBackgoundFrames-- > 0) {
				canvasGraphics.setColor(Color.BLACK);
				canvasGraphics.clearRect(0, 0, getWidth(), getHeight());
			}
			canvasGraphics.translate(canvasOriginX, canvasOriginY);
			return canvasGraphics;
		} catch(IllegalStateException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void canvasFinishGraphics(Graphics2D graphics) {
		graphics.dispose();
		if (bufferStrategy != null) bufferStrategy.show();
		// Toolkit.getDefaultToolkit().sync();		// Really needed?
	}

	@Override
	public void canvasClear() {
		Graphics2D canvasGraphics = canvasGraphics();
		canvasGraphics.setColor(Color.BLACK);
		canvasGraphics.clearRect(0, 0, getWidth(), getHeight());
		canvasFinishGraphics(canvasGraphics);
	}

	@Override
	public Container canvasContainer() {
		return this;
	}

	@Override
	// Gets the largest possible size at the default aspect
	public float getDefaultOpenningScaleX(int displayWidth, int displayHeight) {
		float scaleX = getWidth() / displayWidth;
		scaleX -= (scaleX % Screen.DEFAULT_SCALE_ASPECT_X);				// Round multiple of the default X scale
		float h = scaleX / Screen.DEFAULT_SCALE_ASPECT_X * displayHeight;
		while (h > getHeight() + 20) {									// 20 is a little tolerance
			scaleX -= Screen.DEFAULT_SCALE_ASPECT_X;					// Decrease one full default X scale
			h = scaleX / Screen.DEFAULT_SCALE_ASPECT_X * displayHeight;
		}
		return scaleX;
	}


	private int canvasOriginX;
	private int canvasOriginY;
	private Dimension canvasEffectiveSize;
	private int clearBackgoundFrames = 0;

	private BufferStrategy bufferStrategy;

	public static final long serialVersionUID = 1L;

}
