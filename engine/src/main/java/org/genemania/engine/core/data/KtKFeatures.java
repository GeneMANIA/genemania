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

import org.genemania.engine.core.integration.FeatureList;
/*
 * 
 */
public class KtKFeatures extends Data {
    private static final long serialVersionUID = -4017577380988476582L;
    private FeatureList features;
    
    public KtKFeatures(String namespace, long organismId) {
        super(namespace, organismId);
    }

    @Override
    public String [] getKey() {
        return new String [] {getNamespace(), "" + getOrganismId(), "KtK_Features"};
    }

    public void setFeatures(FeatureList features) {
        this.features = features;
    }

    public FeatureList getFeatures() {
        return features;
    }
}
