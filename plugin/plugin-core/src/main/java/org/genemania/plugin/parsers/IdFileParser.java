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
package org.genemania.plugin.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.genemania.domain.Gene;
import org.genemania.domain.GeneData;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.exception.ApplicationException;
import org.genemania.plugin.Strings;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.IModelWriter;
import org.genemania.plugin.data.Namespace;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;

public class IdFileParser {

	private static final String USER_NAMING_SOURCE = "user"; //$NON-NLS-1$

	private final DataSet fData;
	private final Namespace fNamespace;
	
	public IdFileParser(DataSet data, Namespace namespace) {
		fData = data;
		fNamespace = namespace;
	}
	
	public Set<Node> parseNodes(Reader source, Organism organism, ProgressReporter progress) throws IOException, ApplicationException {
		List<GeneNamingSource> sources = fData.getAllNamingSources();
		GeneNamingSource userNamingSource = getUserNamingSource(sources);
		GeneNamingSource synonymNamingSource = getSynonymNamingSource(sources);
		
		BufferedReader reader = new BufferedReader(source);
		String line = reader.readLine();
		long nodeId = 1;
		long geneDataId = fData.getNextAvailableId(GeneData.class, fNamespace);
		long geneId = fData.getNextAvailableId(Gene.class, fNamespace);
		
		Set<Node> nodes = new HashSet<Node>();
		while (line != null) {
			String[] parts = line.split("\t"); //$NON-NLS-1$
			Node node = new Node();
			node.setId(nodeId);
			node.setName(parts[0]);
			
			Set<Gene> genes = new HashSet<Gene>();
			node.setGenes(genes);
			
			boolean first = true;
			for (String symbol : parts) {
				Gene gene = new Gene();
				if (first) {
					first = false;
					gene.setNamingSource(userNamingSource);
				} else {
					gene.setNamingSource(synonymNamingSource);
				}
				gene.setId(geneId);
				gene.setSymbol(symbol);
				gene.setNode(node);
				gene.setOrganism(organism);
				genes.add(gene);
				
				if (fNamespace == Namespace.USER) {
					geneId--;
				} else {
					geneId++;
				}
			}
			
			GeneData data = new GeneData();
			data.setId(geneDataId);
			data.setDescription(parts[0]);
			
			node.setGeneData(data);
			
			nodes.add(node);
			progress.setDescription(String.format(Strings.idFileParser_status, nodes.size()));
			nodeId++;
			
			if (fNamespace == Namespace.USER) {
				geneDataId--;
			} else {
				geneDataId++;
			}
			line = reader.readLine();
		}
		return nodes;
	}
	
	private GeneNamingSource getUserNamingSource(List<GeneNamingSource> sources) throws ApplicationException, IOException {
		for (GeneNamingSource source : sources) {
			if (source.getShortName().equals("user")) { //$NON-NLS-1$
				return source;
			}
		}
		return createUserNamingSource();
	}

	private GeneNamingSource getSynonymNamingSource(List<GeneNamingSource> sources) {
		for (GeneNamingSource source : sources) {
			if (source.getShortName().equalsIgnoreCase("synonym")) { //$NON-NLS-1$
				return source;
			}
		}
		return null;
	}
	
	private GeneNamingSource createUserNamingSource() throws ApplicationException, IOException {
		GeneNamingSource source = new GeneNamingSource();
		source.setId(fData.getNextAvailableId(GeneNamingSource.class, fNamespace));
		source.setName("user"); //$NON-NLS-1$
		source.setRank(Byte.MAX_VALUE);
		source.setShortName(USER_NAMING_SOURCE);
		
		IModelWriter writer = fData.createModelWriter();
		try {
			writer.addNamingSource(source);
		} finally {
			writer.close();
		}
		fData.reload(NullProgressReporter.instance());
		return source;
	}
	
	public Collection<Long> extractNodeIds(Set<Node> nodes) {
		Set<Long> ids = new HashSet<Long>();
		for (Node node : nodes) {
			ids.add(node.getId());
		}
		return ids;
	}
}
