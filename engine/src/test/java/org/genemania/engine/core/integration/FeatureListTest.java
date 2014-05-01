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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import org.genemania.engine.Constants.NetworkType;
import org.genemania.exception.ApplicationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FeatureListTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws Exception {
        Feature f1 = new Feature(NetworkType.ATTRIBUTE_VECTOR, 1, 10);
        Feature f2 = new Feature(NetworkType.SPARSE_MATRIX, 1, 10);
        
        FeatureList list = new FeatureList();
        list.add(f1);
        list.add(f2);
                
        // no dups, no exception
        list.validate();
        
        // check sort 
        Collections.sort(list);
        Iterator<Feature> iter = list.iterator();
        
        assertEquals(NetworkType.SPARSE_MATRIX, iter.next().getType());
        assertEquals(NetworkType.ATTRIBUTE_VECTOR, iter.next().getType());
        assertFalse(iter.hasNext());
        
        // stuff into a hashset
        HashSet<Feature> featureSet = new HashSet<Feature>();
        featureSet.addAll(list);
        assertEquals(2, featureSet.size());
        featureSet.addAll(list);
        assertEquals(2, featureSet.size());   
    }
    
    @Test(expected = ApplicationException.class)
    public void testDuplicate() throws Exception {
        Feature f1 = new Feature(NetworkType.ATTRIBUTE_VECTOR, 1, 10);
        Feature f2 = new Feature(NetworkType.ATTRIBUTE_VECTOR, 1, 10);
        
        FeatureList list = new FeatureList();
        list.add(f1);
        list.add(f2);
        
        list.validate();       
    }
}
