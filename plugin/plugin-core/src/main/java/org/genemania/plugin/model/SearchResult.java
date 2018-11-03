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
package org.genemania.plugin.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Gene;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkById;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.InteractionNetworkGroupById;
import org.genemania.domain.Organism;
import org.genemania.type.CombiningMethod;

public interface SearchResult {

	int getGeneSearchLimit();

	int getAttributeSearchLimit();

	CombiningMethod getCombiningMethod();

	Organism getOrganism();

	Map<InteractionNetwork, Double> getNetworkWeights();

	Map<Long, Collection<Attribute>> getAttributesByNodeId();

	Collection<Attribute> getAttributes(long nodeId);

	AttributeGroup getAttributeGroup(long attributeId);

	List<AnnotationEntry> getEnrichmentSummary();

	Map<Long, Gene> getQueryGenes();
	
	InteractionNetworkGroup getInteractionNetworkGroup(long networkId);
	
	Map<Long, InteractionNetworkGroup> getInteractionNetworkGroups();
	
	InteractionNetworkGroupById getInteractionNetworkGroupById(long networkId);

	Map<Long, InteractionNetworkGroupById> getInteractionNetworkGroupsById();

	boolean isQueryNode(long nodeId);

	Gene getGene(long nodeId);

	Map<Gene, Double> getScores();

	Collection<Gene> getNodesByAnnotation(String categoryName);

	Collection<AnnotationEntry> getAnnotations(long nodeId);

	Map<Attribute, Double> getAttributeWeights();

}
