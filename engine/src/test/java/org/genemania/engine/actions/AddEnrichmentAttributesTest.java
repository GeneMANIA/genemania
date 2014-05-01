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
package org.genemania.engine.actions;


import static org.junit.Assert.*;

import org.genemania.dto.AddEnrichmentAttributesEngineRequestDto;
import org.genemania.dto.AddEnrichmentAttributesEngineResponseDto;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.cache.RandomDataCacheConfig;
import org.genemania.engine.core.data.GoAnnotations;
import org.genemania.util.NullProgressReporter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AddEnrichmentAttributesTest {

    static RandomDataCacheBuilder cacheBuilder;
    RandomDataCacheConfig config = RandomDataCacheConfig.getStandardConfig();

    @Before
    public void setUp() throws Exception {
        cacheBuilder = new RandomDataCacheBuilder(7132);
        cacheBuilder.setUp();

        // random organism 1
        cacheBuilder.addOrganism(config);
    }

    @After
    public void tearDown() throws Exception {
        cacheBuilder.tearDown();
    }
    
    @Test
    public void testProcess() throws Exception {
        AddEnrichmentAttributesEngineRequestDto request = new AddEnrichmentAttributesEngineRequestDto();
        
        long newOntologyId = -1;
                
        request.setOrganismId(config.getOrg1Id());
        request.setOntologyId(newOntologyId);
        request.setProgressReporter(NullProgressReporter.instance());
        
        AddEnrichmentAttributes adder = new AddEnrichmentAttributes(cacheBuilder.getCache(), request);
        
        AddEnrichmentAttributesEngineResponseDto response = adder.process();
        assertNotNull(response);
        
        // retrieve the newly created ontology matrix
        GoAnnotations annos = cacheBuilder.getCache().getGoAnnotations(config.getOrg1Id(), "" + newOntologyId);
        assertNotNull(annos);
    }
}
