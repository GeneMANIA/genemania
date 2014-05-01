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
package org.genemania.engine.core.utils;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ObjectSelectorTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testLt() {
        ObjectSelector<Integer> selector = new ObjectSelector<Integer>();

        selector.add(1, 10.0);
        selector.add(2, 20.0);
 
        ObjectSelector<Integer> result = selector.lt(20.0);
        assertEquals(2, selector.size());
        assertEquals(1, result.size());
        
        assertEquals(1, (int) result.getElement(0));
        assertEquals(10.0d, result.getScore(0), 0d);
    }

    @Test
    public void testSelectLevelledSmallestScores() {
        ObjectSelector<Integer> selector = new ObjectSelector<Integer>();
          
        selector.add(1, 40.0);
        selector.add(2, 20.0);
        selector.add(3, 20.0);
        selector.add(4, 10.0);
 
        ObjectSelector<Integer> result = selector.selectLevelledSmallestScores(0, 10);
        assertEquals(0, result.size());        
        
        result = selector.selectLevelledSmallestScores(1, 10);
        assertEquals(1, result.size());
        assertEquals(4, (int) result.getElement(0));
        assertEquals(10.0d, result.getScore(0), 0d);

        result = selector.selectLevelledSmallestScores(2, 10);
        assertEquals(3, result.size());
        assertEquals(2, (int) result.getElement(1));
        assertEquals(20.0d, result.getScore(1), 0d);

        result = selector.selectLevelledSmallestScores(3, 10);
        assertEquals(3, result.size());
        assertEquals(3, (int) result.getElement(2));
        assertEquals(20.0d, result.getScore(2), 0d);

        result = selector.selectLevelledSmallestScores(4, 10);
        assertEquals(4, result.size());
        assertEquals(1, (int) result.getElement(3)); // last element
        assertEquals(40.0d, result.getScore(3), 0d);

        result = selector.selectLevelledSmallestScores(5, 10);
        assertEquals(4, result.size());

        result = selector.selectLevelledSmallestScores(2, 2);
        assertEquals(3, result.size());
        assertEquals(2, (int) result.getElement(1));
        assertEquals(20.0d, result.getScore(1), 0d);
    }

}
