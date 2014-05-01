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

import java.util.ArrayList;
import java.util.Collection;
import org.apache.log4j.Logger;
import org.genemania.domain.Organism;
import org.genemania.engine.Constants;
import org.genemania.engine.Constants.CombiningMethod;
import org.genemania.engine.config.Config;
import org.genemania.engine.core.data.CombinedNetwork;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.integration.FeatureWeightMap;
import org.genemania.engine.core.integration.INetworkWeightCalculator;
import org.genemania.engine.core.integration.NetworkWeightCalculatorFactory;
import org.genemania.engine.core.integration.calculators.AbstractNetworkWeightCalculator;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;
import org.kohsuke.args4j.Option;

/**
 * Precomputes combined networks for all and default
 * network selections for each go based combining method, 
 * as well for average and average-by-type combining
 * 
 * The result is a set of networks keyed by a hash of their
 * construction parameters, which can be loaded instead of
 * recomputed at run time when available.
 */
public class NetworkPrecombiner extends AbstractEngineApp {

    private static Logger logger = Logger.getLogger(NetworkPrecombiner.class);

    @Option(name="-orgId", usage = "optional organism id, otherwise will process all oganisms")
    private static int orgId = -1;

    @Option(name="-combineGroups", usage = "optinal, build precombined networks for each network group individually, defaults to false")
    private static boolean combineGroups = false;
    
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
        preComputeForNetworkCollections(organism);
    }

    /*
     * perform computations for default, all, and each network group
     */
    void preComputeForNetworkCollections(Organism organism) throws ApplicationException, DataStoreException {

        // default networks
        logger.info("precomputing default networks");
        Collection<Collection<Long>> networkIds = getDefaultNetworks(organism);
        preComputeForAllMethods(organism, networkIds);

        // all networks
        logger.info("precomputing all networks");
        networkIds = getAllNetworks(organism);
        preComputeForAllMethods(organism, networkIds);

        // each group in all networks individually selected, eg so all of coexp, all of ppi, etc
        if (combineGroups) {
            for (Collection<Long> group: networkIds) {
                logger.info("precomputing for network group");
                Collection<Collection<Long>> holder = new ArrayList<Collection<Long>>();
                holder.add(group);
                preComputeForAllMethods(organism, holder);
            }
        }
    }

    /*
     * given networks, precompute for average, average_category, and BP, CC, and MF combining
     */
    void preComputeForAllMethods(Organism organism, Collection<Collection<Long>> networkIds) throws ApplicationException, DataStoreException {

        // annotation based combining for each of the three branches
        for (int goBranchNum = 0; goBranchNum < Constants.goBranches.length; goBranchNum++) {
            CombiningMethod method = Constants.getMethodForBranch(Constants.goBranches[goBranchNum]);
            logger.info("using method " + method + " for goBranch " + Constants.goBranches[goBranchNum]);
            precomputeForNetworks(organism, method, networkIds);
        }

        // equal weights
        precomputeForNetworks(organism, CombiningMethod.AVERAGE, networkIds);

        // equal by group
        precomputeForNetworks(organism, CombiningMethod.AVERAGE_CATEGORY, networkIds);

    }

    /*
     * perform precomputation and cache combined network given combining method and networks
     */
    void precomputeForNetworks(Organism organism, CombiningMethod method, Collection<Collection<Long>> networkIds) throws ApplicationException {

        logger.info("using method " + method);

        // TODO: load default attribute groups, if any. for now we set to empty list
        Collection<Long> attributeGroupIds = new ArrayList<Long>();
        INetworkWeightCalculator calculator = NetworkWeightCalculatorFactory.getCalculator(Data.CORE, cache, networkIds, attributeGroupIds, organism.getId(), 
        		null, Config.instance().getAttributeEnrichmentMaxSize(), method, NullProgressReporter.instance());
        calculator.process();
        FeatureWeightMap weights = calculator.getWeights();
        SymMatrix combinedNetwork = calculator.getCombinedMatrix();
        combinedNetwork.compact();

        double WtW = combinedNetwork.elementMultiplySum(combinedNetwork);
        logger.debug("WtW: " + WtW);

        String key = calculator.getParameterKey();
        String hash = AbstractNetworkWeightCalculator.hashString(key);

        logger.info("storing precombined network for organism " + organism.getId() + " with hash " + hash + " for combination key " + key);
        CombinedNetwork combined = new CombinedNetwork(Data.CORE, organism.getId(), hash);
        combined.setFeatureWeightMap(weights);
        combined.setWtW(WtW);
        combined.setData(combinedNetwork);
        cache.putCombinedNetwork(combined);

    }

    @Override
    public void init() throws Exception {
    	super.init();    	
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

        NetworkPrecombiner combiner = new NetworkPrecombiner();
        if (!combiner.getCommandLineArgs(args)) {
            System.exit(1);
        }

        try {
        	combiner.init();
            combiner.process();
            combiner.cleanup();
        }
        catch (Exception e) {
            logger.error("Fatal exception", e);
            System.exit(1);
        }
    }
}
