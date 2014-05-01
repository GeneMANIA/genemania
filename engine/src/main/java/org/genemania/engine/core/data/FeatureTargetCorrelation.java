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
package org.genemania.engine.core.data;

import java.util.HashMap;

import org.genemania.engine.core.integration.FeatureList;

/*
 * for an attribute group, provides a map from go branch name
 * to an ordered list of features from most to least correlated
 */
public class FeatureTargetCorrelation extends Data {
    private static final long serialVersionUID = -3590365574550975591L;
    private long attributeGroupId;
    private HashMap<String, FeatureList> correlations;
    
    public FeatureTargetCorrelation(String namespace, long organismId, long attributeGroupId) {
        super(namespace, organismId);
        this.setAttributeGroupId(attributeGroupId);
    }

    @Override
    public String [] getKey() {
        return new String [] {getNamespace(), "" + getOrganismId(), "FeatureCorrelations." + getAttributeGroupId()};
    }
    
    public void setAttributeGroupId(long attributeGroupId) {
        this.attributeGroupId = attributeGroupId;
    }

    public long getAttributeGroupId() {
        return attributeGroupId;
    }

    public void setCorrelations(HashMap<String, FeatureList> correlations) {
        this.correlations = correlations;
    }

    public HashMap<String, FeatureList> getCorrelations() {
        return correlations;
    } 
}
