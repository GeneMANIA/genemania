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
import java.util.List;

import org.apache.log4j.Logger;
import org.genemania.domain.Gene;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.NetworkMetadata;
import org.genemania.domain.Organism;
import org.genemania.dto.NetworkDto;
import org.genemania.dto.RelatedGenesEngineRequestDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.engine.Mania2;
import org.genemania.engine.core.utils.ObjectSelector;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.lucene.exporter.IndexUpdater;
import org.genemania.type.CombiningMethod;
import org.genemania.type.ScoringMethod;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/*
 * select default networks according to our heuristic, all desired
 * default networks should already be marked, except for coexp which
 * can be marked or not. this program will run BP combining with default
 * networks + all coexp networks, then set the top 20 coexp networks 
 * to be included in the default set.
 * 
 * TODO: doesn't support attributes as part of default set yet
 */
public class DefaultNetworkSelector extends AbstractEngineApp {
    private static Logger logger = Logger.getLogger(DefaultNetworkSelector.class);

    @Option(name = "-orgId", usage = "optional organism id, otherwise will process all oganisms")
    private int orgId = -1;

    @Option(name = "-num", usage = "# of coexp networks to include in default set, defaults to 20")
    private int numCoexp = 20;

    private Mania2 mania2;
    private static String COEXP_GROUP_CODE = "coexp";

    @Override
    public void process() throws DataStoreException, ApplicationException, IOException {
        // default, do all organisms
        if (orgId == -1) {
            processAllOrganisms(NullProgressReporter.instance());
        } else {
            processOrganism(orgId, NullProgressReporter.instance());
        }
    }

    /*
     * since we are going to be closing & reopening lucene index 
     * around our updates, let's not keep any domain objects around
     * in-between just in case. load the organism ids up front and use them
     * to specify access.
     */
    public void processAllOrganisms(ProgressReporter progress)
            throws ApplicationException, DataStoreException, IOException {
        try {
            ArrayList<Long> organismIds = new ArrayList<Long>();
            for (Organism organism: organismMediator.getAllOrganisms()) {
                organismIds.add(organism.getId());
            }
            
            for (Long organismId : organismIds) {
                processOrganism(organismId, progress);
            }
        } finally {
        }
    }

    public void processOrganism(long orgId, ProgressReporter progress)
            throws ApplicationException, DataStoreException, IOException {
        Organism organism = organismMediator.getOrganism(orgId);
        processOrganism(organism, progress);
    }

    public void processOrganism(Organism organism, ProgressReporter progress)
            throws ApplicationException, DataStoreException, IOException {
        logger.info("processing organism " + organism.getId() + " "
                + organism.getName());

        Collection<Collection<Long>> networkIds = getDefaultPlusCoexpNetworks(organism);
        Collection<Long> coexpNetworks = getNetworkGroupByCode(organism,
                COEXP_GROUP_CODE);

        if (coexpNetworks.size() == 0) {
            logger.warn(String.format("No coexp networks found for %s, skipping", organism.getName()));
        }
        else {    
            RelatedGenesEngineRequestDto request = buildRequest(organism,
                    networkIds);
            RelatedGenesEngineResponseDto response = mania2.findRelated(request);
            updateDefaultNetworks(organism, coexpNetworks, response);
        }
    }

    /*
     * 
     */
    public static Collection<Collection<Long>> getDefaultPlusCoexpNetworks(
            Organism organism) throws ApplicationException, DataStoreException {

        int numFound = 0;

        Collection<InteractionNetworkGroup> groups = organism
                .getInteractionNetworkGroups();

        Collection<Collection<Long>> ids = new ArrayList<Collection<Long>>();
        for (InteractionNetworkGroup group : groups) {

            Collection<InteractionNetwork> networks = group
                    .getInteractionNetworks();
            List<Long> list = new ArrayList<Long>();

            for (InteractionNetwork n : networks) {
                if (n.isDefaultSelected()
                        || group.getCode().equalsIgnoreCase(COEXP_GROUP_CODE)) {
                    NetworkMetadata metadata = n.getMetadata();

                    list.add(n.getId());
                    numFound += 1;
                }
            }

            if (list.size() > 0) {
                ids.add(list);
            }
        }

        if (ids.size() == 0) {
            throw new ApplicationException("no networks found!");
        }

        return ids;
    }

    private Collection<Long> getNetworkGroupByCode(Organism organism,
            String code) throws ApplicationException {

        int numFound = 0;
        Collection<InteractionNetworkGroup> groups = organism
                .getInteractionNetworkGroups();

        Collection<Long> ids = new ArrayList<Long>();
        for (InteractionNetworkGroup group : groups) {

            if (group.getCode().equalsIgnoreCase(code)) {

                Collection<InteractionNetwork> networks = group
                        .getInteractionNetworks();

                for (InteractionNetwork n : networks) {
                    ids.add(n.getId());
                    numFound += 1;
                }
            }
        }

        if (ids.size() == 0) {
            throw new ApplicationException("no default networks found!");
        }

        return ids;
    }

    /*
     * for safety we close the searcher & re-init index access around our index updates
     */
    private void updateDefaultNetworks(Organism organism, Collection<Long> coexpNetworks,
            RelatedGenesEngineResponseDto response) throws IOException, ApplicationException {

        ArrayList<InteractionNetwork> topNetworks = getTopNetworks(response, coexpNetworks);
        if (topNetworks.size() == 0) {
            throw new ApplicationException(String.format("strange, selected no default coexpression networks for %s, please investigate!", organism.getName()));
        }

        getSearcher().close();
        IndexUpdater updater = new IndexUpdater(new File(getIndexDir()),
                getAnalyzer());

        // clear default for all coexp networks
        for (Long id: coexpNetworks) {
            updater.updateNetworkIsDefault(organism.getId(), id, false);
        }

        // mark the top ones to default
        for (InteractionNetwork nw: topNetworks) {
            logger.info(String.format("setting network %s for oganism %s to default", nw.getName(), organism.getName()));
            updater.updateNetworkIsDefault(organism.getId(), nw.getId(), true);
        }

        getAnalyzer().close();
        initLucene(getIndexDir());
        initDataConnector();
    }

    private ArrayList<InteractionNetwork> getTopNetworks(
            RelatedGenesEngineResponseDto response,
            Collection<Long> coexpNetworks) {

        ObjectSelector<InteractionNetwork> selector = new ObjectSelector<InteractionNetwork>();

        for (NetworkDto nwDto: response.getNetworks()) {
            long id = nwDto.getId();
            double weight = nwDto.getWeight();
            weight = -1*weight; // since the selector gets the smallest and we want the largest
            InteractionNetwork nw = dataConnector.getNetworkMediator().getNetwork(id);
            InteractionNetworkGroup group = dataConnector.getNetworkMediator().getNetworkGroupForNetwork(id);
            if (group.getCode().equalsIgnoreCase(COEXP_GROUP_CODE)) {
                selector.add(nw, weight);                
            }
        }

        ObjectSelector<InteractionNetwork> result = selector.selectLevelledSmallestScores(numCoexp, numCoexp);
        return result.getElements();
    }

    Collection<Long> getDefaultGeneList(Organism organism) {
        ArrayList<Long> ids = new ArrayList<Long>();

        for (Gene gene : organism.getDefaultGenes()) {
            ids.add(gene.getNode().getId());
        }

        return ids;
    }

    /*
     * TODO
     */
    Collection<Long> getDefaultAttributeGroups(Organism organism) {
        return new ArrayList<Long>();
    }

    /*
     * the request is all networks marked as default in data set, plus all coexp
     * networks whether or not they are default. run using BP combining.
     */
    RelatedGenesEngineRequestDto buildRequest(Organism organism,
            Collection<Collection<Long>> networkIds) {
        RelatedGenesEngineRequestDto request = new RelatedGenesEngineRequestDto();
        request.setOrganismId(organism.getId());
        request.setAttributeGroups(new ArrayList<Long>());
        request.setCombiningMethod(CombiningMethod.BP);
        request.setScoringMethod(ScoringMethod.DISCRIMINANT);
        request.setInteractionNetworks(networkIds);
        request.setPositiveNodes(getDefaultGeneList(organism));
        request.setAttributeGroups(getDefaultAttributeGroups(organism));
        request.setProgressReporter(NullProgressReporter.instance());

        return request;
    }

    public boolean getCommandLineArgs(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err
            .println("java -jar myprogram.jar [options...] arguments...");
            parser.printUsage(System.err);
            return false;
        }

        return true;
    }

    @Override
    public void init() throws Exception {
        super.init();
        mania2 = new Mania2(cache);
    }

    public static void main(String[] args) throws Exception {

        DefaultNetworkSelector selector = new DefaultNetworkSelector();
        if (!selector.getCommandLineArgs(args)) {
            System.exit(1);
        }

        try {
            selector.init();
            selector.process();
            selector.cleanup();
        } catch (Exception e) {
            logger.error("Fatal error", e);
            System.exit(1);
        }
    }
}
