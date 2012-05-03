// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.av.video;

public enum VideoStandard  {

	NTSC("NTSC", 228, 262, 60),
	PAL("PAL", 228, 312, 50);

	VideoStandard(String name, int width, int height, int fps) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.fps = fps;
	}

	public final String name;
	public final int width;
	public final int height;
	public final int fps;

}
	
