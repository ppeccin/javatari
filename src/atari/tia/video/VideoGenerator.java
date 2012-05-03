// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package atari.tia.video;

import general.av.video.VideoMonitor;
import general.av.video.VideoSignal;
import general.av.video.VideoStandard;

public final class VideoGenerator implements VideoSignal {

	public boolean newLine(final int[] pixels, boolean vSynch) {
		if (monitor == null) return false;
		return monitor.nextLine(pixels, vSynch);
	}

	public void showOSD(String message) {
		if (monitor == null) return;
		monitor.showOSD(message);
	}

	public VideoStandard videoStandardDetected() {
		return monitor != null ? monitor.videoStandardDetected() : null;
	}

	@Override
	public VideoStandard standard() {
		return standard;
	}

	@Override
	public void connectMonitor(VideoMonitor monitor) {
		this.monitor = monitor;
	}

	public VideoMonitor monitor;
	public VideoStandard standard;
	
}
