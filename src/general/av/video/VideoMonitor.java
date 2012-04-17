// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.av.video;

public interface VideoMonitor {
	
	public boolean nextLine(int[] pixels, boolean vSynch);
	public void showOSD(String message);
	public void synchOutput();

	public int currentLine();
	
}
