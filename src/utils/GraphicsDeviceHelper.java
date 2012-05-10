// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package utils;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class GraphicsDeviceHelper {

	public static BufferedImage loadImage(String fileName) throws IOException {
		URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
		if (url == null)
			throw new IOException("Could not find image: " + fileName);
		return ImageIO.read(url);
	}

	public static BufferedImage loadAsCompatibleImage(String fileName) throws IOException {
		return asCompatibleImage(loadImage(fileName));
	}
	
	public static BufferedImage asCompatibleImage(Image img) {
		BufferedImage ret = defaultScreenDeviceConfiguration().createCompatibleImage(img.getWidth(null), img.getHeight(null));
		Graphics2D gc = ret.createGraphics();
		gc.drawImage(img, 0, 0, null);
		gc.dispose();
		return ret;
	}

	public static BufferedImage asCompatibleImage(Image img, int transparency) {
		BufferedImage ret = defaultScreenDeviceConfiguration().createCompatibleImage(img.getWidth(null), img.getHeight(null), transparency);
		Graphics2D gc = ret.createGraphics();
		gc.setComposite(AlphaComposite.Src);
		gc.drawImage(img, 0, 0, null);
		gc.dispose();
		return ret;
	}

	public static VolatileImage asCompatibleVolatileImage(Image img) {
		VolatileImage ret = defaultScreenDeviceConfiguration().createCompatibleVolatileImage(img.getWidth(null), img.getHeight(null));
		Graphics2D gc = ret.createGraphics();
		gc.drawImage(img, 0, 0, null);
		gc.dispose();
		return ret;
	}

	public static VolatileImage asCompatibleVolatileImage(Image img, int transparency) {
		VolatileImage ret = defaultScreenDeviceConfiguration().createCompatibleVolatileImage(img.getWidth(null), img.getHeight(null), transparency);
		Graphics2D gc = ret.createGraphics();
		gc.setComposite(AlphaComposite.Src);
		gc.drawImage(img, 0, 0, null);
		gc.dispose();
		return ret;
	}

	public static GraphicsConfiguration defaultScreenDeviceConfiguration() {
		return defaultScreenDevice().getDefaultConfiguration();
	}

	public static GraphicsDevice defaultScreenDevice() {
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		if (env == null)
			throw new UnsupportedOperationException("Could not get Local Graphics Environment");
		GraphicsDevice dev = env.getDefaultScreenDevice();
		if (dev == null)
			throw new UnsupportedOperationException("Could not get Default Graphics Device");
		return dev;
	}
	
}
