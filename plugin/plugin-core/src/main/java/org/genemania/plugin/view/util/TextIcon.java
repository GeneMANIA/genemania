/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2008-2017 University of Toronto.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.genemania.plugin.view.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.UIManager;

public class TextIcon implements Icon {
	
	private final Color TRANSPARENT_COLOR = new Color(255, 255, 255, 0);
	
	private final String[] texts;
	private final Font[] fonts;
	private final Color[] colors;
	private final int width;
	private final int height;
	
	public TextIcon(String text, Font font, Color color, int width, int height) {
		this.texts = new String[] { text };
		this.fonts = new Font[] { font };
		this.colors = new Color[] { color };
		this.width = width;
		this.height = height;
	}
	
	public TextIcon(String[] texts, Font font, Color[] colors, int width, int height) {
		this(texts, new Font[] { font }, colors, width, height);
	}
	
	public TextIcon(String[] texts, Font[] fonts, Color[] colors, int width, int height) {
		this.texts = texts;
		this.fonts = fonts;
		this.colors = colors;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
        
        int xx = c.getWidth();
        int yy = c.getHeight();
        g2d.setPaint(TRANSPARENT_COLOR);
        g2d.fillRect(0, 0, xx, yy);
        
        if (texts != null && fonts != null) {
	    		Font f = null;
	    		Color fg = null;
	    	
	    		for (int i = 0; i < texts.length; i++) {
	    			String txt = texts[i];
	    			
	    			if (fonts.length > i) 
	    				f = fonts[i];
	    			
	    			if (txt == null || f == null)
	    				continue;
	    			
	    			if (colors != null && colors.length > i)
	    				fg =  colors[i];
		        
		        if (fg == null)
	        			fg = c.getForeground();
		        
		        if (c instanceof AbstractButton) {
			        	if (!c.isEnabled())
			        		fg = UIManager.getColor("Label.disabledForeground");
			        	else if (((AbstractButton) c).getModel().isPressed())
			        		fg = fg.darker();
		        }
		        
		        g2d.setPaint(fg);
		        g2d.setFont(f);
		        drawText(txt, f, g2d, c, x, y);
	    		}
	    }
        
        g2d.dispose();
	}
	
	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}
	
	protected void drawText(String text, Font font, Graphics g, Component c, int x, int y) {
		FontMetrics fm = g.getFontMetrics(font);
		Rectangle2D rect = fm.getStringBounds(text, g);

		int textHeight = (int) rect.getHeight();
		int textWidth = (int) rect.getWidth();

		// Center text horizontally and vertically
		int xx = x + Math.round((getIconWidth() - textWidth) / 2.0f);
		int yy = y + Math.round((getIconHeight() - textHeight) / 2.0f) + fm.getAscent();

		g.drawString(text, xx, yy);
	}
}

