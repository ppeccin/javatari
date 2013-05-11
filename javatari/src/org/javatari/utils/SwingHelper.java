// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.utils;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public final class SwingHelper {

	public static BufferedImage loadImage(String fileName) throws IOException {
		URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
		if (url == null)
			throw new IOException("Could not find image: " + fileName);
		return ImageIO.read(url);
	}

	public static BufferedImage loadAsCompatibleImage(String fileName) throws IOException {
		return asCompatibleImage(loadImage(fileName));
	}

	public static BufferedImage loadAsCompatibleTranslucentImage(String fileName) throws IOException {
		return asCompatibleImage(loadImage(fileName), Transparency.TRANSLUCENT);
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
	
	public static void edtInvokeLater(Runnable block) {
		SwingUtilities.invokeLater(block);
	}

	public static void edtSmartInvokeAndWait(Runnable block) {
		if (!SwingUtilities.isEventDispatchThread())
			try {
				SwingUtilities.invokeAndWait(block);
			} catch (InterruptedException e) {} catch (InvocationTargetException e) {}
		else
			block.run();
	}
	
	public static GraphicsConfiguration getGraphicsConfigurationForCurrentLocation(Window window) {
		GraphicsConfiguration ownedConfig = window.getGraphicsConfiguration();
		Point currLocation = window.getLocation();
		// Shortcut for "owned" case
		if (ownedConfig.getBounds().contains(currLocation))
			return ownedConfig;
		
		// Search for the right screen
		GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		for (GraphicsDevice screen : screens)
			for (GraphicsConfiguration config : screen.getConfigurations())
				if (config.getBounds().contains(currLocation)) 
					return config;
		
		// If none found, lets return the "owned" one
		return ownedConfig;
	}
	
}
