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

package org.genemania.engine.validation;

public class GeneResult implements Comparable<GeneResult> {
	
	private int nodeId;
	private double totalScore;
	private int scoreCount;
	private double totalPercentile;
	private int percentileCount;
	
	public GeneResult(int nodeId) {
		this.nodeId = nodeId;
	}
	
	public int compareTo(GeneResult qr) {
		double diff = getAveragePercentile() - qr.getAveragePercentile();
		if (diff == 0)
			return (int)diff;
		return diff > 0 ? 1 : -1;
	}
	
	public int getNodeID() {
		return nodeId;
	}
	
	public double getAverageScore() {
		return totalScore / scoreCount;
	}
	
	public double getAveragePercentile() {
		return totalPercentile / percentileCount;
	}
	
	public void addScore(double score) {
		totalScore += score;
		scoreCount++;
	}
	
	public void addPercentile(double percentile) {
		totalPercentile += percentile;
		percentileCount++;
	}
}
