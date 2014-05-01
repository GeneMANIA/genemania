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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.genemania.dto.AddOrganismEngineRequestDto;
import org.genemania.dto.AddOrganismEngineResponseDto;
import org.genemania.dto.RelatedGenesEngineRequestDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.dto.RemoveNetworkEngineRequestDto;
import org.genemania.dto.RemoveNetworkEngineResponseDto;
import org.genemania.dto.UploadNetworkEngineRequestDto;
import org.genemania.dto.UploadNetworkEngineResponseDto;
import org.genemania.engine.IMania;
import org.genemania.engine.Mania2;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.DatasetInfo;
import org.genemania.engine.core.data.NetworkIds;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.type.CombiningMethod;
import org.genemania.type.DataLayout;
import org.genemania.type.NetworkProcessingMethod;
import org.genemania.type.ScoringMethod;
import org.genemania.util.NullProgressReporter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AddOrganismTest {

	static RandomDataCacheBuilder cacheBuilder;
	// params for test organism
	static int org1Id = 1;
	static int org1numGenes = 20;
	static int org1numNetworks = 10;
	static double org1networkSparsity = .5;
	static int numCategories = 50;
	static double org1AnnotationSparsity = .5;

	@Before
	public void setUp() throws Exception {
		cacheBuilder = new RandomDataCacheBuilder(7132);
		cacheBuilder.setUp();

		// random organism 1
		cacheBuilder.addOrganism(org1Id, org1numGenes, org1numNetworks,
				org1networkSparsity, numCategories, org1AnnotationSparsity);

	}

	@After
	public void tearDown() throws Exception {
	    cacheBuilder.tearDown();
	}

	@Test
	public void testProcess() throws Exception {
		AddOrganismEngineRequestDto request = new AddOrganismEngineRequestDto();
		long newOrgId = org1Id + 10;
		HashSet<Long> newNodeIds = new HashSet<Long>();
		for (Long i: new long[] {7, 17, 27, 37, 47}) {
			newNodeIds.add(i);
		}

		request.setOrganismId(newOrgId);
		request.setNodeIds(newNodeIds);
		request.setProgressReporter(NullProgressReporter.instance());

		IMania mania = new Mania2(cacheBuilder.getCache());
		AddOrganismEngineResponseDto response = mania.addOrganism(request);
		assertNotNull(response);

		// retrieve new organisms core objects
		DatasetInfo info = cacheBuilder.getCache().getDatasetInfo(newOrgId);
		assertNotNull(info);

		NodeIds nodeIds = cacheBuilder.getCache().getNodeIds(newOrgId);
		assertNotNull(nodeIds);
		assertEquals(newNodeIds.size(), nodeIds.getNodeIds().length);

		for (long id: nodeIds.getNodeIds()) {
			assertTrue(newNodeIds.contains(id));
		}

		NetworkIds networkIds = cacheBuilder.getCache().getNetworkIds(Data.CORE, newOrgId);
		assertNotNull(networkIds);
		assertNotNull(networkIds.getNetworkIds());
		assertEquals(0, networkIds.getNetworkIds().length);

	}

	/*
	 * add a user network to a newly added organism with no core networks.
	 * execute a find-related test.
	 */
	@Test
	public void testAddUserNetwork() throws Exception {

	    String userNamespace = "user1";
        long newOrgId = -1;
        long newNetworkId = -2;

        // create new organisms
        AddOrganismEngineRequestDto addOrgRequest = new AddOrganismEngineRequestDto();
        HashSet<Long> newNodeIds = new HashSet<Long>();
        for (Long i: new long[] {7, 17, 27, 37, 47}) {
            newNodeIds.add(i);
        }

        addOrgRequest.setOrganismId(newOrgId);
        addOrgRequest.setNodeIds(newNodeIds);
        addOrgRequest.setProgressReporter(NullProgressReporter.instance());

        IMania mania = new Mania2(cacheBuilder.getCache());
        AddOrganismEngineResponseDto addOrgResponse = mania.addOrganism(addOrgRequest);
        assertNotNull(addOrgResponse);

        // now add a user network
        UploadNetworkEngineRequestDto uploadRequest = new UploadNetworkEngineRequestDto();
        uploadRequest.setLayout(DataLayout.WEIGHTED_NETWORK);
        uploadRequest.setMethod(NetworkProcessingMethod.DIRECT);
        uploadRequest.setNamespace(userNamespace);
        uploadRequest.setProgressReporter(NullProgressReporter.instance());
        uploadRequest.setOrganismId(newOrgId);
        uploadRequest.setNetworkId(newNetworkId);
        uploadRequest.setSparsification(50);

        // no newline at end of file, should still get both interactions
        String data = "7\t17\t1.0\n7\t27\t0.8";
        uploadRequest.setData(new StringReader(data));

        // process load request
        UploadNetworkEngineResponseDto uploadResponse = mania.uploadNetwork(uploadRequest);

        assertNotNull(uploadResponse);
        assertEquals(2, uploadResponse.getNumInteractions());

        // now should be able to perform a find-related request on the new organism & user-network
        RelatedGenesEngineRequestDto findRelatedRequest = new RelatedGenesEngineRequestDto();
        findRelatedRequest.setOrganismId(newOrgId);
        findRelatedRequest.setCombiningMethod(CombiningMethod.AUTOMATIC);
        findRelatedRequest.setScoringMethod(ScoringMethod.DISCRIMINANT);
        findRelatedRequest.setNamespace(userNamespace);
        // first 10 nodes as +ve
        ArrayList<Long> posNodes = new ArrayList<Long>();
        posNodes.add(7l);
        posNodes.add(17l);

        findRelatedRequest.setPositiveNodes(posNodes);

        // just the single user network
        Collection<Collection<Long>> requestNetworks = new ArrayList<Collection<Long>>();

        ArrayList<Long> group = new ArrayList<Long>();
        group.add(newNetworkId);
        requestNetworks.add(group);

        findRelatedRequest.setInteractionNetworks(requestNetworks);
        findRelatedRequest.setLimitResults(10);
        findRelatedRequest.setProgressReporter(NullProgressReporter.instance());

        // compute result
        RelatedGenesEngineResponseDto findRelatedResponse = mania.findRelated(findRelatedRequest);
        assertNotNull(findRelatedResponse);
        assertNotNull(findRelatedResponse.getNetworks());
        assertEquals(1, findRelatedResponse.getNetworks().size());

        // remove the user network after
        RemoveNetworkEngineRequestDto removeRequest = new RemoveNetworkEngineRequestDto();
        removeRequest.setOrganismId(newOrgId);
        removeRequest.setNamespace(userNamespace);
        removeRequest.setNetworkId(newNetworkId);

        RemoveNetworkEngineResponseDto removeResponse = mania.removeUserNetworks(removeRequest);
        assertNotNull(removeResponse);
	}

	@Test
	public void testAddUserAttribute() throws Exception {

        String userNamespace = "user1";
        long newOrgId = -2;
        long newNetworkId = -2;

        // create new organisms
        AddOrganismEngineRequestDto addOrgRequest = new AddOrganismEngineRequestDto();
        HashSet<Long> newNodeIds = new HashSet<Long>();
        for (Long i: new long[] {7, 17, 27, 37, 47}) {
            newNodeIds.add(i);
        }

        addOrgRequest.setOrganismId(newOrgId);
        addOrgRequest.setNodeIds(newNodeIds);
        addOrgRequest.setProgressReporter(NullProgressReporter.instance());

        IMania mania = new Mania2(cacheBuilder.getCache());
        AddOrganismEngineResponseDto addOrgResponse = mania.addOrganism(addOrgRequest);
        assertNotNull(addOrgResponse);

        // now add a user network
        UploadNetworkEngineRequestDto uploadRequest = new UploadNetworkEngineRequestDto();
        uploadRequest.setLayout(DataLayout.WEIGHTED_NETWORK);
        uploadRequest.setMethod(NetworkProcessingMethod.DIRECT);
        uploadRequest.setNamespace(userNamespace);
        uploadRequest.setProgressReporter(NullProgressReporter.instance());
        uploadRequest.setOrganismId(newOrgId);
        uploadRequest.setNetworkId(newNetworkId);
        uploadRequest.setSparsification(50);

        // no newline at end of file, should still get both interactions
        String data = "7\t17\t1.0\n7\t27\t0.8";
        uploadRequest.setData(new StringReader(data));

        // process load request
        UploadNetworkEngineResponseDto uploadResponse = mania.uploadNetwork(uploadRequest);

        assertNotNull(uploadResponse);
        assertEquals(2, uploadResponse.getNumInteractions());

        // now should be able to perform a find-related request on the new organism & user-network
        RelatedGenesEngineRequestDto findRelatedRequest = new RelatedGenesEngineRequestDto();
        findRelatedRequest.setOrganismId(newOrgId);
        findRelatedRequest.setCombiningMethod(CombiningMethod.AUTOMATIC);
        findRelatedRequest.setScoringMethod(ScoringMethod.DISCRIMINANT);
        findRelatedRequest.setNamespace(userNamespace);
        // first 10 nodes as +ve
        ArrayList<Long> posNodes = new ArrayList<Long>();
        posNodes.add(7l);
        posNodes.add(17l);

        findRelatedRequest.setPositiveNodes(posNodes);

        // just the single user network
        Collection<Collection<Long>> requestNetworks = new ArrayList<Collection<Long>>();

        ArrayList<Long> group = new ArrayList<Long>();
        group.add(newNetworkId);
        requestNetworks.add(group);

        findRelatedRequest.setInteractionNetworks(requestNetworks);
        findRelatedRequest.setLimitResults(10);
        findRelatedRequest.setProgressReporter(NullProgressReporter.instance());

        // compute result
        RelatedGenesEngineResponseDto findRelatedResponse = mania.findRelated(findRelatedRequest);
        assertNotNull(findRelatedResponse);
        assertNotNull(findRelatedResponse.getNetworks());
        assertEquals(1, findRelatedResponse.getNetworks().size());

        // remove the user network after
        RemoveNetworkEngineRequestDto removeRequest = new RemoveNetworkEngineRequestDto();
        removeRequest.setOrganismId(newOrgId);
        removeRequest.setNamespace(userNamespace);
        removeRequest.setNetworkId(newNetworkId);

        RemoveNetworkEngineResponseDto removeResponse = mania.removeUserNetworks(removeRequest);
        assertNotNull(removeResponse);
	}
}
