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

import java.io.Serializable;

import org.genemania.domain.OntologyCategory;
import org.genemania.dto.OntologyCategoryDto;

import com.fasterxml.jackson.databind.JsonNode;

public class AnnotationEntry implements Serializable {
	
	private static final long serialVersionUID = -1170134838167969091L;
	
	private final String name;
	private final String description;
	private final double qValue;
	private final int sampleOccurrences;
	private final int totalOccurrences;

	public AnnotationEntry(OntologyCategory category, OntologyCategoryDto categoryVo) {
		name = category.getName();
		description = category.getDescription();
		qValue = categoryVo.getqValue();
		sampleOccurrences = categoryVo.getNumAnnotatedInSample();
		totalOccurrences = categoryVo.getNumAnnotatedInTotal();
	}
	
	public AnnotationEntry(JsonNode node) {
		name = node.get("name").textValue(); //$NON-NLS-1$
		description = node.get("description").textValue(); //$NON-NLS-1$
		qValue = node.get("qValue").doubleValue(); //$NON-NLS-1$
		sampleOccurrences = node.get("sample").intValue(); //$NON-NLS-1$
		totalOccurrences = node.get("total").intValue(); //$NON-NLS-1$
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public double getQValue() {
		return qValue;
	}
	
	public int getSampleOccurrences() {
		return sampleOccurrences;
	}
	
	public int getTotalOccurrences() {
		return totalOccurrences;
	}
}
