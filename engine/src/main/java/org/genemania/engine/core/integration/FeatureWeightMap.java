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
package org.genemania.engine.core.integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.genemania.engine.Constants.NetworkType;

/*
 * name the collection type that maintains feature weights
 */
public class FeatureWeightMap extends HashMap<Feature, Double> {
    private static final long serialVersionUID = -1013826142154334182L;
    
    /*
     * convenience accessors
     */
    public double getNetworkWeight(long networkId) {
        return get(new Feature(NetworkType.SPARSE_MATRIX, 0, networkId));
    }
    
    public double getAttributeWeight(long attributeGroupId, long attributeId) {
        return get(new Feature(NetworkType.ATTRIBUTE_VECTOR, attributeGroupId, attributeId));
    }
    
    /*
     * return all attributes by group that appear. might have been
     * better to use a multi-level data structure to group these
     * appropriately instead of having to scan through when needed.
     */
    public Map<Long, Collection<Feature>> getGroupedAttributes() {
        HashMap<Long, Collection<Feature>> result = new HashMap<Long, Collection<Feature>>();
        for (Feature feature: this.keySet()) {
            
            if (feature.getType() == NetworkType.ATTRIBUTE_VECTOR) {
                Collection<Feature> features = result.get(feature.getGroupId());
                if (features == null) {
                    features = new ArrayList<Feature>();
                    result.put(feature.getGroupId(), features);
                }
                features.add(feature);
            }
        }
        
        return result;
    }
}
