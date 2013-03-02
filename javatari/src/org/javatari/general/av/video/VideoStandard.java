// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.av.video;

import org.javatari.parameters.Parameters;

public enum VideoStandard  {

	NTSC(228, 262, Parameters.VIDEO_NTSC_FPS),
	PAL(228, 312, Parameters.VIDEO_PAL_FPS);

	VideoStandard(int width, int height, double fps) {
		this.width = width;
		this.height = height;
		this.fps = fps;
	}

	public final int width;
	public final int height;
	public final double fps;

}
	
