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
package org.genemania.engine.summary;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.engine.Constants;
import org.genemania.engine.Constants.CombiningMethod;
import org.genemania.engine.Constants.NetworkType;
import org.genemania.engine.apps.AbstractEngineApp;
import org.genemania.engine.apps.support.DataConnector;
import org.genemania.engine.config.Config;
import org.genemania.engine.core.data.CombinedNetwork;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.core.integration.FeatureWeightMap;
import org.genemania.engine.core.integration.INetworkWeightCalculator;
import org.genemania.engine.core.integration.NetworkWeightCalculatorFactory;
import org.genemania.engine.core.integration.calculators.AbstractNetworkWeightCalculator;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.genemania.util.NullProgressReporter;

/*
 * combining methods that don't depend on query 
 * genes are precomputed and stored. dump them out
 * here to include in the public database archive. 
 */
public class PrecombinedDumper implements Summarizer {

    Organism organism;
    DataConnector dataConnector;
    PreferredNames preferredNames;
    String [] combiningNetworkFilter;
    String combinedNetworksFilter;

    public PrecombinedDumper(Organism organism, DataConnector dataConnector,
            String combinedNetworksFilter, PreferredNames preferredNames) {
        super();
        this.organism = organism;
        this.dataConnector = dataConnector;
        this.combinedNetworksFilter = combinedNetworksFilter;
        this.preferredNames = preferredNames;
    }

    public void setUp() throws Exception {
        // TODO Auto-generated method stub

    }

    /*
     * TODO: make set of combined networks dumped out configurable
     * 
     * @see org.genemania.engine.summary.Summarizer#summarize(org.genemania.engine.summary.ReporterFactory)
     */
    public void summarize(ReporterFactory reporterFactory) throws Exception {

        if ("BP.DEFAULT".equalsIgnoreCase(combinedNetworksFilter)) {
            dump("BP", "DEFAULT", reporterFactory);                 
        }
        else if ("ALL".equalsIgnoreCase(combinedNetworksFilter)) {
            dump("BP", "DEFAULT", reporterFactory);     
            dump("BP", "ALL", reporterFactory);
            dump("CC", "DEFAULT", reporterFactory);     
            dump("CC", "ALL", reporterFactory);
            dump("MF", "DEFAULT", reporterFactory);     
            dump("MF", "ALL", reporterFactory);

            dump("AVERAGE", "DEFAULT", reporterFactory);     
            dump("AVERAGE", "ALL", reporterFactory);
            dump("AVERAGE_CATEGORY", "DEFAULT", reporterFactory);     
            dump("AVERAGE_CATEGORY", "ALL", reporterFactory);            
        }
        else {
            throw new ApplicationException("unexpected value for combinedNetworksFilter: " + combinedNetworksFilter);
        }

    }

    /*
     * write out the precombined network with the given key, and also the
     * corresponding network weights
     */
    void dump(String method, String networks, ReporterFactory reporterFactory) throws Exception {

        String key = getDefaultNetworkKey(organism, networks, method);

        CombinedNetwork cnw = dataConnector.getCache().getCombinedNetwork(Data.CORE, organism.getId(), key);
        FeatureWeightMap weights = cnw.getFeatureWeightMap();
        SymMatrix nw = cnw.getData();
        String reportName = makeCombinedWeightsName(method, networks);
        reportWeights(weights, reporterFactory, reportName);

        NodeIds nodeIds = dataConnector.getCache().getNodeIds(organism.getId());		
        reportName = makeCombinedNetworkName(method, networks);
        NetworksDumper.dumpNetwork(nw, reporterFactory, reportName, preferredNames, nodeIds);
    }

    static String makeCombinedNetworkName(String method, String networks) {
        return String.format("COMBINED.%s_NETWORKS.%s_COMBINING", networks, method);
    }

    static String makeCombinedWeightsName(String method, String networks) {
        return String.format("COMBINATION_WEIGHTS.%s_NETWORKS.%s_COMBINING", networks, method);
    }

    void reportWeights(FeatureWeightMap weights, ReporterFactory reporterFactory, String reportName) throws Exception {
        Reporter weightReporter = reporterFactory.getReporter(reportName);
        DecimalFormat formatter = new DecimalFormat("0.#E0"); // couple digits
        
        try {
            weightReporter.init("group", "network", "weight");
            for (Feature feature: weights.keySet()) {
                if (feature.getType() != NetworkType.SPARSE_MATRIX) {
                    throw new Exception("don't know how to report features of type " + feature.getType().name());
                }
                    
                double weight = weights.get(feature);
                InteractionNetwork network = dataConnector.getNetworkMediator().getNetwork(feature.getId());

                String group = getGroupForNetworkId(feature.getId()).getName();
                String name = network.getName();
                name = NetworksDumper.normalizeString(name);

                weightReporter.write(group, name, formatter.format(weight));
            }
        }
        finally {
            weightReporter.close();
        }
    }



    // why do i need this ... must be doing something wrong with poking around the object model 
    HashMap<Long, InteractionNetworkGroup> nid2gid = null;
    void initnid2gid() {
        nid2gid = new HashMap<Long, InteractionNetworkGroup>();
        Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();
        for (InteractionNetworkGroup group: groups) {
            Collection<InteractionNetwork> networks = group.getInteractionNetworks();
            for (InteractionNetwork n: networks) {
                nid2gid.put(n.getId(), group);
            }
        }
    }
    InteractionNetworkGroup getGroupForNetworkId(long networkId) {
        if (nid2gid == null) {
            initnid2gid();
        }
        return nid2gid.get(networkId);
    }
    /*
     * compute key that identifiers the precomputed network, based on list of network ids and combining method
     */
    String getDefaultNetworkKey(Organism organism, String networkSelection, String methodName) throws Exception {

        Collection<Collection<Long>> networkIds = null;

        if (networkSelection.equals("DEFAULT")) {
            networkIds = AbstractEngineApp.getDefaultNetworks(organism);
        }
        else if (networkSelection.equals("ALL")) {
            networkIds = AbstractEngineApp.getAllNetworks(organism);			
        }
        else {
            throw new Exception("unexpected network selection: " + networkSelection);
        }
        
        CombiningMethod method = Constants.getCombiningMethod(methodName);
        INetworkWeightCalculator calculator = NetworkWeightCalculatorFactory.getCalculator(Data.CORE, dataConnector.getCache(), 
                networkIds, null, organism.getId(), null, Config.instance().getAttributeEnrichmentMaxSize(), method, NullProgressReporter.instance());

        String hash = AbstractNetworkWeightCalculator.hashString(calculator.getParameterKey());
        return hash;
    }

    public void tearDown() throws Exception {
        // TODO Auto-generated method stub

    }

}
