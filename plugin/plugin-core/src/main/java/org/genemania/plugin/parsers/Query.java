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
	private Organism fOrganism;
	private List<String> fGenes;
	private Collection<Group<?, ?>> fNetworks;
	private int fGeneLimit;
	private CombiningMethod fCombiningMethod;
	private ScoringMethod fScoringMethod;
	private Collection<Long> fNodes;
	private Collection<AttributeGroup> fAttributes;
	private int fAttributeLimit;
	
	public Organism getOrganism() {
		return fOrganism;
	}
	
	public List<String> getGenes() {
		return fGenes;
	}
	
	public Collection<Group<?, ?>> getNetworks() {
		return fNetworks;
	}
	
	public Collection<AttributeGroup> getAttributes() {
		return fAttributes;
	}
	
	public int getGeneLimit() {
		return fGeneLimit;
	}
	
	public int getAttributeLimit() {
		return fAttributeLimit;
	}
	
	public CombiningMethod getCombiningMethod() {
		return fCombiningMethod;
	}
	
	public ScoringMethod getScoringMethod() {
		return fScoringMethod;
	}
	
	public Collection<Long> getNodes() {
		return fNodes;
	}
	
	public void setOrganism(Organism organism) {
		fOrganism = organism;
	}
	
	public void setGenes(List<String> genes) {
		fGenes = genes;
	}
	
	public void setNetworks(Collection<Group<?, ?>> groups) {
		fNetworks = groups;
	}
	
	public void setAttributes(Collection<AttributeGroup> attributes) {
		fAttributes = attributes;
	}
	
	public void setGeneLimit(int limit) {
		fGeneLimit = limit;
	}
	
	public void setAttributeLimit(int limit) {
		fAttributeLimit = limit;
	}
	
	public void setCombiningMethod(CombiningMethod method) {
		fCombiningMethod = method;
	}
	
	public void setScoringMethod(ScoringMethod method) {
		fScoringMethod = method;
	}
	
	public void setNodes(Collection<Long> nodes) {
		fNodes = nodes;
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
