// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.tia.video;

import org.javatari.general.av.video.VideoMonitor;
import org.javatari.general.av.video.VideoSignal;
import org.javatari.general.av.video.VideoStandard;

public final class VideoGenerator implements VideoSignal {

	public boolean nextLine(final int[] pixels, boolean vSynch) {
		if (monitor == null) return false;
		return monitor.nextLine(pixels, vSynch);
	}

	public void signalOff() {
		if (monitor != null) monitor.nextLine(null, false);
	}

	public void showOSD(String message, boolean overlap) {
		if (monitor == null) return;
		monitor.showOSD(message, overlap);
	}

	public VideoMonitor monitor() {
		return monitor;
	}

	@Override
	public VideoStandard standard() {
		return standard;
	}

	@Override
	public void connectMonitor(VideoMonitor monitor) {
		this.monitor = monitor;
	}

	public void standard(VideoStandard standard) {
		this.standard = standard;
	}

	private VideoMonitor monitor;
	private VideoStandard standard;
	
}
