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

package org.genemania.plugin.apps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class NetworkAssessorTest {
	private NetworkAssessor assessor;

	@Before
	public void setUp() {
		assessor = new NetworkAssessor();
	}
	
	@Test
	public void testSortResults() {
		List<String> queryIds = new ArrayList<String>();
		queryIds.add("A");
		queryIds.add("B");
		queryIds.add("C");
		
		Map<String, double[]> baselineMeasures = new HashMap<String, double[]>();
		Map<String, double[]> measures = new HashMap<String, double[]>();
		
		baselineMeasures.put("A", new double[] { 1 });
		baselineMeasures.put("B", new double[] { 1 });
		baselineMeasures.put("C", new double[] { 1 });
		
		measures.put("A", new double[] { 3 });
		measures.put("B", new double[] { 2 });
		measures.put("C", new double[] { 4 });
		
		assessor.sortResults(queryIds, baselineMeasures, measures);
		
		String result = flatten(queryIds);
		Assert.assertEquals("C,A,B", result);
	}

	@Test
	public void testNullResults() {
		List<String> queryIds = new ArrayList<String>();
		queryIds.add("A");
		queryIds.add("B");
		queryIds.add("C");
		queryIds.add("D");
		queryIds.add("E");
		queryIds.add("F");
		
		Map<String, double[]> baselineMeasures = new HashMap<String, double[]>();
		Map<String, double[]> measures = new HashMap<String, double[]>();
		
		baselineMeasures.put("A", null);
		baselineMeasures.put("B", new double[] { 2 });
		baselineMeasures.put("C", null);
		baselineMeasures.put("D", new double[] { 1 });
		baselineMeasures.put("E", new double[] { 1 });
		baselineMeasures.put("F", new double[] { 0 });
		
		measures.put("A", new double[] { 3 });
		measures.put("B", null);
		measures.put("C", null);
		measures.put("D", new double[] { 2 });
		measures.put("E", new double[] { 3 });
		measures.put("F", new double[] { 2 });
		
		assessor.sortResults(queryIds, baselineMeasures, measures);
		
		String result = flatten(queryIds);
		Assert.assertEquals("E,D,F,A,B,C", result);
	}

	private String flatten(List<String> list) {
		StringBuilder builder = new StringBuilder();
		for (String entry : list) {
			if (builder.length() > 0) {
				builder.append(",");
			}
			builder.append(entry);
		}
		return builder.toString();
	}
}
