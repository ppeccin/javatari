// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.av.video;

public interface VideoMonitor {
	
	public boolean nextLine(int[] pixels, boolean vSynch);
	public void showOSD(String message, boolean overlap);
	public void synchOutput();

	public int currentLine();
	public void videoStandardDetectionStart();
	public VideoStandard videoStandardDetected();
	
}
