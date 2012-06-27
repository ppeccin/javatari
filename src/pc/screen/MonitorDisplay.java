// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.screen;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;

public interface MonitorDisplay {
	
	public void displayCenter();
	
	public void displaySize(Dimension size);
		
	public void displayMinimumSize(Dimension size);
	
	public void displayFinishFrame(Graphics2D graphics);

	public void displayClear();
	
	public Dimension displayEffectiveSize();
	
	public Graphics2D displayGraphics();
		
	public Container displayContainer();
	
	public float displayDefaultOpenningScaleX(int displayWidth, int displayHeight);
	
	public void displayRequestFocus();

	public void displayLeaveFullscreen();
		
}
