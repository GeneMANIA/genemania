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
 * UidGenerator: Utility class for generating unique IDs 
 * Created Oct 23, 2009
 * @author Ovi Comes
 */
package org.genemania.util;

import java.util.Hashtable;
import java.util.Map;

public class UidGenerator {

	// __[static]______________________________________________________________
	private static UidGenerator instance = new UidGenerator();
	private static Map<String, Integer> sessionIdMap = new Hashtable<String, Integer>();
	private static long lastId = 0;
	
	// __[constructors]________________________________________________________
	private UidGenerator() {
	}
	
	// __[accessors]___________________________________________________________
	public static UidGenerator getInstance() {
		return instance;
	}
	
	// __[public helpers]______________________________________________________
	public synchronized long getApplicationWideUid() {
		return --lastId;
	}
	
	public synchronized long getNextNegativeUid() {
		return --lastId;
	}
	
	public synchronized long getNextUidForSession(String sessionId) {
		return --lastId;
	}

	public synchronized long getNegativeUid() {
		return --lastId;
	}
	
	
}
