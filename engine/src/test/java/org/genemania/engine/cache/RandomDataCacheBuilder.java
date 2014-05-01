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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import no.uib.cipr.matrix.DenseVector;

import org.genemania.domain.Organism;
import org.genemania.engine.Constants;
import org.genemania.engine.apps.FastWeightCacheBuilder;
import org.genemania.engine.config.Config;
import org.genemania.engine.core.data.AttributeData;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.DatasetInfo;
import org.genemania.engine.core.data.GoAnnotations;
import org.genemania.engine.core.data.GoIds;
import org.genemania.engine.core.data.Network;
import org.genemania.engine.core.data.NetworkIds;
import org.genemania.engine.core.data.NodeDegrees;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.core.utils.Normalization;
import org.genemania.engine.matricks.Matrix;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.OrganismMediator;
import org.genemania.util.NullProgressReporter;

/**
 *
 * utility class to build a random network cache for performing unit tests.
 * takes seed for reproducibility ... must make construction calls
 * in the same order with the same params.
 *
 *   RandomCacheBuilder builder = new RandomCacheBuilder(90125);
 *   builder.setUp();
 *
 *   // organism 1 with 50 genes, 5 networks and 10% sparsity
 *   builder.addOrganism(1, 50, 5, .10);
 *
 *   // run your tests
 *   ...
 *
 *   builder.tearDown();
 */
public class RandomDataCacheBuilder {

    private TempDirManager tempDir = new TempDirManager("target/temp_test_random_cache");
    private Random random;
    private DataCache cache;
    private boolean useMemCache;

    // since networks and node ids belong to a single identifier space,
    // we separate based on the following constant
    private static int NUMBERING_CONST = 10000;

    // for when we build up network groups in the domain model
    private static int MAX_NUMBER_OF_NETWORKS_PER_GROUP = 5;

    // don't use mem cache, default
    public RandomDataCacheBuilder(long seed) {
        this(seed, false);
    }

    public RandomDataCacheBuilder(long seed, boolean useMemCache) {
        random = new Random(seed);
        this.useMemCache = useMemCache;

    }

    public void setUp() {
        tempDir.setUp();

        IObjectCache objectCache = new FileSerializedObjectCache(tempDir.getTempDir());

        if (useMemCache) {
            objectCache = new MemObjectCache(objectCache);
        }
        cache = new DataCache(objectCache);
    }

    public void tearDown() {
        tempDir.tearDown();
    }

    /*
     * pass the cachedir location from tempDir on through
     */
    public String getCacheDir() {
        return tempDir.getTempDir();
    }

    /*
     * convenience constructor for tests that don't need annotation or attribute data
     */
    public long[] addOrganism(long organismId, int numGenes, int numNetworks, double sparsity) throws ApplicationException {
        return addOrganism(organismId, numGenes, numNetworks, sparsity, 0, 0, 0, 0, 0);
    }

    /*
     * convenience constructor for tests that don't need annotation data
     */
    public long[] addOrganism(long organismId, int numGenes, int numNetworks, double sparsity, int numCategories, double annotationSparsity) throws ApplicationException {
        return addOrganism(organismId, numGenes, numNetworks, sparsity, numCategories, annotationSparsity, 0, 0, 0);
    }

    public void addOrganism(RandomDataCacheConfig cacheConfig) throws ApplicationException {
        long [] org1NetworkIds = addOrganism(cacheConfig.getOrg1Id(), cacheConfig.getOrg1numGenes(), cacheConfig.getOrg1numNetworks(),
                cacheConfig.getOrg1networkSparsity(), cacheConfig.getNumCategories(), cacheConfig.getOrg1AnnotationSparsity(),
                cacheConfig.getNumAttributeGroups(), cacheConfig.getNumAttributesPerGroup(), cacheConfig.getOrg1AttributeSparsity());
        cacheConfig.setOrg1NetworkIds(org1NetworkIds);
    }

    /*
     * networks are numbered from organismId*NUMBERING_CONST.
     * returns an array of the network ids created
     *
     */
    public long[] addOrganism(long organismId, int numGenes, int numNetworks, double sparsity, int numCategories, double annotationSparsity, int numAttributeGroups, int numAttributesPerGroup, double attributeSparsity) throws ApplicationException {

        if (numNetworks >= NUMBERING_CONST) {
            throw new ApplicationException("too many networks");
        }

        if (numGenes >= NUMBERING_CONST) {
            throw new ApplicationException("too many genes");
        }

        long[] rawMapping = makeRawMapping(organismId, numGenes);
        NodeIds nodeIds = new NodeIds(organismId);
        nodeIds.setNodeIds(rawMapping);
        getCache().putNodeIds(nodeIds);


        DenseVector organismDegrees = new DenseVector(numGenes);
        DenseVector networkDegrees = new DenseVector(numGenes);

        long[] networkIds = new long[numNetworks];

        for (int n = 1; n <= numNetworks; n++) {
            org.genemania.engine.matricks.SymMatrix data = makeRandomNetwork(numGenes, sparsity);
            long networkId = organismId * 10000 + n;
            networkIds[n - 1] = networkId;

            Network network = new Network(Data.CORE, organismId, networkId, data);
            getCache().putNetwork(network);

            networkDegrees.zero();
            data.rowSums(networkDegrees.getData());
            organismDegrees.add(networkDegrees);

        }

        // determine num interacting genes for organism
        int numInteracting = 0;
        for (int i=0; i<numGenes; i++) {
            if (organismDegrees.get(i) > 0) {
                numInteracting += 1;
            }
        }

        // write out node degrees
        NodeDegrees nodeDegrees = new NodeDegrees(Data.CORE, organismId);
        nodeDegrees.setDegrees(organismDegrees);
        cache.putNodeDegrees(nodeDegrees);

        // write out the dataset info object
        DatasetInfo info = new DatasetInfo(organismId);
        info.setNumGenes(numGenes);
        info.setNumInteractingGenes(numInteracting);
        getCache().putDatasetInfo(info);

        // if generating synthetic annotation data, we don't need to write
        // the network map since its done at that stage. otherwise create one
        // manually here
        //
        if (numCategories > 0) {
            try {
                putRandomAnnotationData(organismId, networkIds, numGenes, numCategories, annotationSparsity);
            }
            catch (DataStoreException e) {
                throw new ApplicationException("failed to build annotation data", e);
            }
        }
        else {
            putNetworkMap(organismId, networkIds);
        }

        putRandomAttributeData(organismId, numGenes, numAttributeGroups, numAttributesPerGroup, attributeSparsity);

        return networkIds;
    }

    private Map<Long, Integer> putNetworkMap(long organismId, long[] networkIds) throws ApplicationException {
        Map<Long, Integer> map = new LinkedHashMap<Long, Integer>();
        for (int i = 0; i < networkIds.length; i++) {
            //System.out.println("updating idmap with network " + networkIds[i] + " index " + (i));
            map.put(networkIds[i], i);
        }

//        getCache().putColumnId(organismId, map);
        NetworkIds nIds = new NetworkIds(Data.CORE, organismId);
        nIds.setNetworkIds(networkIds);
        getCache().putNetworkIds(nIds);
        return map;
    }

    /*
     * cook up some random go annotations. chances of
     * a particular gene being annotated to a particular
     * category are determined by sparsity.
     */
    private void putRandomAnnotationData(long organismId, long [] networkIds, int numGenes, int numCategories, double sparsity) throws ApplicationException, DataStoreException {

        Map<String, Integer> goIndexMap = new HashMap<String, Integer>();
        String [] goIndices = new String[numCategories];

        DatasetInfo info = getCache().getDatasetInfo(organismId);

        for (int branch = 0; branch < Constants.goBranches.length; branch++) {
            Matrix labels = Config.instance().getMatrixFactory().sparseMatrix(numGenes, numCategories);

            for (int j = 0; j < numCategories; j++) {
                String goId = String.format("GO:%0,7d", numCategories*branch+j);
                goIndices[j] = goId;
                goIndexMap.put(goId, j);
            }

            for (int i = 0; i < numGenes; i++) {
                for (int j = 0; j < numCategories; j++) {
                    double x = random.nextDouble();
                    if (x < sparsity) {
                        labels.set(i, j, 1);
                    }
                }
            }

            GoAnnotations annos = new GoAnnotations(organismId, Constants.goBranches[branch]);
            annos.setData(labels);
            cache.putGoAnnotations(annos);

            GoIds goIds = new GoIds(organismId, Constants.goBranches[branch]);
            goIds.setGoIds(goIndices);

            cache.putGoIds(goIds);
            info.getNumCategories()[branch] = numCategories;
        }

        getCache().putDatasetInfo(info);
        computeSimultCombiningConstants(organismId, networkIds);
    }

    public void computeSimultCombiningConstants(long organismId, long[] networkIds) throws ApplicationException, DataStoreException {
        FastWeightCacheBuilder fwcb = new FastWeightCacheBuilder();
        fwcb.setCacheDir(cache.getCacheDir());
        fwcb.setCache(getCache());
        OrganismMediator organismMediator = new MemOrganismMediator2(organismId, networkIds, MAX_NUMBER_OF_NETWORKS_PER_GROUP);
        Organism organism = organismMediator.getOrganism(organismId);
        fwcb.setOrganismMediator(organismMediator);

        try {
            fwcb.buildFastWeightDataForOrganism(organism, NullProgressReporter.instance());
        }
        catch (Exception e) {
            throw new ApplicationException("failed to compute combining constants", e);
        }
    }

    /*
     * populate attribute test data. number attribute groups starting from 1, attribute ids from 1, attribute
     * ids don't overlap between groups.
     */
    public static long attributeGroupStartingId = 1;
    public static long attributeStartingId = 1;

    void putRandomAttributeData(long organismId, int numGenes, int numAttributeGroups, int numAttributesPerGroup, double sparsity) throws ApplicationException {
        if (numAttributeGroups <= 0) {
            return;
        }

        AttributeGroups attributeGroups = new AttributeGroups(Data.CORE, organismId);
        HashMap<Long, ArrayList<Long>> groupData = new HashMap<Long, ArrayList<Long>>();
        attributeGroups.setAttributeGroups(groupData);

        long currentAttributeId = attributeStartingId;

        // random set of attribute data for each group
        for (long groupId = attributeGroupStartingId; groupId < attributeGroupStartingId + numAttributeGroups; groupId++) {

            // mapping of attribute ids
            ArrayList<Long> attributeIds = new ArrayList<Long>();
            for (int j=0; j<numAttributesPerGroup; j++) {
                attributeIds.add(currentAttributeId);
                currentAttributeId++;
            }
            groupData.put(groupId, attributeIds);

            // random attributes
            Matrix data = Config.instance().getMatrixFactory().sparseColMatrix(numGenes, numAttributesPerGroup);

            for (int i=0; i<numGenes; i++) {
                for (int j=0; j<numAttributesPerGroup; j++) {
                    double x = random.nextDouble();
                    if (x < sparsity) {
                        data.set(i, j, 1);
                    }
                }
            }
            AttributeData attributeSet = new AttributeData(Data.CORE, organismId, groupId);
            attributeSet.setData(data);
            cache.putAttributeData(attributeSet);
        }

        cache.putAttributeGroups(attributeGroups);
    }

    /* contains node ids, numbered from organismId*NUMBERING_CONST
     *
     */
    private long[] makeRawMapping(long organismId, int numGenes) throws ApplicationException {

        long[] rawMapping = new long[numGenes];
        for (int i = 0; i < numGenes; i++) {
            rawMapping[i] = organismId * NUMBERING_CONST + i;
        }
        return rawMapping;
    }

    /*
     * return symmetric sparse matrix with zero diagonal and given
     * sparsity. Normalize as usual.
     *
     */
    private org.genemania.engine.matricks.SymMatrix makeRandomNetwork(int numGenes, double sparsity) throws ApplicationException {

        org.genemania.engine.matricks.SymMatrix matrix = Config.instance().getMatrixFactory().symSparseMatrix(numGenes);

        for (int i = 0; i < numGenes; i++) {
            for (int j = 0; j < i; j++) {
                double x = random.nextDouble();
                if (x < sparsity) {
                    double weight = random.nextDouble();
                    matrix.set(i, j, weight);
                }
            }
        }

        // make symmetric, and normalize
        matrix.setToMaxTranspose(); // TODO: won't need this when enforcing sym type
        Normalization.normalizeNetwork(matrix);

        return matrix;
    }

    /**
     * @return the cache
     */
    public DataCache getCache() {
        return cache;
    }

}
