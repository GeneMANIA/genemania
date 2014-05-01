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
 * Profiler: basic memory and time profiler 
 * Created Jan 22, 2010
 * @author Ovi Comes
 */
package org.genemania.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.genemania.Constants;

public class Profiler {

	// __[static]______________________________________________________________
	private static Logger LOG = Logger.getLogger(Profiler.class);
	
	// __[attributes]__________________________________________________________
	private Hashtable<String, Profile> markers = new Hashtable<String, Profile>();
	private List<Metric> metrics = new ArrayList<Metric>();

	// __[constructors]________________________________________________________
	public Profiler() {
	}

	// __[public interface]____________________________________________________
	public void startSession() {
		ProfilingUtils.clearMemory();
		markers = new Hashtable<String, Profile>();
		metrics = new ArrayList<Metric>();
	}

	public void stopSession() {
		markers = new Hashtable<String, Profile>();
		metrics = new ArrayList<Metric>();
		ProfilingUtils.clearMemory();
	}

	public void addStartMarker(String pid) {
		System.gc();
		Profile profile = new Profile(pid);
		markers.put(Profile.getStartId(pid), profile);
	}

	public void addStopMarker(String pid) {
		Profile profile = new Profile(pid);
		markers.put(Profile.getStopId(pid), profile);
		Metric metric = new Metric(pid);
		metrics.add(metric);
	}

	public synchronized void printMetrics() {
		for (Metric metric : metrics) {
			LOG.info(metric.toString());
		}
	}

	// __[inner classes]_______________________________________________________
	public static class Profile {
		
		private static String id;
		private long timestamp;
		private long usedMemory; 
		
		public Profile(String id) {
			Profile.id = id;
			timestamp = System.currentTimeMillis();
			usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(); 
		}

		public static String getStartId(String pid) {
			return pid + "_start";
		}
		
		public static String getStopId(String pid) {
			return pid + "_stop";
		}
		
		public long getTimestamp() {
			return timestamp;
		}

		public long getUsedMemory() {
			return usedMemory;
		}

	}
	
	public class Metric {

		private String pid;
		private long duration;
		private long memory;

		public Metric(String pid) {
			this.pid = pid;
			Profile startMarker = markers.get(Profile.getStartId(pid));
			if (startMarker == null) {
				LOG.warn("\"" + pid + "\" process has no start marker");
			}
			Profile stopMarker = markers.get(Profile.getStopId(pid));
			if (stopMarker == null) {
				LOG.warn("\"" + pid + "\" process has no stop marker");
			}
			if ((startMarker != null) && (stopMarker != null)) {
				duration = stopMarker.getTimestamp()
						- startMarker.getTimestamp();
				memory = stopMarker.getUsedMemory()
						- startMarker.getUsedMemory();
			}
		}

		public String getPid() {
			return pid;
		}

		public long getDuration() {
			return duration;
		}

		public long getMemory() {
			return memory;
		}

		public String toString() {
			StringBuffer ret = new StringBuffer();
			ret.append(pid);
			ret.append(": duration=");
			if (duration < 1000) {
				ret.append(duration);
				ret.append("ms");
			} else {
				ret.append(Math.round(duration / 1000));
				ret.append("s");
			}
			ret.append(", memory=");
			if (memory > 0) {
				if (memory < Constants.oneKilobyte) {
					ret.append(memory);
					ret.append(" bytes");
				} else if (memory < Constants.oneMegabyte) {
					ret.append(memory / Constants.oneKilobyte);
					ret.append("kb");
				} else {
					ret.append(memory / Constants.oneMegabyte);
					ret.append("Mb");
				}
			} else {
				ret.append("???");
			}
			return ret.toString();
		}
	}
	
}
