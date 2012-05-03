// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.av.video;

import java.io.Serializable;

public class VideoStandard implements Serializable {
	
	private VideoStandard(String name, int width, int height, int fps) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.fps = fps;
	}

	@Override
	public boolean equals(Object obj) {
		return this.name.equals(((VideoStandard)obj).name);
	}

	@Override
	public String toString() {
		return this.name;
	}

	public final String name;
	public final int width;
	public final int height;
	public final int fps;

	public static final VideoStandard NTSC = new VideoStandard("NTSC", 228, 262, 60);
	public static final VideoStandard PAL = new VideoStandard("PAL", 228, 312, 50);
	
	private static final long serialVersionUID = 1L;

}
	
