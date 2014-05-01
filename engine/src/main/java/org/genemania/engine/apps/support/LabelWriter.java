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

package org.genemania.engine.apps.support;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import no.uib.cipr.matrix.Vector;

import org.genemania.domain.Gene;
import org.genemania.domain.Node;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.exception.ApplicationException;
import org.genemania.mediator.NodeMediator;

public class LabelWriter  {
	private String basePath;
	private NodeMediator nodeMediator;
	private long organismId;

	public LabelWriter(String basePath, NodeMediator nodeMediator, long organismId) {
		this.basePath = basePath;
		this.nodeMediator = nodeMediator;
		this.organismId = organismId;
	}
	
	public void write(String queryName, int fold, Vector label, Vector discriminant, Collection<Integer> labelIndices, NodeIds nodeIds) throws ApplicationException {
		String path = String.format("%s-labels-%s-%d", basePath, queryName, fold);
		Writer fileWriter;
		try {
			fileWriter = new FileWriter(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		PrintWriter writer = new PrintWriter(fileWriter);
		try {
			List<LabelResult> results = sortResults(label, discriminant, labelIndices, nodeIds);
			for (LabelResult result : results) {
				writer.printf("%s\t%d\t%f\n", result.getName(), result.getLabel() == 1.0 ? 1 : 0, result.getScore());
			}
		} finally {
			writer.close();
		}
	}
	
	List<LabelResult> sortResults(Vector label, Vector discriminant, Collection<Integer> labelIndices, NodeIds nodeIds) throws ApplicationException {
		List<LabelResult> list = new ArrayList<LabelResult>();
		for (int index : labelIndices) {
			long nodeId = nodeIds.getIdForIndex(index);
			Node node = nodeMediator.getNode(nodeId, organismId);
			Gene gene = getPreferredGene(node);
			list.add(new LabelResult(gene.getSymbol(), label.get(index), discriminant.get(index)));
		}
		Collections.sort(list);
		return list;
	}

	Gene getPreferredGene(Node node) {
		Gene best = null;
		byte bestRank = Byte.MIN_VALUE;
		Collection<Gene> genes = node.getGenes();
		for (Gene gene : genes) {
			byte rank = gene.getNamingSource().getRank();
			if (rank > bestRank) {
				best = gene;
				bestRank = rank;
			}
		}
		return best;
	}

	static class LabelResult implements Comparable<LabelResult> {
		private String name;
		private double label;
		private double score;

		public LabelResult(String name, double label, double score) {
			this.name = name;
			this.label = label;
			this.score = score;
		}

		public String getName() {
			return name;
		}

		public double getScore() {
			return score;
		}

		public double getLabel() {
			return label;
		}
		
		@Override
		public int compareTo(LabelResult other) {
			int result = Double.compare(score, other.score);
			if (result != 0) {
				return result;
			}
			result = Double.compare(label, other.label);
			if (result != 0) {
				return result;
			}
			return name.compareToIgnoreCase(other.name);
		}
	}

}
