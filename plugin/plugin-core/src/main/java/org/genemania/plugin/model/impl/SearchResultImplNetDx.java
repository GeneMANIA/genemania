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
import org.genemania.domain.InteractionNetworkById;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.InteractionNetworkGroupById;
import org.genemania.domain.Organism;
import org.genemania.plugin.model.AnnotationEntry;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.SearchResultBuilder;
import org.genemania.type.CombiningMethod;

//public class SearchResultImplNetDx extends SearchResultImpl implements SearchResultBuilder, Serializable {
public class SearchResultImplNetDx implements Serializable {
	
	private static final long serialVersionUID = -5723132964169233142L;
	
	private Organism organism;
	private Map<Long, Gene> queryGenes;
//	private Map<Long, InteractionNetworkGroup> groupsByNetwork;
//	private Map<Long, InteractionNetworkGroupById> groupsByNetworkById;
//	private Map<Long, AttributeGroup> groupsByAttribute;
//	
//	private Map<InteractionNetwork, Double> networkWeights;
//	private Map<InteractionNetworkById, Double> networkWeightsById;
//	private Map<Attribute, Double> attributeWeights;
	private Map<Gene, Double> geneScores;
	
	private Map<String, Double> parsedNetworkWeights;
//	private Map<Long, Collection<Attribute>> attributesByNode;
//	private Map<Long, Collection<AnnotationEntry>> annotationsByNode;
//	private Map<String, Collection<Gene>> genesByAnnotation;
//	private Map<String, AnnotationEntry> categories;
	private Map<Long, Gene> genesByNodeId;
//	private Map<String, InteractionNetworkGroup> groupsByName;
//	private Map<String, InteractionNetworkGroupById> groupsByNameById;
//	private int geneSearchLimit;
//	private int attributeSearchLimit;
	private CombiningMethod combiningMethod;

	public SearchResultImplNetDx() {
//		categories = Collections.emptyMap();
//		genesByAnnotation = Collections.emptyMap();
//		annotationsByNode = Collections.emptyMap();
//		attributesByNode = Collections.emptyMap();
//		groupsByAttribute = Collections.emptyMap();
	}
//	
//	public Map<Long, InteractionNetworkGroupById> getGroupsByNetworkById() {
//		return this.groupsByNetworkById;
//	}
//	
//	public Map<String, InteractionNetworkGroupById> getGroupsByNameById() {
//		return this.groupsByNameById;
//	}
//	
//	//@Override
//	public int getGeneSearchLimit() {
//		return geneSearchLimit;
//	}
//	
//	//@Override
//	public int getAttributeSearchLimit() {
//		return attributeSearchLimit;
//	}
//	
//	//@Override
	public CombiningMethod getCombiningMethod() {
		return combiningMethod;
	}
	
//	//@Override
	public Organism getOrganism() {
		return organism;
	}
//	
//	//@Override
//	public Map<InteractionNetwork, Double> getNetworkWeights() {
//		return networkWeights;
//	}
	
//	//@Override
	public Map<String, Double> getParsedNetworkWeights() {
		return this.parsedNetworkWeights;
	}

//	//@Override
	public void setParsedNetworkWeights(Map<String, Double> map) {
		this.parsedNetworkWeights = map;
	}
//	
////	//@Override
//	public Map<InteractionNetworkById, Double> getNetworkWeightsById() {
//		return networkWeightsById;
//	}
//
//	//@Override
//	public Map<Attribute, Double> getAttributeWeights() {
//		return attributeWeights;
//	}
//	
//	//@Override
//	public Map<Long, Collection<Attribute>> getAttributesByNodeId() {
//		return attributesByNode;
//	}
//	
//	//@Override
//	public Collection<Attribute> getAttributes(long nodeId) {
//		return attributesByNode.get(nodeId);
//	}
//
//	//@Override
//	public AttributeGroup getAttributeGroup(long attributeId) {
//		return groupsByAttribute.get(attributeId);
//	}
//	
//	//@Override
//	public List<AnnotationEntry> getEnrichmentSummary() {
//		List<AnnotationEntry> result = new ArrayList<AnnotationEntry>(categories.values());
//		Collections.sort(result, (AnnotationEntry o1, AnnotationEntry o2) -> {
//			return Double.compare(o1.getQValue(), o2.getQValue());
//		});
//		
//		return result;
//	}
	
	//@Override
	public Map<Long, Gene> getQueryGenes() {
		return queryGenes;
	}

//	//@Override
//	public InteractionNetworkGroup getInteractionNetworkGroup(long networkId) {
//		return groupsByNetwork.get(networkId);
//	}
//	
//	//@Override
//	public InteractionNetworkGroupById getInteractionNetworkGroupById(long networkId) {
//		return groupsByNameById.get(networkId);
//	}
//	
//	//@Override
//	public Map<Long, InteractionNetworkGroup> getInteractionNetworkGroups() {
//		return groupsByNetwork;
//	}
//	
//	//@Override
//	public Map<Long, InteractionNetworkGroupById> getInteractionNetworkGroupsById() {
//		return groupsByNetworkById;
//	}

	//@Override
	public boolean isQueryNode(long nodeId) {
		return queryGenes.containsKey(nodeId);
	}
	
//	//@Override
	public Gene getGene(long nodeId) {
		return genesByNodeId.get(nodeId);
	}

	//@Override
	public Map<Gene, Double> getScores() {
		return geneScores;
	}
	
//	//@Override
//	public Collection<Gene> getNodesByAnnotation(String categoryName) {
//		return genesByAnnotation.get(categoryName);
//	}
	
//	//@Override
//	public Collection<AnnotationEntry> getAnnotations(long nodeId) {
//		return annotationsByNode.get(nodeId);
//	}
	
	//@Override
	public SearchResultImplNetDx build() {
		return this;
	}
	
	//@Override
	public void setOrganism(Organism organism) {
		this.organism = organism;
	}
	
	//@Override
	public void setSearchQuery(Map<Long, Gene> queryGenes) {
		this.queryGenes = queryGenes;
	}
	
//	//@Override
//	public void setNetworkWeights(Map<InteractionNetwork, Double> networkWeights) {
//		this.networkWeights = networkWeights;
//	}
	
//	//@Override
//	public void setNetworkWeightsById(Map<InteractionNetworkById, Double> networkWeights) {
//		this.networkWeightsById = networkWeights;
////		this.networkWeights = networkWeights;
//	}

	//@Override
	public void setGeneScores(Map<Gene, Double> geneScores) {
		this.geneScores = geneScores;
		this.genesByNodeId = new HashMap<Long, Gene>();
		for (Gene gene : geneScores.keySet()) {
			genesByNodeId.put(gene.getNode().getId(), gene);
		}
	}
	
	//@Override
//	public void setGroups(Map<Long, InteractionNetworkGroup> groupsByNetwork) {
//		this.groupsByNetwork = groupsByNetwork;
//		groupsByName = new HashMap<String, InteractionNetworkGroup>();
//		for (InteractionNetworkGroup group : groupsByNetwork.values()) {
//			groupsByName.put(group.getName(), group);
//		}
//	}
	
//	//@Override
//	public void setGroupsById(Map<Long, InteractionNetworkGroupById> groupsByNetworkById) {
//		this.groupsByNetworkById = groupsByNetworkById;
//		this.groupsByNameById = new HashMap<String, InteractionNetworkGroupById>();
//		for (InteractionNetworkGroupById group : groupsByNetworkById.values()) {
//			groupsByNameById.put(group.getName(), group);
//		}
//	}

	//@Override
//	public void setEnrichment(Map<Long, Collection<AnnotationEntry>> enrichment) {
//		this.annotationsByNode = enrichment;
//		categories  = new HashMap<String, AnnotationEntry>();
//		genesByAnnotation = new HashMap<String, Collection<Gene>>();
//
//		for (Entry<Long, Collection<AnnotationEntry>> entry : enrichment.entrySet()) {
//			long nodeId = entry.getKey();
//			Collection<AnnotationEntry> annotations = entry.getValue();
//			for (AnnotationEntry annotation : annotations) {
//				String name = annotation.getName();
//				categories.put(name, annotation);
//				
//				Collection<Gene> genes = genesByAnnotation.get(name);
//				if (genes == null) {
//					genes = new HashSet<Gene>();
//					genesByAnnotation.put(name, genes);
//				}
//				
//				Gene gene = genesByNodeId.get(nodeId);
//				genes.add(gene);
//			}
//		}
//	}
	
//	//@Override
//	public void setAttributes(Map<Long, Collection<Attribute>> attributesByNode) {
//		this.attributesByNode = attributesByNode;
//	}
//	
//	//@Override
//	public void setGroupsByAttribute(Map<Long, AttributeGroup> groupsByAttribute) {
//		this.groupsByAttribute = groupsByAttribute;
//	}
//	
	//@Override
	public void setCombiningMethod(CombiningMethod combiningMethod) {
		this.combiningMethod = combiningMethod;
	}
//	
//	//@Override
//	public void setGeneSearchLimit(int geneSearchLimit) {
//		this.geneSearchLimit = geneSearchLimit;
//	}
//	
//	//@Override
//	public void setAttributeSearchLimit(int limit) {
//		attributeSearchLimit = limit;
//	}
//	
//	//@Override
//	public void setAttributeWeights(Map<Attribute, Double> weights) {
//		attributeWeights = weights;
//	}
}
