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
package org.genemania.plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.genemania.domain.Gene;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.Node;
import org.genemania.plugin.controllers.RankedGeneProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class RankedGeneProviderTest {
	GeneNamingSource source1;
	GeneNamingSource source2;
	GeneNamingSource source3;
	
	Gene geneA1;
	Gene geneA3;
	Gene geneB2;
	Gene geneB3;
	
	Node nodeA;
	Node nodeB;
	private List<GeneNamingSource> allSources;
	
	@Before
	public void setUp() {
		source1 = new GeneNamingSource("S1", (byte) 1, "S1");
		source1.setId(1);
		source2 = new GeneNamingSource("S2", (byte) 2, "S2");
		source2.setId(2);
		source3 = new GeneNamingSource("S3", (byte) 3, "S3");
		source3.setId(3);

		Set<Gene> genesA = new HashSet<Gene>();
		Set<Gene> genesB = new HashSet<Gene>();
		
		nodeA = new Node("A", genesA, null);
		nodeA.setId(1);
		nodeB = new Node("B", genesB, null);
		nodeB.setId(2);

		geneA1 = new Gene("A1", "", source1, nodeA, null);
		geneA1.setId(1);
		geneA3 = new Gene("A3", "", source3, nodeA, null);
		geneA3.setId(2);
		geneB2 = new Gene("B2", "", source2, nodeB, null);
		geneB2.setId(3);
		geneB3 = new Gene("B3", "", source3, nodeB, null);
		geneB3.setId(4);
		
		genesA.add(geneA1);
		genesA.add(geneA3);
		genesB.add(geneB2);
		genesB.add(geneB3);
		
		allSources = new ArrayList<GeneNamingSource>();
		allSources.add(source1);
		allSources.add(source2);
		allSources.add(source3);
	}
	
	@Test
	public void testRankNoPreference() {
		List<GeneNamingSource> preferences = new ArrayList<GeneNamingSource>();
		RankedGeneProvider provider = new RankedGeneProvider(allSources, preferences);
		Assert.assertEquals(geneA3, provider.getGene(nodeA));
		Assert.assertEquals(geneB3, provider.getGene(nodeB));
	}

	@Test
	public void testRank1() {
		List<GeneNamingSource> preferences = new ArrayList<GeneNamingSource>();
		preferences.add(source1);
		preferences.add(source2);
		RankedGeneProvider provider = new RankedGeneProvider(allSources, preferences);
		Assert.assertEquals(geneA1, provider.getGene(nodeA));
		Assert.assertEquals(geneB2, provider.getGene(nodeB));
	}

	@Test
	public void testRank2() {
		List<GeneNamingSource> preferences = new ArrayList<GeneNamingSource>();
		preferences.add(source2);
		preferences.add(source1);
		RankedGeneProvider provider = new RankedGeneProvider(allSources, preferences);
		Assert.assertEquals(geneA1, provider.getGene(nodeA));
		Assert.assertEquals(geneB2, provider.getGene(nodeB));
	}

	@Test
	public void testRank3() {
		List<GeneNamingSource> preferences = new ArrayList<GeneNamingSource>();
		preferences.add(source3);
		RankedGeneProvider provider = new RankedGeneProvider(allSources, preferences);
		Assert.assertEquals(geneA3, provider.getGene(nodeA));
		Assert.assertEquals(geneB3, provider.getGene(nodeB));
	}
}
