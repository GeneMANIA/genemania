/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2008-2011 University of Toronto.
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

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

public class ImageCache {
	
	public static final String ARROW_COLLAPSED_IMAGE = "arrow_collapsed.png"; //$NON-NLS-1$
	public static final String ARROW_EXPANDED_IMAGE = "arrow_expanded.png"; //$NON-NLS-1$
	public static final String ICON = "helix.png"; //$NON-NLS-1$
	
	private Map<String, ImageIcon> iconCache;
	private Map<String, Image> imageCache;
	
	public ImageCache() {
		iconCache = new HashMap<String, ImageIcon>();
		imageCache = new HashMap<String, Image>();
	}
	
	public ImageIcon getIcon(String resourcePath) {
		ImageIcon icon = iconCache.get(resourcePath);
		if (icon != null) {
			return icon;
		}
		URL imageUrl = getClass().getResource(resourcePath);
		if (imageUrl == null) {
			imageUrl = getClass().getClassLoader().getResource(resourcePath);
			if (imageUrl == null) {
				return null;
			}
		}
		icon = new ImageIcon(imageUrl);
		iconCache.put(resourcePath, icon);
		return icon;
	}
	
	public Image getImage(String resourcePath) {
		Image image = imageCache.get(resourcePath);
		if (image != null) {
			return image;
		}
		URL imageUrl = getClass().getResource(resourcePath);
		if (imageUrl == null) {
			imageUrl = getClass().getClassLoader().getResource(resourcePath);
			if (imageUrl == null) {
				return null;
			}
		}
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		image = toolkit.getImage(imageUrl);
		imageCache.put(resourcePath, image);
		return image;
	}
}
