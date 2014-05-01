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

package org.genemania.engine.apps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import no.uib.cipr.matrix.DenseMatrix;

import org.apache.log4j.Logger;
import org.genemania.domain.Organism;
import org.genemania.engine.Constants;
import org.genemania.engine.Constants.DataFileNames;
import org.genemania.engine.Constants.NetworkType;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.engine.core.data.CoAnnotationSet;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.FeatureTargetCorrelation;
import org.genemania.engine.core.data.GoAnnotations;
import org.genemania.engine.core.data.KtK;
import org.genemania.engine.core.data.KtKFeatures;
import org.genemania.engine.core.data.KtT;
import org.genemania.engine.core.data.NetworkIds;
import org.genemania.engine.core.integration.AssocLoader;
import org.genemania.engine.core.integration.CoAnnoTargetBuilder;
import org.genemania.engine.core.integration.CorrelatedAttributeSelector;
import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.core.integration.FeatureList;
import org.genemania.engine.core.integration.gram.BasicGramBuilder;
import org.genemania.exception.ApplicationException;
import org.genemania.mediator.GeneMediator;
import org.genemania.mediator.NetworkMediator;
import org.genemania.mediator.NodeMediator;
import org.genemania.mediator.OrganismMediator;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Generates and Stores the following in Cache
 * Computes the KtK and Ktt Matrix used by Simultaneous Weighting
 * Computes a map of NetworkId to weights generated for Simultaneous Weighting
 * Generates 3 Matrix where rows are genes and columns are GO categories
 * Generates a map of column number of the matrices above to GO id
 *
 * Load GO annotations from tab delimited text files into binary
 * engine cache data structures used for Fast aka Simultaneous aka GO-based
 * network weighting methods.
 *
 * Input: reads files in -qdir matching the pattern organismId_gobranch.txt, eg:
 *
 *   db/GoCategories/1_BP.txt
 *   db/GoCategories/1_CC.txt
 *   db/GoCategories/1_MF.txt
 *
 * for organism 1.
 *
 * Requires
 *
 */
public class FastWeightCacheBuilder extends AbstractEngineApp {

    private static Logger logger = Logger.getLogger(FastWeightCacheBuilder.class);

    @Option(name = "-qdir", usage = "name of file name containing positiver go terms")
    private static String queryDir;

    @Option(name="-orgId", usage = "optional organism id, otherwise will process all oganisms")
    private static int orgId = -1;

    @Option(name="-incAttr", usage = "include attributes in KtK/KtT")
    private boolean includeAttributes = false;

    @Override
    public NodeMediator getNodeMediator() {
        return nodeMediator;
    }

    @Override
    public void setNodeMediator(NodeMediator nodeMediator) {
        this.nodeMediator = nodeMediator;
    }

    @Override
    public OrganismMediator getOrganismMediator() {
        return organismMediator;
    }

    @Override
    public void setOrganismMediator(OrganismMediator organismMediator) {
        this.organismMediator = organismMediator;
    }

    @Override
    public NetworkMediator getNetworkMediator() {
        return networkMediator;
    }

    @Override
    public void setNetworkMediator(NetworkMediator networkMediator) {
        this.networkMediator = networkMediator;
    }

    @Override
    public GeneMediator getGeneMediator() {
        return geneMediator;
    }

    @Override
    public void setGeneMediator(GeneMediator geneMediator) {
        this.geneMediator = geneMediator;
    }

    @Override
    public void setCache(DataCache cache) {
        this.cache = cache;
    }

    @Override
    public DataCache getCache() {
        return this.cache;
    }

    public void setIncludeAttributes(boolean includeAttributes) {
        this.includeAttributes = includeAttributes;
    }

    public boolean isIncludeAttributes() {
        return includeAttributes;
    }

    /**
     * produce all the cache files for a given organism. first the mapping from
     * matrix index's to GMID's is produced by scanning over the networks, next
     * each network is converted to matrix form.
     *
     * @param organism
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void processAllOrganisms() throws Exception {
        processAllOrganisms(NullProgressReporter.instance());
    }

    public void processAllOrganisms(ProgressReporter progress) throws Exception {
        try {
            for (Organism organism: organismMediator.getAllOrganisms()) {
                processOrganism(organism, progress);
            }
        }
        finally {
        }
    }

    public void processOrganism(int organismId, ProgressReporter progress) throws Exception {
        Organism organism = organismMediator.getOrganism(organismId);
        processOrganism(organism, progress);
    }

    public void processOrganism(Organism organism, ProgressReporter progress) throws Exception {
        loadAnnos(organism);
        buildFastWeightDataForOrganism(organism, progress);
    }

    public void loadAnnos(Organism organism) throws ApplicationException, IOException {
        AssocLoader gen = new AssocLoader(organism, getGeneMediator(),
                getOntologyMediator(), getCache());

        String filebase = queryDir + File.separator + organism.getId();

        for (String goBranch: Constants.goBranches) {
            String filename = filebase + "_" + goBranch + ".txt";
            logger.info("getting labels from file: " + filename);
            gen.loadGoBranchAnnos(filename, goBranch);
        }
    }

    public void buildFastWeightDataForOrganism(Organism organism, ProgressReporter progress) throws ApplicationException {
        logger.info("processing organism " + organism.getId() + " " + organism.getName());

        Collection<Collection<Long>> groupedIdList = getAllNetworks(organism);
        int numNetworks = count(groupedIdList);

        NetworkIds networkIds = makeNetworkIds(organism, groupedIdList);
        cache.putNetworkIds(networkIds);
        logger.debug("Number of Network is : " + numNetworks);

        // build co-annotation target data structures for each go branch. compute attributes
        // correlated with each of these branch-specific targets if requested
        // keep all the selected attributes in a set instead of FeatureList to dedup attributes,
        // in case same attribute selected for multiple branches
        HashSet<Feature> attributesForAllBranches = new HashSet<Feature>();

        // attribute group id -> FeatureTargetCorrelations object
        HashMap<Long, FeatureTargetCorrelation> ftcMap = new HashMap<Long, FeatureTargetCorrelation>();
        for (String goBranch: Constants.goBranches) {
            logger.info("processing branch: " + goBranch);
            GoAnnotations goAnnos = cache.getGoAnnotations(organism.getId(), goBranch);
            CoAnnotationSet annoSet = CoAnnoTargetBuilder.computeCoAnnoationSet(organism.getId(), goBranch, goAnnos.getData());
            cache.putCoAnnotationSet(annoSet);

            // add correlated attributes?
            if (includeAttributes) {
                int attributesLimit = 30; // where should we set this?
                ArrayList<Long> attributeGroupIds = getAttributeGroupIds(Data.CORE, organism);
                FeatureList attributesForBranch = computeAttributeCorrelations(ftcMap, Data.CORE, organism, goBranch, attributeGroupIds, attributesLimit);
                attributesForAllBranches.addAll(attributesForBranch);
            }
        }

        if (includeAttributes) {
            for (FeatureTargetCorrelation featureTargetCorrelations: ftcMap.values()) {
                cache.putData(featureTargetCorrelations);
            }
        }

        // build ktk and ktt, including attributes if we've selected any
        BasicGramBuilder builder = new BasicGramBuilder(cache, Data.CORE, organism.getId(), NullProgressReporter.instance());

        FeatureList networkFeatureList = makeNetworkFeatureList(networkIds);
        FeatureList networksAndAttributes = new FeatureList();

        networksAndAttributes.addAll(networkFeatureList);
        networksAndAttributes.addAll(attributesForAllBranches);
        Collections.sort(networksAndAttributes);

        networksAndAttributes.addBias();

        logger.debug(String.format("building KtK, size %dx%d", networksAndAttributes.size(), networksAndAttributes.size()));
        DenseMatrix basicKtK = builder.buildBasicKtK(networksAndAttributes, NullProgressReporter.instance());
        KtK ktk = new KtK(Data.CORE, organism.getId(), DataFileNames.KtK_BASIC.getCode());
        ktk.setData(basicKtK);
        cache.putKtK(ktk);

        // TODO: write out networksAndAttributes featurelist, since no
        // longer implicit in KtK
        KtKFeatures ktkFeatures = new KtKFeatures(Data.CORE, organism.getId());
        ktkFeatures.setFeatures(networksAndAttributes);
        cache.putData(ktkFeatures);

        for (String goBranch: Constants.goBranches) {
            logger.info("processing branch: " + goBranch);
            CoAnnotationSet annoSet = cache.getCoAnnotationSet(organism.getId(), goBranch);
            DenseMatrix Ktt = builder.buildKtT(networksAndAttributes, annoSet, NullProgressReporter.instance());
            KtT kttData = new KtT(Data.CORE, organism.getId(), goBranch);
            kttData.setData(Ktt);
            cache.putKtT(kttData);
        }
    }


    public FeatureList computeAttributeCorrelations(HashMap<Long, FeatureTargetCorrelation> ftcMap, String namespace, Organism organism, String goBranch, ArrayList<Long> attributeGroupIds, int maxAttributes) throws ApplicationException {
        FeatureList topCorrelatedAttributesForAllGroups= new FeatureList();

        for (long attributeGroupId: attributeGroupIds) {
            FeatureTargetCorrelation ftc = ftcMap.get(attributeGroupId);
            if (ftc == null) {
                ftc = new FeatureTargetCorrelation(namespace, organism.getId(), attributeGroupId);
                ftc.setCorrelations(new HashMap<String, FeatureList>());
                ftcMap.put(attributeGroupId, ftc);
            }

            CorrelatedAttributeSelector attributeSelector = new CorrelatedAttributeSelector(cache, goBranch, 0);
            FeatureList attributesForBranch = attributeSelector.selectAttributes(organism.getId(), attributeGroupId);
            ftc.getCorrelations().put(goBranch, attributesForBranch);
            topCorrelatedAttributesForAllGroups.addAll(attributesForBranch.subList(0, maxAttributes));
        }

        return topCorrelatedAttributesForAllGroups;
    }

    private ArrayList<Long> getAttributeGroupIds(String namespace, Organism organism) throws ApplicationException {
        ArrayList<Long> attributeGroupIds = new ArrayList<Long>();
        try {
            AttributeGroups groups = cache.getAttributeGroups(namespace, organism.getId());
            attributeGroupIds.addAll(groups.getAttributeGroups().keySet());
        }
        catch (ApplicationException e) {
            logger.warn(String.format("no attribute groups found for %d, skipping (%s)", organism.getId(), e.toString()));
        }
        return attributeGroupIds;
    }

    /*
     * generate a network id mapping for all core networks in the dataset
     */
    public NetworkIds makeNetworkIds(Organism organism, Collection<Collection<Long>> ids) {

        int numNetworks = count(ids);
        long [] idsTable = new long[numNetworks];
        int i = 0;

        for (Collection<Long> group: ids) {
            for (Long id: group) {
                idsTable[i] = id;
                i++;
            }
        }

        NetworkIds networkIds = new NetworkIds(Data.CORE, organism.getId());
        networkIds.setNetworkIds(idsTable);

        return networkIds;
    }

    public FeatureList makeNetworkFeatureList(NetworkIds networkIds) {
        FeatureList featureList = new FeatureList();

        long fakeGroupId = Feature.FAKE_SPARSE_NETWORK_GROUP_ID;
        for (long networkId: networkIds.getNetworkIds()) {
            Feature feature = new Feature(NetworkType.SPARSE_MATRIX, fakeGroupId, networkId);
            featureList.add(feature);
        }

        return featureList;
    }

    public void selectAttributes() {

        if (includeAttributes) {
//            CorrelatedAttributeSelector attributeSelector = new CorrelatedAttributeSelector(cache, method.toString(), attributesLimit);

        }

    }
    @Override
    public boolean getCommandLineArgs(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        }
        catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java -jar myprogram.jar [options...] arguments...");
            parser.printUsage(System.err);
            return false;
        }

        return true;
    }

    @Override
    public void process() throws Exception {
        // default, do all organisms
        if (orgId == -1) {
            processAllOrganisms();
        }
        else {
            processOrganism(orgId, NullProgressReporter.instance());
        }

    }

    public static void main(String[] args) throws Exception {

        FastWeightCacheBuilder cacheBuilder = new FastWeightCacheBuilder();
        cacheBuilder.getCommandLineArgs(args);

        try {
            cacheBuilder.init();
            cacheBuilder.process();
            cacheBuilder.cleanup();
        }
        catch (Exception e) {
            logger.error("Fatal exception", e);
            System.exit(1);
        }
    }
}
