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

package org.genemania.engine;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.mediator.NetworkMediator;
import org.genemania.mediator.NodeMediator;
import org.genemania.mediator.OrganismMediator;

import org.genemania.dto.InteractionDto;
import org.genemania.dto.NetworkDto;
import org.genemania.dto.RelatedGenesEngineRequestDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.dto.UploadNetworkEngineRequestDto;
import org.genemania.dto.UploadNetworkEngineResponseDto;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.cache.TempDirManager;
import org.genemania.engine.config.Config;
import org.genemania.exception.ApplicationException;
import org.genemania.type.CombiningMethod;
import org.genemania.type.DataLayout;
import org.genemania.type.NetworkProcessingMethod;
import org.genemania.type.ScoringMethod;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;

public class Mania2Test {

    int numNodes = 5;
    int numNetworks = 3;
    int numPositives = 2;
    int numToFind = 2;
    Vector<Node> nodes;
    Vector<Organism> organisms;
    Vector<InteractionNetwork> networks;
    Vector<Node> positiveNodes;
    Vector<Node> negativeNodes;
    NodeMediator nodeMediator;
    OrganismMediator organismMediator;
    NetworkMediator networkMediator;

    @BeforeClass
    public static void setUpClass() throws Exception {

        Config.reload("default_test_config.properties");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {

        Config.reload();

    }

    /*
     * test the 'VO' api (uses dto's in common for request and
     * and response) on random test data.
     */
    @Test
    public void testRandomNetworksDtoApi() throws ApplicationException {

        // create some test data
        RandomDataCacheBuilder rcb = new RandomDataCacheBuilder(2112);
        rcb.setUp();
        long[] organism1NetworkIds = rcb.addOrganism(1, 50, 5, 0.5);
        long[] organism2NetworkIds = rcb.addOrganism(2, 20, 3, 0.5);

        long[] organism1NodeIds = rcb.getCache().getNodeIds(1).getNodeIds();
        long[] organism2NodeIds = rcb.getCache().getNodeIds(2).getNodeIds();

        // sanity check on test data
        assertEquals(5, organism1NetworkIds.length);
        assertEquals(3, organism2NetworkIds.length);
        assertEquals(50, organism1NodeIds.length);
        assertEquals(20, organism2NodeIds.length);

        // setup mania instance ... only data dependency is on the cache
        Mania2 mania = new Mania2(rcb.getCache());

        // create request for organism 1
        RelatedGenesEngineRequestDto request = new RelatedGenesEngineRequestDto();
        request.setOrganismId(1);
        request.setCombiningMethod(CombiningMethod.AUTOMATIC);
        request.setScoringMethod(ScoringMethod.DISCRIMINANT);

        // first 10 nodes as +ve
        ArrayList<Long> posNodes = new ArrayList<Long>();
        for (int i = 0; i < 10; i++) {
            posNodes.add((long) organism1NodeIds[i]);
        }
        request.setPositiveNodes(posNodes);

        // all networks
        Collection<Collection<Long>> requestNetworks = new ArrayList<Collection<Long>>();

        ArrayList<Long> group = new ArrayList<Long>();
        for (int i = 0; i < organism1NetworkIds.length; i++) {
            group.add((long) organism1NetworkIds[i]);
            //requestNetworks.add((long)organism1NetworkIds[i]);
        }
        requestNetworks.add(group);
        request.setInteractionNetworks(requestNetworks);
        request.setLimitResults(10);
        request.setProgressReporter(NullProgressReporter.instance());

        // compute result
        RelatedGenesEngineResponseDto response = mania.findRelated(request);
        assertNotNull(response);
        assertNotNull(response.getNetworks());
        assertEquals(3, response.getNetworks().size());

        System.out.println("num networks in result: " + response.getNetworks().size());
        for (NetworkDto n: response.getNetworks()) {

            System.out.println("  network id: " + n.getId() + " weight: " + n.getWeight() + " num interactions: " + n.getInteractions().size());

            // check values
            if (n.getId() == 10004L) {
                assertEquals(104, n.getInteractions().size());
                assertEquals(2.10650979198242, n.getWeight(), 1e-5);
            }
            else if (n.getId() == 10001L) {
                assertEquals(87, n.getInteractions().size());
                assertEquals(1.250439888726468, n.getWeight(), 1e-5);

            }
            else if (n.getId() == 10002L) {
                assertEquals(104, n.getInteractions().size());
                assertEquals(1.6702085621334295, n.getWeight(), 1e-5);
            }
            else {
                fail("unexpected network id in result");
            }

            for (InteractionDto interaction: n.getInteractions()) {
                /*
                System.out.println(String.format("   inter: node %s %s, node %s %s, %s",
                interaction.getNodeVO1().getId(),
                interaction.getNodeVO1().getScore(),
                interaction.getNodeVO2().getId(),
                interaction.getNodeVO2().getScore(),
                interaction.getWeight()
                ));
                 */
            }
        }

        // cleanup test data
        rcb.tearDown();

    }

    /*
     * very simple cancellation test, just pass in
     * a progress reporter that is already set to cancelled.
     *
     * the setup request setup code should be moved into a helper TODO
     */
    @Test
    public void testRelatedCancellation() throws ApplicationException {

        // create some test data
        RandomDataCacheBuilder rcb = new RandomDataCacheBuilder(2112);
        rcb.setUp();
        long[] organism1NetworkIds = rcb.addOrganism(1, 50, 5, 0.5);
        long[] organism2NetworkIds = rcb.addOrganism(2, 20, 3, 0.5);

        long[] organism1NodeIds = rcb.getCache().getNodeIds(1).getNodeIds();
        long[] organism2NodeIds = rcb.getCache().getNodeIds(2).getNodeIds();

        // sanity check on test data
        assertEquals(5, organism1NetworkIds.length);
        assertEquals(3, organism2NetworkIds.length);
        assertEquals(50, organism1NodeIds.length);
        assertEquals(20, organism2NodeIds.length);

        // setup mania instance ... only data dependency is on the cache
        Mania2 mania = new Mania2(rcb.getCache());

        // create request for organism 1
        RelatedGenesEngineRequestDto request = new RelatedGenesEngineRequestDto();
        request.setOrganismId(1);
        request.setCombiningMethod(CombiningMethod.AUTOMATIC);
        request.setScoringMethod(ScoringMethod.DISCRIMINANT);

        // first 10 nodes as +ve
        ArrayList<Long> posNodes = new ArrayList<Long>();
        for (int i = 0; i < 10; i++) {
            posNodes.add((long) organism1NodeIds[i]);
        }
        request.setPositiveNodes(posNodes);

        // all networks
        Collection<Collection<Long>> requestNetworks = new ArrayList<Collection<Long>>();

        ArrayList<Long> group = new ArrayList<Long>();
        for (int i = 0; i < organism1NetworkIds.length; i++) {
            group.add((long) organism1NetworkIds[i]);
            //requestNetworks.add((long)organism1NetworkIds[i]);
        }
        requestNetworks.add(group);
        request.setInteractionNetworks(requestNetworks);
        request.setLimitResults(10);

        ProgressReporter progress = new SimpleProgressReporter();
        progress.cancel();

        request.setProgressReporter(progress);

        // compute result
        RelatedGenesEngineResponseDto response = mania.findRelated(request);
        assertNull(response);

        // cleanup test data
        rcb.tearDown();

    }

    /*
     * very simple cancellation test, just pass in
     * a progress reporter that is already set to cancelled.
     *
     */
    @Test
    public void testUploadCancellation() throws Exception {
        String user = "user1";

        // create some test data
        RandomDataCacheBuilder rcb = new RandomDataCacheBuilder(2112);
        rcb.setUp();
        long[] organism1NetworkIds = rcb.addOrganism(1, 20, 5, 0.5, 5, 0.5);

        long[] organism1NodeIds = rcb.getCache().getNodeIds(1).getNodeIds();


        // populate a request
        UploadNetworkEngineRequestDto request = new UploadNetworkEngineRequestDto();
        request.setLayout(DataLayout.WEIGHTED_NETWORK);
        request.setMethod(NetworkProcessingMethod.DIRECT);
        request.setNamespace(user);
        request.setProgressReporter(NullProgressReporter.instance());
        request.setOrganismId(1);
        request.setNetworkId(-2);
        request.setSparsification(50);

        String data = "10000\t10001\t1.0\n10000\t10002\t0.8\n";
        request.setData(new StringReader(data));

        ProgressReporter progress = new SimpleProgressReporter();
        progress.cancel();

        request.setProgressReporter(progress);

        Mania2 mania = new Mania2(rcb.getCache());

        // process load request
        UploadNetworkEngineResponseDto result = mania.uploadNetwork(request);

        assertNull(result);
    }

    /*
     * if client doesn't specify the CORE namespace but uses a user namespace
     * instead, however with no user networks specified (or even loaded!),
     * we should still be able to return a response using the CORE data only
     *
     * TODO: tidy up code duplication here with other testcases
     */
    @Test
    public void testNoUserNetworksInUserSession() throws Exception {
        // create some test data
        RandomDataCacheBuilder rcb = new RandomDataCacheBuilder(2112);
        rcb.setUp();
        long[] organism1NetworkIds = rcb.addOrganism(1, 50, 5, 0.5);
        long[] organism2NetworkIds = rcb.addOrganism(2, 20, 3, 0.5);

        long[] organism1NodeIds = rcb.getCache().getNodeIds(1).getNodeIds();
        long[] organism2NodeIds = rcb.getCache().getNodeIds(2).getNodeIds();

        // sanity check on test data
        assertEquals(5, organism1NetworkIds.length);
        assertEquals(3, organism2NetworkIds.length);
        assertEquals(50, organism1NodeIds.length);
        assertEquals(20, organism2NodeIds.length);

        // setup mania instance ... only data dependency is on the cache
        Mania2 mania = new Mania2(rcb.getCache());

        // create request for organism 1
        RelatedGenesEngineRequestDto request = new RelatedGenesEngineRequestDto();
        request.setNamespace("TEST_USER_SESSION");
        request.setOrganismId(1);
        request.setCombiningMethod(CombiningMethod.AUTOMATIC);
        request.setScoringMethod(ScoringMethod.DISCRIMINANT);

        // first 10 nodes as +ve
        ArrayList<Long> posNodes = new ArrayList<Long>();
        for (int i = 0; i < 10; i++) {
            posNodes.add((long) organism1NodeIds[i]);
        }
        request.setPositiveNodes(posNodes);

        // all networks
        Collection<Collection<Long>> requestNetworks = new ArrayList<Collection<Long>>();

        ArrayList<Long> group = new ArrayList<Long>();
        for (int i = 0; i < organism1NetworkIds.length; i++) {
            group.add((long) organism1NetworkIds[i]);
            //requestNetworks.add((long)organism1NetworkIds[i]);
        }
        requestNetworks.add(group);
        request.setInteractionNetworks(requestNetworks);
        request.setLimitResults(10);
        request.setProgressReporter(NullProgressReporter.instance());

        // compute result
        RelatedGenesEngineResponseDto response = mania.findRelated(request);
        assertNotNull(response);
        assertNotNull(response.getNetworks());
        assertEquals(3, response.getNetworks().size());

        System.out.println("num networks in result: " + response.getNetworks().size());
        for (NetworkDto n: response.getNetworks()) {

            System.out.println("  network id: " + n.getId() + " weight: " + n.getWeight() + " num interactions: " + n.getInteractions().size());

            // check values
            if (n.getId() == 10004L) {
                assertEquals(104, n.getInteractions().size());
                assertEquals(2.10650979198242, n.getWeight(), 1e-5);
            }
            else if (n.getId() == 10001L) {
                assertEquals(87, n.getInteractions().size());
                assertEquals(1.250439888726468, n.getWeight(), 1e-5);

            }
            else if (n.getId() == 10002L) {
                assertEquals(104, n.getInteractions().size());
                assertEquals(1.6702085621334295, n.getWeight(), 1e-5);
            }
            else {
                fail("unexpected network id in result");
            }

            for (InteractionDto interaction: n.getInteractions()) {
                /*
                System.out.println(String.format("   inter: node %s %s, node %s %s, %s",
                interaction.getNodeVO1().getId(),
                interaction.getNodeVO1().getScore(),
                interaction.getNodeVO2().getId(),
                interaction.getNodeVO2().getScore(),
                interaction.getWeight()
                ));
                 */
            }
        }

        // cleanup test data
        rcb.tearDown();
    }

    @Test
    public void testVersion() {
        IMania mania = new Mania2(null); // data cache not needed for this test
        String version = mania.getVersion();
        System.out.println("version: " + version);
        assertNotNull(version);
        assertFalse(version.equalsIgnoreCase(Constants.UNKNOWN_VERSION));
    }
    
    // TODO: add test case for label node not appearing in interaction networks
}
