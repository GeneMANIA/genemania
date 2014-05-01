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

package org.genemania.engine.summary;

import org.genemania.engine.core.data.Network;
import org.genemania.engine.matricks.MatrixCursor;
import org.genemania.engine.matricks.Vector;

/*
 * compute a few reporting measures on
 * an interaction networks.
 */
public class NetworkStats {
	
	Network network;
	int numEdges;
	
	// number of genes with degree > 0
	int numInteractingNodes;
	
	 // some of weights of edges incident on each node
	Vector nodeDegrees;
	
	// number of edges incident on each node
	int [] nodeInteractorsCount;                      
	
	public NetworkStats(Network network) {
		this.network = network;
		computeStats();
	}
	
	void computeStats() {
		computeDegree();
		countInteractors();
		countEdges();
	}
	
	void computeDegree() {
		nodeDegrees = network.getData().columnSums();
	}
	
	/*
	 * assumes we've already computed the degree
	 */
	void countInteractors() {
		numInteractingNodes = 0;
		for (int i=0; i<nodeDegrees.getSize(); i++) {
			if (nodeDegrees.get(i) > 0) {
				numInteractingNodes += 1;
			}
		}
	}
	
	/*
	 * our network is symmetric and without
	 * self interactions, we only need count
	 * the number of non-zeros in the lower (or upper)
	 * triangle.
	 * 
	 * keeps a count of the total # of edges incident on
	 * each node, as well as the total
	 */
	void countEdges() {
		MatrixCursor cursor = network.getData().cursor();
		numEdges = 0;
		
		int numNodes = network.getData().numCols();
		nodeInteractorsCount = new int[numNodes];
		
		while (cursor.next()) {
			if (cursor.val() != 0 && cursor.col() < cursor.row()) {
				nodeInteractorsCount[cursor.col()] += 1;
				nodeInteractorsCount[cursor.row()] += 1;
				numEdges += 1;
			}
		}		
	}

	public Vector getNodeDegrees() {
		return nodeDegrees;
	}

	public int getNumEdges() {
		return numEdges;
	}

	public int getNumInteractingNodes() {
		return numInteractingNodes;
	}

	public int[] getNodeInteractorsCount() {
		return nodeInteractorsCount;
	}
	
}
