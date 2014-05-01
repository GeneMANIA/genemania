/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2010 University of Toronto.
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

/**
 * GeneManiaStringUtils - String utility methods
 * Created Jan 28, 2009
 * @author Ovi Comes
 */
package org.genemania.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.genemania.Constants;

public class GeneManiaStringUtils {

	// __[static]______________________________________________________________
	private static Logger LOG = Logger.getLogger(GeneManiaStringUtils.class);
	private final static String SPACE = " "; 
	private final static String TAB = "\t"; 
	private final static String CR = "\n"; 
	private final static String COMMA = ","; 
	private final static String SEMICOLON = ";"; 
	
	// __[public helpers]______________________________________________________
	public static String extractSeparator(String text) {
		String ret = " ";
		Hashtable<String, Integer> separatorsMap = new Hashtable<String, Integer>();
		separatorsMap.put(SPACE, Integer.valueOf(StringUtils.countMatches(text, " ")));
		separatorsMap.put(TAB, Integer.valueOf(StringUtils.countMatches(text, "\t")));
		separatorsMap.put(CR, Integer.valueOf(StringUtils.countMatches(text, "\n")));
		separatorsMap.put(COMMA, Integer.valueOf(StringUtils.countMatches(text, ",")));
		separatorsMap.put(SEMICOLON, Integer.valueOf(StringUtils.countMatches(text, ";")));
		Enumeration<String> separators = separatorsMap.keys();
		int maxCount = 0; 
		while(separators.hasMoreElements()) {
			String nextSeparator = separators.nextElement();
			int nextSeparatorCount = ((Integer)separatorsMap.get(nextSeparator)).intValue();
			if(nextSeparatorCount > maxCount) {
				maxCount = nextSeparatorCount;
				ret = nextSeparator;
			}
		}
		return ret;
	}

	public static boolean isDoublePrecisionNumber(String text) {
		boolean ret = true;
		try {
			Double.parseDouble(text);
		} catch (NumberFormatException e) {
			ret = false;
		}
		return ret;
	}
	
	public static List<Long> networksStringToList(String networkIdStr) {
		List<Long> ret = new ArrayList<Long>();
		StringTokenizer st = new StringTokenizer(networkIdStr, Constants.NETWORK_IDS_SEPARATOR);
		while (st.hasMoreElements()) {
			String nextNetworkId = st.nextToken();
			try {
				ret.add(Long.parseLong(nextNetworkId));
			} catch (NumberFormatException e) {
				LOG.warn("Invalid network ID: " + nextNetworkId);
			}
		}
		return ret;
	}
	
}
