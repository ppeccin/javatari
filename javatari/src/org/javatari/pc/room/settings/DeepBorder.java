// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.room.settings;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.border.Border;

public class DeepBorder implements Border {    
    private final int radius;
	private final Insets insets;
    
    public DeepBorder(int radius, Insets insets) {
        this.radius = radius;
		this.insets = insets;
    }

    public Insets getBorderInsets(Component c) {
        return insets;
    }

    public Insets getBorderInsets(Component c, Insets ins) {
        ins.set(insets.top, insets.left, insets.bottom, insets.right);
        return ins;
    }

    public boolean isBorderOpaque() {
        return false;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Color color = newBrightness(c.getBackground(), -.3f);
        
        g2.setColor(newAlpha(color, 40));        
        g2.drawRoundRect(x, y + 2, width - 1, height - 4, radius, radius);

        g2.setColor(newAlpha(color, 90));        
        g2.drawRoundRect(x + 1, y + 1, width - 3, height - 2, radius, radius); 

        g2.setColor(newAlpha(color, 255));        
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);

        g2.dispose();            
    }
    
   	public static Color newBrightness(Color base, float dB) {
    	float hsb[] = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
    	hsb[2] += dB;
    	return Color.getHSBColor(hsb[0], hsb[1], hsb[2] < 0? 0 : (hsb[2] > 1? 1 : hsb[2]));
   	}
    
    public static Color newAlpha(Color base, int alpha) {
    	return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
   	}

}
