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
package org.genemania.plugin.model.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Gene;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.plugin.model.AnnotationEntry;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.SearchResultBuilder;
import org.genemania.type.CombiningMethod;

public class SearchResultImplNetDx implements Serializable {
	
	private static final long serialVersionUID = -5723132964169233142L;
	
	private Organism organism;
	private Map<Long, Gene> queryGenes;
	private Map<Gene, Double> geneScores;
	
	private Map<String, Double> parsedNetworkWeights;
	private Map<Long, Gene> genesByNodeId;
	private CombiningMethod combiningMethod;

	public SearchResultImplNetDx() {
	}

	public CombiningMethod getCombiningMethod() {
		return combiningMethod;
	}
	

	public Organism getOrganism() {
		return organism;
	}
	

	public Map<String, Double> getParsedNetworkWeights() {
		return this.parsedNetworkWeights;
	}


	public void setParsedNetworkWeights(Map<String, Double> map) {
		this.parsedNetworkWeights = map;
	}

	public Map<Long, Gene> getQueryGenes() {
		return queryGenes;
	}

	public boolean isQueryNode(long nodeId) {
		return queryGenes.containsKey(nodeId);
	}
	

	public Gene getGene(long nodeId) {
		return genesByNodeId.get(nodeId);
	}

	public Map<Gene, Double> getScores() {
		return geneScores;
	}
	
	
	public SearchResultImplNetDx build() {
		return this;
	}
	
	public void setOrganism(Organism organism) {
		this.organism = organism;
	}
	
	public void setSearchQuery(Map<Long, Gene> queryGenes) {
		this.queryGenes = queryGenes;
	}
	
	public void setGeneScores(Map<Gene, Double> geneScores) {
		this.geneScores = geneScores;
		this.genesByNodeId = new HashMap<Long, Gene>();
		for (Gene gene : geneScores.keySet()) {
			genesByNodeId.put(gene.getNode().getId(), gene);
		}
	}
	
	public void setCombiningMethod(CombiningMethod combiningMethod) {
		this.combiningMethod = combiningMethod;
	}
}
