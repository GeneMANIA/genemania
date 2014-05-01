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

package org.genemania.engine.cache;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class FileSerializedObjectCacheTest {

    public static TempDirManager tempDir = new TempDirManager();

    public FileSerializedObjectCacheTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        tempDir.setUp();
    }

    @After
    public void tearDown() {
        tempDir.tearDown();
    }


    
    @Test 
    public void testExists() throws Exception {
        IObjectCache cache = new FileSerializedObjectCache(tempDir.getTempDir());
        
        String [] key = {"A", "B.txt"};
        cache.put(key, key, true);
        
        assertFalse(cache.exists(new String[] {"C"}));
        assertFalse(cache.exists(new String[] {"A", "C.txt"}));
        assertTrue(cache.exists(key));
    }
    
    @Test public void testList() throws Exception {
        IObjectCache cache = new FileSerializedObjectCache(tempDir.getTempDir());

        String [] key = {"A", "B.txt"};
        cache.put(key, key, true);
    	
        // should return empty
        List<String[]> result;
        result = cache.list(new String[] {"C"});
        assertNotNull(result);
        assertEquals(0, result.size());
        
        // listing parent should return they original key
        result = cache.list(new String[] {"A"});
        assertNotNull(result);
        assertEquals(1, result.size());
        String[] childKey = result.get(0);
        assertEquals(2, childKey.length);
        assertEquals("A", childKey[0]);
        assertEquals("B.txt", childKey[1]);
        
    }

}
