// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.av.video;

public interface VideoSignal {

	public VideoStandard standard();

	public void connectMonitor(VideoMonitor monitor);
	
}
	
