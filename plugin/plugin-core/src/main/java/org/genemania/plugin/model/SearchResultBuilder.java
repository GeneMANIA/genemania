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
import java.util.Map;

import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Gene;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.type.CombiningMethod;

public interface SearchResultBuilder extends SearchResult {

	SearchResult build();

	void setOrganism(Organism organism);

	void setSearchQuery(Map<Long, Gene> queryGenes);

	void setNetworkWeights(Map<InteractionNetwork, Double> networkWeights);

	void setGeneScores(Map<Gene, Double> geneScores);

	void setGroups(Map<Long, InteractionNetworkGroup> groupsByNetwork);

	void setEnrichment(Map<Long, Collection<AnnotationEntry>> enrichment);

	void setAttributes(Map<Long, Collection<Attribute>> attributesByNode);

	void setGroupsByAttribute(Map<Long, AttributeGroup> groupsByAttribute);

	void setCombiningMethod(CombiningMethod combiningMethod);

	void setGeneSearchLimit(int limit);

	void setAttributeSearchLimit(int limit);

	void setAttributeWeights(Map<Attribute, Double> weights);

}
