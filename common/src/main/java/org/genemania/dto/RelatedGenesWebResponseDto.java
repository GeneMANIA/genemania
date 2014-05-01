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
 * RelatedGenesWebResponseDto: Website-specific Related Genes request data transfer object   
 * Created Jul 22, 2009
 * @author Ovi Comes
 */
package org.genemania.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.OntologyCategory;
import org.genemania.type.CombiningMethod;

public class RelatedGenesWebResponseDto implements Serializable {

    // __[static]______________________________________________________________
    private static final long serialVersionUID = -6530663764531226940L;

    // __[attributes]__________________________________________________________
    private long organismId;
    private List<InteractionNetwork> networks = new ArrayList<InteractionNetwork>();
    private Map<Long, Double> networkWeightsMap = new HashMap<Long, Double>();
    private Map<Long, Double> nodeScoresMap = new HashMap<Long, Double>();
    private Map<Long, Collection<OntologyCategory>> annotations = new HashMap<Long, Collection<OntologyCategory>>();
    private Map<Long, OntologyCategoryDto> ontologyCategories = new HashMap<Long, OntologyCategoryDto>();
    private Map<Long, Collection<AttributeDto>> attributes = new HashMap<Long, Collection<AttributeDto>>();
    private CombiningMethod combiningMethod;

    // __[constructors]________________________________________________________
    public RelatedGenesWebResponseDto() {
    }

    // __[accessors]___________________________________________________________
    public long getOrganismId() {
        return organismId;
    }

    public void setOrganismId(long organismId) {
        this.organismId = organismId;
    }

    public List<InteractionNetwork> getNetworks() {
        return networks;
    }

    public void setNetworks(List<InteractionNetwork> networks) {
        this.networks = networks;
    }

    public Map<Long, Double> getNetworkWeightsMap() {
        return networkWeightsMap;
    }

    public void setNetworkWeightsMap(Map<Long, Double> networkWeightsMap) {
        this.networkWeightsMap = networkWeightsMap;
    }

    public Map<Long, Double> getNodeScoresMap() {
        return nodeScoresMap;
    }

    public void setNodeScoresMap(Map<Long, Double> nodeScoresMap) {
        this.nodeScoresMap = nodeScoresMap;
    }

    public Map<Long, Collection<OntologyCategory>> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(
            Map<Long, Collection<OntologyCategory>> annotations) {
        this.annotations = annotations;
    }

    public Map<Long, OntologyCategoryDto> getOntologyCategories() {
        return ontologyCategories;
    }

    public void setOntologyCategories(
            Map<Long, OntologyCategoryDto> ontologyCategories) {
        this.ontologyCategories = ontologyCategories;
    }

    public CombiningMethod getCombiningMethod() {
        return combiningMethod;
    }

    public void setCombiningMethod(CombiningMethod combiningMethod) {
        this.combiningMethod = combiningMethod;
    }

    public void setAttributes(Map<Long, Collection<AttributeDto>> attributes) {
        this.attributes = attributes;
    }

    public Map<Long, Collection<AttributeDto>> getAttributes() {
        return attributes;
    }

}
