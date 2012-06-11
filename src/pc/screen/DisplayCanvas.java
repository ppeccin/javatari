// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.screen;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;

public interface DisplayCanvas {
	
	public void canvasCenter();
	
	public void canvasSize(Dimension size);
		
	public void canvasMinimumSize(Dimension size);
	
	public void canvasFinishFrame(Graphics2D graphics);

	public void canvasClear();
	
	public Dimension canvasEffectiveSize();
	
	public Graphics2D canvasGraphics();
		
	public Container canvasContainer();
	
	public float canvasDefaultOpenningScaleX(int displayWidth, int displayHeight);
	
	public void canvasLeaveFullscreen();

		
}
