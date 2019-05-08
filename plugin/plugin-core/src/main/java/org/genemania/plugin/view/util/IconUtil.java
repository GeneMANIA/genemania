package org.genemania.plugin.view.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.util.swing.TextIcon;

public abstract class IconUtil {
	
	public static final String GENEMANIA_LOGO = "a";
	public static final String BACTERIA = "b";
	public static final String FISH = "c";
	public static final String FLY = "d";
	public static final String HUMAN = "e";
	public static final String MOUSE = "f";
	public static final String PLANT = "g";
	public static final String RAT = "h";
	public static final String WORM = "i";
	public static final String YEAST = "j";
	public static final String MISSING_ORGANISM = "k";
	
	public static final Color GENEMANIA_LOGO_COLOR = Color.BLACK;
	
	public static final int ORG_ICON_SIZE = 32;
	
	private static Font iconFont;
	private static final Map<Long, String> orgMap = new HashMap<>();

	static {
		try {
			iconFont = Font.createFont(Font.TRUETYPE_FONT, IconUtil.class.getResourceAsStream("/fonts/genemania.ttf"));
		} catch (FontFormatException e) {
			throw new RuntimeException();
		} catch (IOException e) {
			throw new RuntimeException();
		}
		
		orgMap.put(83333L, BACTERIA);
		orgMap.put(7955L, FISH);
		orgMap.put(7227L, FLY);
		orgMap.put(9606L, HUMAN);
		orgMap.put(10090L, MOUSE);
		orgMap.put(3702L, PLANT);
		orgMap.put(10116L, RAT);
		orgMap.put(6239L, WORM);
		orgMap.put(4932L, YEAST);
	}
	
	public static Font getIconFont(float size) {
		return iconFont.deriveFont(size);
	}
	
	public static TextIcon getOrganismIcon(long taxonomyId) {
		String text = orgMap.get(taxonomyId);
		
		if (text == null)
			text = MISSING_ORGANISM;
		
		return new TextIcon(text, getIconFont(28.0f), null, ORG_ICON_SIZE, ORG_ICON_SIZE);
	}

	private IconUtil() {
		// ...
	}
}
