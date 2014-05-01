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
import java.util.Map;

import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.data.AttributeData;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.engine.matricks.Vector;
import org.genemania.engine.matricks.custom.DenseDoubleVector;
import org.genemania.engine.matricks.custom.MultiOPCSymMatrix;
import org.genemania.engine.matricks.custom.OuterProductComboSymMatrix;
import org.genemania.exception.ApplicationException;

/* 
 * precombined kernels don't include attributes,
 * which are included on the fly. Build a matrix from
 * a precombined kernel and feature weights that
 * performs the desired computations on the fly
 * 
 * this class needs a better name. and a better implementation.
 * oh i see, it needs a better author.
 */
public class CombinedKernelBuilder {

    private DataCache cache;

    public CombinedKernelBuilder(DataCache cache) {
        this.cache = cache;
    }
    
    public SymMatrix build(long organismId, String namespace, SymMatrix backing, FeatureWeightMap featureWeights) throws ApplicationException {
        
        Map<Long, Collection<Feature>> groupedAttributes = featureWeights.getGroupedAttributes();
        
        ArrayList<OuterProductComboSymMatrix> combos = new ArrayList<OuterProductComboSymMatrix>();
        
        if (groupedAttributes.size() == 0) {
            // no need for wrapping, backing matrix will work by itself
            return backing;
        }
        
        for (Long attributeGroupId: groupedAttributes.keySet()) {
            Collection<Feature> attributes = groupedAttributes.get(attributeGroupId);
            
            Vector weights = buildWeightsForAttributeGroup(organismId, namespace, attributeGroupId, attributes, featureWeights);
         
            AttributeData attributeData = cache.getAttributeData(namespace, organismId, attributeGroupId);
            OuterProductComboSymMatrix matrix = new OuterProductComboSymMatrix(attributeData.getData(), weights, true);
            combos.add(matrix);
        }
        
        // can now build the big wrapper matrix
        MultiOPCSymMatrix result = new MultiOPCSymMatrix(backing, combos.toArray(new OuterProductComboSymMatrix[0]));
        return result;
    }

    /* 
     * in addition to the feature weights, for consistency when comparing weights
     * to other networks, we take into account our usual network normalization,
     * which divides each interaction by the square root of the product of the
     * degrees of the two interacting nodes. For attribute networks this boils
     * down to dividing by (n-1) where n is the sum of attribute weights, and the -1
     * is to remove the weight of the self interaction. Note that this assumes
     * our attributes are binary vectors!
     */
    private Vector buildWeightsForAttributeGroup(long organismId, String namespace, 
            long attributeGroupId, Collection<Feature> attributes, FeatureWeightMap featureWeights) 
    throws ApplicationException {
        
        AttributeData attributeData = cache.getAttributeData(namespace, organismId, attributeGroupId);
        Vector sums = attributeData.getData().columnSums();
        
        AttributeGroups attributeGroups = cache.getAttributeGroups(namespace, organismId);
        ArrayList<Long> allAttributes = attributeGroups.getAttributesForGroup(attributeGroupId);
        
        Vector weights = new DenseDoubleVector(allAttributes.size());
        
        for (Feature feature: attributes) {
            int index = attributeGroups.getIndexForAttributeId(attributeGroupId, feature.getId());
            double weight = featureWeights.get(feature)/(sums.get(index)-1);            
            weights.set(index, weight);
        }
        
        return weights;
    }
}
