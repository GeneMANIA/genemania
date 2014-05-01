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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import no.uib.cipr.matrix.DenseVector;
import org.genemania.dto.EnrichmentEngineRequestDto;
import org.genemania.dto.EnrichmentEngineResponseDto;
import org.genemania.dto.OntologyCategoryDto;
import org.genemania.engine.SimpleProgressReporter;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.cache.FileSerializedObjectCache;
import org.genemania.engine.cache.MemObjectCache;
import org.genemania.engine.cache.NetworkMemCache;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.core.data.CategoryIds;
import org.genemania.engine.core.data.GoAnnotations;
import org.genemania.engine.core.data.GoIds;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.exception.ApplicationException;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class ComputeEnrichmentTest {

    static RandomDataCacheBuilder cacheBuilder;
    // params for test organism
    static int org1Id = 1;
    static int org1numGenes = 20;
    static int org1numNetworks = 10;
    static double org1networkSparsity = .5;
    static int numCategories = 50;
    static double org1AnnotationSparsity = .5;

    public ComputeEnrichmentTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        cacheBuilder = new RandomDataCacheBuilder(7132);
        cacheBuilder.setUp();

        // random organism 1
        cacheBuilder.addOrganism(org1Id, org1numGenes, org1numNetworks,
                org1networkSparsity, numCategories, org1AnnotationSparsity);

        makeAnnoData(1);
        // clear mem cache since its a singleton process wide, don't want stuff left over from other tests
        NetworkMemCache.instance().clear();
    }

    @After
    public void tearDown() {
//        cacheBuilder.tearDown();
    }

    // quick hack to get the unit tests running. until we
    // build annotation test data, just copy the BP annotations
    public void makeAnnoData(long ontologyId) throws ApplicationException {
        GoAnnotations goAnnotations = cacheBuilder.getCache().getGoAnnotations(org1Id, "BP");
        GoAnnotations newAnnos = new GoAnnotations(ontologyId, "" + ontologyId);
        newAnnos.setData(goAnnotations.getData());

        cacheBuilder.getCache().putGoAnnotations(newAnnos);

        GoIds goIds = cacheBuilder.getCache().getGoIds(org1Id, "BP");
        GoIds newIds = new GoIds(org1Id, "" + ontologyId);
        newIds.setGoIds(goIds.getGoIds());

        cacheBuilder.getCache().putGoIds(newIds);

        // build category ids starting for some index, lets say 1511

        CategoryIds catIds = new CategoryIds(org1Id, ontologyId);
        long [] ids = new long[goIds.getGoIds().length];
        int startId = 1511;
        for (int i=0; i<ids.length; i++) {
           ids[i] = startId + i;
        }
        catIds.setCategoryIds(ids);

        cacheBuilder.getCache().putCategoryIds(catIds);
    }
    
    @Test
//    @Ignore // broken because BP doesn't have a corresponding ontology in
    public void testBPEnrichment() throws ApplicationException {

        int MIN_NUM_TO_RETURN = 5;
        EnrichmentEngineRequestDto request = new EnrichmentEngineRequestDto();
        request.setOrganismId(org1Id);
        request.setOntologyId(1); //"BP");
        request.setqValueThreshold(0.5);
        request.setMinCategories(MIN_NUM_TO_RETURN);
        request.setProgressReporter(NullProgressReporter.instance());

        // build up list of id to test for enrichment
        NodeIds nodeIds = cacheBuilder.getCache().getNodeIds(request.getOrganismId());
        Collection<Long> nodes = new ArrayList<Long>();
        for (int i = 0; i < nodeIds.getNodeIds().length / 2; i++) {
            nodes.add(nodeIds.getIdForIndex(i));
        }
        request.setNodes(nodes);

        // compute
        ComputeEnrichment ce = new ComputeEnrichment(cacheBuilder.getCache(), request);
        EnrichmentEngineResponseDto response = ce.process();

        assertNotNull(response);
        assertNotNull(response.getAnnotations());
        assertEquals(nodes.size(), response.getAnnotations().size());

        for (long nodeId: response.getAnnotations().keySet()) {
            Collection<OntologyCategoryDto> cats = response.getAnnotations().get(nodeId);
            assertNotNull(cats);
            System.out.println("# of cats for node " + nodeId + " is " + cats.size());
        }

        assertNotNull(response.getEnrichedCategories());
        assertTrue("failed to return min required number of categories, only got " + response.getEnrichedCategories().size(), response.getEnrichedCategories().size() >= MIN_NUM_TO_RETURN);
    }

    @Test
    public void testHyperGeo() {
        double N = 20;
        double k = 6;
        double n = 7;
        double x = 4;

        double p = ComputeEnrichment.computeHyperGeo(x, N, n, k);
        assertEquals(7.04334e-2, p, 1e-6);
    }

    @Test
    public void testCumulHyperGeo() {
        double N = 20;
        double k = 6;
        double n = 7;
        double x = 4;

        double p = ComputeEnrichment.computeCumulHyperGeo(x, N, n, k);
        assertEquals(7.76573e-2, p, 1e-6);
    }

    @Test
    public void testComputeFDRqval() {
        double[] data = {.5, .3, .8, .02, .1};
        DenseVector pval = new DenseVector(data);

        DenseVector qval = ComputeEnrichment.computeFDRqval(10, pval);
        assertNotNull(qval);

        System.out.println(qval);
        // TODO: check if the result is correct!
    }

    @Test
    public void testCancellation() throws Exception {
        int MIN_NUM_TO_RETURN = 5;
        EnrichmentEngineRequestDto request = new EnrichmentEngineRequestDto();
        request.setOrganismId(org1Id);
        request.setOntologyId(1);
        request.setqValueThreshold(0.05);
        request.setMinCategories(MIN_NUM_TO_RETURN);

        ProgressReporter progress = new SimpleProgressReporter();
        progress.cancel();

        request.setProgressReporter(progress);

        // build up list of id to test for enrichment
        NodeIds nodeIds = cacheBuilder.getCache().getNodeIds(request.getOrganismId());
        Collection<Long> nodes = new ArrayList<Long>();
        for (int i = 0; i < nodeIds.getNodeIds().length / 2; i++) {
            nodes.add(nodeIds.getIdForIndex(i));
        }
        request.setNodes(nodes);

        // compute, should give null response because cancellation is set
        ComputeEnrichment ce = new ComputeEnrichment(cacheBuilder.getCache(), request);
        EnrichmentEngineResponseDto response = ce.process();

        assertNull(response);

    }

    @Test
    @Ignore
    public void testLocalCache() throws Exception {
        DataCache cache = new DataCache(new MemObjectCache(new FileSerializedObjectCache("/Users/khalid/bulk/network_cache_dev")));


        int MIN_NUM_TO_RETURN = 10;
        EnrichmentEngineRequestDto request = new EnrichmentEngineRequestDto();
        request.setOrganismId(4);
        request.setOntologyId(1); // need an id to map to this("GO_SLIM");
        request.setqValueThreshold(0.10);
        request.setMinCategories(MIN_NUM_TO_RETURN);
        request.setProgressReporter(NullProgressReporter.instance());

        // build up list of id to test for enrichment
//        NodeIds nodeIds = cache.getNodeIds(request.getOrganismId());
//        Collection<Long> nodes = new ArrayList<Long>();
//        for (int i = 0; i < nodeIds.getNodeIds().length / 2; i++) {
//            nodes.add(nodeIds.getIdForIndex(i));
//        }
        List<Long> nodes = Arrays.asList(410L, 17814L, 2853L, 18081L, 2039L, 3159L, 15618L, 16498L, 703L, 4589L,
                5874L, 1357L, 2249L, 4255L, 12179L, 3571L, 6729L, 2936L, 3921L, 18074L, 536L);
        request.setNodes(nodes);

        // compute
        ComputeEnrichment ce = new ComputeEnrichment(cache, request);
        EnrichmentEngineResponseDto response = ce.process();

        assertNotNull(response);
        assertNotNull(response.getAnnotations());
        assertEquals(nodes.size(), response.getAnnotations().size());

        //System.out.println()
        assertNotNull(response.getEnrichedCategories());
        assertTrue(response.getEnrichedCategories().size() >= MIN_NUM_TO_RETURN);
    }

    @Test
    public void testSelectCategoriesToReturn() {

        double [] qvalsarray = {0.6, 0.2, 0.3, 0.9, 0.8};
        DenseVector qvals = new DenseVector(qvalsarray);

        boolean [] mask = ComputeEnrichment.selectCategoriesToReturn(qvals, .5, 1);
        assertBooleanArrayEquals(new boolean [] {false, true, true, false, false}, mask);

        mask = ComputeEnrichment.selectCategoriesToReturn(qvals, .5, 3);
        assertBooleanArrayEquals(new boolean [] {true, true, true, false, false}, mask);
        
    }

    public static void assertBooleanArrayEquals( boolean [] x, boolean [] y) {
        assertEquals("length equal", x.length, y.length);
        for (int i=0; i<x.length; i++) {
            assertEquals("equal at position " + i, x[i], y[i]);
        }
    }
}
