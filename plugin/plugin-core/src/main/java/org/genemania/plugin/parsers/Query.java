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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Organism;
import org.genemania.plugin.model.Group;
import org.genemania.type.CombiningMethod;
import org.genemania.type.ScoringMethod;

public final class Query {
	
	private Organism organism;
	private List<String> genes;
	private Collection<Group<?, ?>> networks;
	private int geneLimit;
	private CombiningMethod combiningMethod;
	private ScoringMethod scoringMethod;
	private Collection<Long> nodes;
	private Collection<AttributeGroup> attributes;
	private int attributeLimit;
	
	public Organism getOrganism() {
		return organism;
	}
	
	public List<String> getGenes() {
		return genes;
	}
	
	public Collection<Group<?, ?>> getNetworks() {
		return networks;
	}
	
	public Collection<AttributeGroup> getAttributes() {
		return attributes;
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
	
	public void setNetworks(Collection<Group<?, ?>> groups) {
		this.networks = groups;
	}
	
	public void setAttributes(Collection<AttributeGroup> attributes) {
		this.attributes = attributes;
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
	
	public String toJson() throws IOException {
		StringWriter writer = new StringWriter();
		JsonFactory jsonFactory = new MappingJsonFactory();
		
		try {
			JsonGenerator generator = jsonFactory.createJsonGenerator(writer);
			generator.writeStartObject();
			
			try {
			} finally {
				generator.writeEndObject();
			}
			
			return writer.toString();
		} catch (JsonGenerationException e) {
			throw new IOException(e.getMessage());
		}
	}
}
