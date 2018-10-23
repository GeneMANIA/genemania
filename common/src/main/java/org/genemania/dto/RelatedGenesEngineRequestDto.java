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

/**
 * RelatedGenesEngineRequestDto: Engine-specific Related Genes request data transfer object   
 * Created Jul 22, 2009
 * @author Khalid Zuberi
 */
package org.genemania.dto;

import java.io.Serializable;
import java.util.Collection;

import org.genemania.type.CombiningMethod;
import org.genemania.type.ScoringMethod;
import org.genemania.util.ProgressReporter;

public class RelatedGenesEngineRequestDto implements Serializable {

	private static final long serialVersionUID = -3694695256917558382L;

	String namespace;
	long organismId;
	Collection<Collection<Long>> interactionNetworks;
	Collection<Long> attributeGroups;
	Collection<Long> positiveNodes;
	int limitResults; // max # of genes to return, badly phrased
	int attributesLimit; // max # of attributes to return
	CombiningMethod combiningMethod;
	ScoringMethod scoringMethod;
	ProgressReporter progressReporter;

	public RelatedGenesEngineRequestDto() {
	}

	public Collection<Long> getAttributeGroups() {
		return attributeGroups;
	}

	public void setAttributeGroups(Collection<Long> attributeGroups) {
		this.attributeGroups = attributeGroups;
	}

	public CombiningMethod getCombiningMethod() {
		return combiningMethod;
	}

	public void setCombiningMethod(CombiningMethod combiningMethod) {
		this.combiningMethod = combiningMethod;
	}

	public int getLimitResults() {
		return limitResults;
	}

	public void setLimitResults(int limitResults) {
		this.limitResults = limitResults;
	}

	public int getAttributesLimit() {
		return attributesLimit;
	}

	public void setAttributesLimit(int attributesLimit) {
		this.attributesLimit = attributesLimit;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public long getOrganismId() {
		return organismId;
	}

	public void setOrganismId(long organismId) {
		this.organismId = organismId;
	}

	public ScoringMethod getScoringMethod() {
		return scoringMethod;
	}

	public void setScoringMethod(ScoringMethod scoringMethod) {
		this.scoringMethod = scoringMethod;
	}

	public Collection<Collection<Long>> getInteractionNetworks() {
		return interactionNetworks;
	}

	public void setInteractionNetworks(Collection<Collection<Long>> interactionNetworks) {
		this.interactionNetworks = interactionNetworks;
	}

	public Collection<Long> getPositiveNodes() {
		return positiveNodes;
	}

	public void setPositiveNodes(Collection<Long> positiveNodes) {
		this.positiveNodes = positiveNodes;
	}

	public ProgressReporter getProgressReporter() {
		return progressReporter;
	}

	public void setProgressReporter(ProgressReporter progressReporter) {
		this.progressReporter = progressReporter;
	}
}
