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
 * ProfilingUtils: TODO add description
 * Created Jul 08, 2009
 * @author Ovi Comes
 */
package org.genemania.util;

import org.apache.log4j.Logger;
import org.genemania.domain.Organism;

public class ProfilingUtils {

	// __[static]______________________________________________________________
	private static Logger LOG = Logger.getLogger(ProfilingUtils.class);
	
	// __[public interface]____________________________________________________
	public static void clearMemory() {
//		LOG.debug("invoking garbage collector");		
		System.gc(); System.gc(); System.gc(); System.gc();
	}
	
	public static long getFreeMemory() {
		return Runtime.getRuntime().freeMemory()/1073741824;
	}
	
	public static long getUsedMemory() {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}
	
	public static long getUsedMemory(String mu) {
		long ret;
		ret = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		if("K".equalsIgnoreCase(mu)) {
			ret = ret/1024;
		} else if("M".equalsIgnoreCase(mu)) {
			ret = ret/1048576;
		} else if("G".equalsIgnoreCase(mu)) {
			ret = ret/1073741824;
		}
		return ret;
	}
	
	public static long calculateMemoryUsage(ObjectFactory factory) {
		Object handle = factory.makeObject();
		long mem0 = getUsedMemory();
		long mem1 = getUsedMemory();
		handle = null;
		clearMemory();
		mem0 = getUsedMemory();
		handle = factory.makeObject();
		clearMemory();
		mem1 = getUsedMemory();
		return mem1 - mem0;
	}

	public static void showMemoryUsage(ObjectFactory factory) {
		long mem = calculateMemoryUsage(factory);
		System.out.println(factory.getClass().getName() + " produced "
				+ factory.makeObject().getClass().getName() + " which took "
				+ mem + " bytes");
	}

	public interface ObjectFactory {
		public Object makeObject();
	}

//	public static class OrganismFactory implements ObjectFactory {
//		public Object makeObject() {
//			return new Organism();
//		}
//	}
	
}
