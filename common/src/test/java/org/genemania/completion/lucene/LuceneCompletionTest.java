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

package org.genemania.completion.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.genemania.domain.Gene;
import org.genemania.domain.GeneData;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.mediator.GeneMediator;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;
import org.junit.Before;
import org.junit.Test;

public class LuceneCompletionTest {
	private List<Gene> genes;
	private Map<Long, Node> nodes;
	private GeneMediator mediator;

	@Before
	public void init() {
		nodes = new HashMap<Long, Node>();
		genes = createGenes(new String[] {
			"gene1", "gene12", "gene20", "foo"
		});
		
		mediator = new GeneMediator() {
			public GeneNamingSource findNamingSourceByName(String namingSourceName) {
				return null;
			}

			public List<Gene> getAllGenes(long organismId) {
				return genes;
			}

			public Gene getGeneForSymbol(Organism organism, String geneSymbol) {
				return null;
			}

			public List<Gene> getGenes(List<String> geneSymbols, long organismId) {
				return null;
			}

			public void updateGeneData(Organism organism, String geneSymbol, GeneData geneData) {
			}

			public String getCanonicalSymbol(long organismId, String symbol) {
				return null;
			}

			public Long getNodeId(long organismId, String symbol) {
				return null;
			}

			public Set<String> getSynonyms(long organismId, String symbol) {
				return null;
			}

			public Set<String> getSynonyms(long organismId, long nodeId) {
				return null;
			}

			public boolean isValid(long organismId, String symbol) {
				return false;
			}
		};
 
	}
	
	@SuppressWarnings("unchecked")
	private List<Gene> createGenes(String[] names) {
		List<Gene> result = new ArrayList<Gene>();
		for (String name : names) {
			// First character is the node id.
			// Genes with the same first character belong to the
			// same node.
			long id = name.codePointAt(0);
			Node node = nodes.get(id);
			if (node == null) {
				node = new Node();
				node.setId(id);
				nodes.put(id, node);
				node.setGenes(new HashSet<Gene>());
			}
			
			Gene gene = new Gene();
			gene.setSymbol(name);
			gene.setNode(node);
			result.add(gene);
			
			Collection<Gene> allGenes = node.getGenes();
			allGenes.add(gene);
		}
		return result;
	}

	@Test
	public void testIndex() throws Exception {
		File indexFolder = File.createTempFile("genemania", "test");
		indexFolder.delete();
		try {
			String path = indexFolder.getCanonicalPath();
			GeneIndexBuilder builder = new GeneIndexBuilder(path, mediator);
			
			Organism organism = new Organism();
			ProgressReporter progress = NullProgressReporter.instance();
			GeneCompletionProvider provider = builder.createCompletionProvider(organism, progress, true);
			
			SetBasedConsumer consumer = new SetBasedConsumer();
			provider.computeProposals(consumer, "gene1");
			Set<String> completions = consumer.getCompletions();
			
			assertTrue(completions.contains("gene1"));
			assertTrue(completions.contains("gene12"));
			assertTrue(!completions.contains("foo"));
			assertTrue(!completions.contains("gene20"));
			
			assertEquals("gene20", provider.getCanonicalForm("gEnE20"));
			assertTrue(provider.isValid("foo"));
			assertTrue(provider.isValid("gene1"));
			assertTrue(!provider.isValid("bar"));
		} finally {
			delete(indexFolder);
		}
	}
	
	@Test
	public void testSynonyms() throws Exception {
		File indexFolder = File.createTempFile("genemania", "test");
		indexFolder.delete();
		try {
			String path = indexFolder.getCanonicalPath();
			GeneIndexBuilder builder = new GeneIndexBuilder(path, mediator);
			
			Organism organism = new Organism();
			ProgressReporter progress = NullProgressReporter.instance();
			GeneCompletionProvider provider = builder.createCompletionProvider(organism, progress, true);
			
			Set<String> synonyms = provider.getSynonyms("gene1");
			assertTrue(synonyms.contains("gene1"));
			assertTrue(synonyms.contains("gene12"));
			assertTrue(synonyms.contains("gene20"));
			assertTrue(!synonyms.contains("foo"));
			assertTrue(!synonyms.contains("random"));
		} finally {
			delete(indexFolder);
		}
	}
	
	public static void delete(File path) throws IOException {
		if (path.isFile()) {
			path.delete();
			return;
		}
		
		if (path.isDirectory()) {
			for (File file : path.listFiles()) {
				delete(file);
			}
			path.delete();
		}
	}


}
