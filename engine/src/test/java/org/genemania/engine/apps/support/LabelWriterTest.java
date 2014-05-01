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

import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import org.genemania.domain.Gene;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.Node;
import org.genemania.engine.apps.support.LabelWriter.LabelResult;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.mediator.NodeMediator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LabelWriterTest {
	private LabelWriter writer;
	private long organismId;

	@Before
	public void setUp() {
		organismId = 1;
		NodeMediator mediator = new NodeMediator() {
			@Override
			public Node getNode(long nodeId, long organismId) {
				Node node = new Node();
				node.setId(nodeId);
				List<Gene> genes = new ArrayList<Gene>(1);
				
				Gene gene = new Gene();
				gene.setId(nodeId);
				gene.setSymbol(String.valueOf(nodeId));
				genes.add(gene);
				gene.setNamingSource(new GeneNamingSource());
				
				node.setGenes(genes);
				return node;
			}
		};
		writer = new LabelWriter(null, mediator, organismId);
	}
	
	@Test
	public void testSort() throws Exception {
		Vector label = new DenseVector(new double[] { 0, 1, 0, 1 });
		Vector discriminant = new DenseVector(new double[] { 2, 1, 1, 0 });
		List<Integer> labelIndices = new ArrayList<Integer>();
		labelIndices.add(3);
		labelIndices.add(1);
		labelIndices.add(2);
		labelIndices.add(0);
		
		NodeIds nodeIds = new NodeIds(organismId);
		nodeIds.setNodeIds(new long[] { 0, 1, 2, 3 });
		List<LabelResult> results = writer.sortResults(label, discriminant, labelIndices, nodeIds);
		Assert.assertEquals("3", results.get(0).getName());
		Assert.assertEquals("2", results.get(1).getName());
		Assert.assertEquals("1", results.get(2).getName());
		Assert.assertEquals("0", results.get(3).getName());
	}
}
