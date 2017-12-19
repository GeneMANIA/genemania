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

import java.util.Collection;
import java.util.List;

import org.genemania.domain.Organism;
import org.genemania.plugin.model.Group;
import org.genemania.type.CombiningMethod;
import org.genemania.type.ScoringMethod;

public final class Query {
	
	private Organism organism;
	private List<String> genes;
	private Collection<Group<?, ?>> groups;
	private int geneLimit;
	private CombiningMethod combiningMethod;
	private ScoringMethod scoringMethod;
	private Collection<Long> nodes;
	private int attributeLimit;
	
	public Organism getOrganism() {
		return organism;
	}
	
	public List<String> getGenes() {
		return genes;
	}
	
	public Collection<Group<?, ?>> getGroups() {
		return groups;
	}
	
	public int getGeneLimit() {
		return geneLimit;
	}
	
	public int getAttributeLimit() {
		return attributeLimit;
	}
	
	public CombiningMethod getCombiningMethod() {
		return combiningMethod;
	}
	
	public ScoringMethod getScoringMethod() {
		return scoringMethod;
	}
	
	public Collection<Long> getNodes() {
		return nodes;
	}
	
	public void setOrganism(Organism organism) {
		this.organism = organism;
	}
	
	public void setGenes(List<String> genes) {
		this.genes = genes;
	}
	
	public void setGroups(Collection<Group<?, ?>> groups) {
		this.groups = groups;
	}
	
	public void setGeneLimit(int limit) {
		this.geneLimit = limit;
	}
	
	public void setAttributeLimit(int limit) {
		this.attributeLimit = limit;
	}
	
	public void setCombiningMethod(CombiningMethod method) {
		this.combiningMethod = method;
	}
	
	public void setScoringMethod(ScoringMethod method) {
		this.scoringMethod = method;
	}
	
	public void setNodes(Collection<Long> nodes) {
		this.nodes = nodes;
	}
}
