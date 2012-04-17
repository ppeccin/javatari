// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.screen;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;

public interface DisplayCanvas {
	
	public void canvasSize(Dimension size);
	
	public Dimension canvasEffectiveSize();
	
	public void canvasSetRenderingMode();
	
	public Graphics2D canvasGraphics();
	
	public void canvasFinishGraphics(Graphics2D graphics);

	public void canvasClear();
	
	public Container canvasContainer();
	
	public float getDefaultOpenningScaleX(int displayWidth, int displayHeight);
	
}
