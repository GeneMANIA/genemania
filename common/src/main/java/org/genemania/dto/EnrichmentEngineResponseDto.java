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

package org.genemania.dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;


public class EnrichmentEngineResponseDto implements Serializable {
	
	// __[static]______________________________________________________________
	private static final long serialVersionUID = 6472963109149340397L;
	
	// __[attributes]__________________________________________________________
	Collection<OntologyCategoryDto> enrichedCategories;
	Map<Long, Collection<OntologyCategoryDto>> annotations;  // maps Node ID -> Collection<Category ID>

	// __[constructors]________________________________________________________
	public EnrichmentEngineResponseDto() {
	}
	
	// __[accessors]_______
        @Deprecated
	public Collection<OntologyCategoryDto> getEnrichedCategories() {
		return enrichedCategories;
	}

        @Deprecated
	public void setEnrichedCategories(Collection<OntologyCategoryDto> enrichedCategories) {
		this.enrichedCategories = enrichedCategories;
	}

        public Map<Long, Collection<OntologyCategoryDto>> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(Map<Long, Collection<OntologyCategoryDto>> annotations) {
		this.annotations = annotations;
	}
	
}
