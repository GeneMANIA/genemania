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


import static org.junit.Assert.*;

import no.uib.cipr.matrix.DenseVector;

import org.genemania.engine.cache.NetworkMemCache;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.cache.RandomDataCacheConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CorrelatedAttributeSelectorTest {
    static RandomDataCacheBuilder cacheBuilder;
    RandomDataCacheConfig cacheConfig = RandomDataCacheConfig.getStandardConfig2();
    
    @Before
    public void setUp() throws Exception {
        cacheBuilder = new RandomDataCacheBuilder(cacheConfig.getSeed());
        cacheBuilder.setUp();
        cacheBuilder.addOrganism(cacheConfig);
        
        NetworkMemCache.instance().clear();    
    }

    @After
    public void tearDown() throws Exception {
        cacheBuilder.tearDown();
    }

    @Test
    public void testSelectAttributes() throws Exception {
        int maxAttributes = 3;
        DenseVector target = new DenseVector(cacheConfig.getOrg1numGenes());
        CorrelatedAttributeSelector selector = new CorrelatedAttributeSelector(cacheBuilder.getCache(), "BP", maxAttributes);
        FeatureList featureList = selector.selectAttributes(cacheConfig.getOrg1Id(), cacheConfig.getAttributeGroupId()[0]);
        
        assertNotNull(featureList);
        //assertTrue("too many attributes returned", featureList.size() <= maxAttributes);
    }
}
