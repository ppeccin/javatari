package org.javatari.pc.screen;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

// Simulates a TV display at the sub pixel level (triads) 
class CRTTriadCompositeOld implements Composite {
	private int[] data = new int[3000];
	public CompositeContext context = new CompositeContext() {
		@Override
		public void dispose() {
		}
		@Override
		public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
			int w = Math.min(src.getWidth(), dstOut.getWidth());
			int h = Math.min(src.getHeight(), dstOut.getHeight());
			for(int y = 0; y < h; y++) {
				src.getDataElements(src.getMinX(), src.getMinY() + y, w, 1, data);
				int c;
				for(c = 0; c < w - 2; c += 3) {
					data[c]   = (int) Math.min((data[c] & 0xff0000) * 1.3f, 0xff0000) & 0xff0000;
					data[c+1] = (int) Math.min((data[c+1] & 0xff00) * 1.3f, 0xff00) & 0xff00;
					data[c+2] = (int) Math.min((data[c+2] & 0xff) * 1.3f, 0xff);
				}
				if (c < w) data[c] = (int) Math.min((data[c] & 0xff0000) * 1.30f, 0xff0000) & 0xff0000;
				if (c < w - 1) data[c+1] = (int) Math.min((data[c+1] & 0xff00) * 1.30f, 0xff00) & 0xff00;
				dstOut.setDataElements(dstOut.getMinX(), dstOut.getMinY() + y, w, 1, data);
			}
		}
	};
	@Override
	public CompositeContext createContext(ColorModel srcColorModel,	ColorModel dstColorModel, RenderingHints hints) {
		return context;
	}

}