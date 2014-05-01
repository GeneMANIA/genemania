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
import java.util.Date;

import org.apache.log4j.Logger;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.NetworkMetadata;
import org.genemania.domain.Organism;
import org.genemania.engine.apps.AbstractEngineApp;
import org.genemania.engine.config.Config;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.Network;
import org.genemania.engine.matricks.MatrixCursor;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.engine.summary.ReporterFactory;
import org.genemania.engine.summary.Summarizer;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.lucene.exporter.IndexUpdater;
import org.kohsuke.args4j.Option;

/*
 * Sparsification is normally performed as part of a networks
 * generation, e.g. when using P2N. Here we perform a second
 * round of sparsification, based on overlap of interactions
 * in an entire dataset. As such, it should only be performed
 * once the initial version of the entire dataset is computed
 * (so after cachebuilder) but before other steps such as
 * compute node degrees, combined networks etc.
 * 
 * first an overlap matrix is computed for the entire dataset,
 * counting the # of networks each interaction appears in. then
 * all the networks in the given group (e.g. coexp) are sparsified
 * one at a time, so that all interactions that appear only in
 * that network (that is, have a count < 2 in the combined network)
 * are removed. The network data is overwritten for the sparsified
 * networks is overwritten.
 * 
 * the interaction counts in the lucene index are updated with the
 * new values. 
 */
public class PostSparsifier extends AbstractEngineApp {
    private static Logger logger = Logger.getLogger(PostSparsifier.class);

    long total = 0;

    @Option(name = "-orgId", usage = "optional organism id, otherwise will process all oganisms")
    private Long orgId = null;
    @Option(name = "-group", usage = "network group to sparsify specified by short code, defaults to coexp")
    private String group = "coexp";
    @Option(name = "-thresh", usage = "interaction count below which interactions will be pruned, defaults to 2")
    private int threshold = 2;

    private String reportSubdir; // outputs here

    public PostSparsifier() {
        super();
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    void summarize(Summarizer summarizer, ReporterFactory reporterFactory)
            throws Exception {
        summarizer.setUp();
        try {
            summarizer.summarize(reporterFactory);
        } finally {
            summarizer.tearDown();
        }
    }

    public void init() throws Exception {
        super.init();
    }

    /*
     * compute a structural overlap matrix for a selection of networks for the
     * given organism.
     */
    Collection<UpdateRecord> processOrganism(Organism organism)
            throws Exception {
        total = 0;

        ArrayList<UpdateRecord> networksForSparsification = new ArrayList<UpdateRecord>();
        networksForSparsification.addAll(getNetworks(group, organism));
        if (networksForSparsification.size() == 0) {
            logger.warn("no networks to be sparsified for " + organism.getName());
            return networksForSparsification;
        }
        
        logger.info("computing overlap");
        ArrayList<UpdateRecord> allNetworks = new ArrayList<UpdateRecord>();
        allNetworks.addAll(getNetworks("*", organism));
        SymMatrix overlap = computeOverlap(organism, allNetworks);

        logger.info("sparsifying");
        sparsifyAll(organism, networksForSparsification, overlap);

        return networksForSparsification;
    }

    /*
     * compute an overlap count matrix for the given set of networks
     */
    public SymMatrix computeOverlap(Organism organism,
            ArrayList<UpdateRecord> networksList) throws Exception {
        SymMatrix overlap = null;

        for (UpdateRecord record : networksList) {

            Network network = getCache().getNetwork(Data.CORE,
                    organism.getId(), record.networkId);
            SymMatrix iData = network.getData();

            if (overlap == null) {
                int size = iData.numRows(); // should be square
                overlap = Config.instance().getMatrixFactory()
                        .symSparseMatrix(size);
            }

            addCount(overlap, iData);
        }

        System.out.println(String.format("interactions total: %d", total));
        return overlap;
    }

    public void sparsifyAll(Organism organism,
            ArrayList<UpdateRecord> networksList, SymMatrix overlap)
            throws Exception {

        for (UpdateRecord record : networksList) {

            Network network = getCache().getNetwork(Data.CORE,
                    organism.getId(), record.networkId);
            SymMatrix networkData = network.getData();
            logger.info("sparsifying network " + record.networkId);
            SymMatrix sparsifiedNetworkData = sparsify(record, networkData,
                    overlap);

            network.setData(sparsifiedNetworkData);
            getCache().putNetwork(network);
        }
    }

    /*
     * apply sparsification based on threshold param and accumulated overlap
     * counts
     */
    public SymMatrix sparsify(UpdateRecord record, SymMatrix network,
            SymMatrix overlap) {
        SymMatrix sparsifiedNetwork = Config.instance().getMatrixFactory()
                .symSparseMatrix(network.numRows());

        MatrixCursor cursor = network.cursor();
        int r, c;
        int keepers = 0, removed = 0;

        while (cursor.next()) {

            r = cursor.row();
            c = cursor.col();
            if (r <= c) { // don't need symmetric elements
                continue;
            }

            if (overlap.get(r, c) >= threshold) {
                sparsifiedNetwork.set(r, c, cursor.val());
                keepers += 1;
            } else {
                removed += 1;
            }
        }

        sparsifiedNetwork.compact();

        record.newCount = keepers;
        logger.info(String.format("kept %d, removed %d, reduction %5f%%",
                keepers, removed, removed * 100d / (keepers + removed)));
        return sparsifiedNetwork;
    }

    /*
     * accumulate interaction counts
     */
    public void addCount(SymMatrix counts, SymMatrix m) {

        MatrixCursor cursor = m.cursor();
        int r, c;

        while (cursor.next()) {

            r = cursor.row();
            c = cursor.col();
            if (r <= c) { // don't need symmetric elements
                continue;
            }

            double v = cursor.val();
            if (v > 0) {
                total += 1;
                counts.add(r, c, 1);
            }
        }
    }

    @Override
    public void process() throws Exception {

        // get dataset date, used for top level report dir
        Date datasetDate = getStatsMediator().getLatestStatistics().getDate();
        System.out.println(datasetDate);
        logger.info("writing report to " + reportSubdir);

        Collection<UpdateRecord> allUpdateRecords = new ArrayList<UpdateRecord>();
        if (orgId != null) {
            Organism organism = getDataConnector().getOrganismMediator()
                    .getOrganism(orgId);
            allUpdateRecords.addAll(processOrganism(organism));
        } else {
            for (Organism organism : getDataConnector().getOrganismMediator()
                    .getAllOrganisms()) {
                logger.info(String.format("Organism %d: %s", organism.getId(),
                        organism.getName()));
                allUpdateRecords.addAll(processOrganism(organism));
            }
        }

        updateCounts(allUpdateRecords);
    }

    /*
     * select networks by group code
     */
    private Collection<UpdateRecord> getNetworks(String groupCode,
            Organism organism) throws ApplicationException, DataStoreException {

        Collection<InteractionNetworkGroup> groups = organism
                .getInteractionNetworkGroups();
        Collection<UpdateRecord> records = new ArrayList<UpdateRecord>();

        for (InteractionNetworkGroup group : groups) {

            System.out.println(group.getName());
            if (groupCode.equals("*") || group.getCode().equals(groupCode)) {
                Collection<InteractionNetwork> networks = group
                        .getInteractionNetworks();

                for (InteractionNetwork n : networks) {
                    NetworkMetadata metadata = n.getMetadata();

                    logger.debug(String
                            .format("using network %d containing %d interactions from group %s: %s",
                                    n.getId(), metadata.getInteractionCount(),
                                    group.getName(), n.getName()));

                    UpdateRecord record = new UpdateRecord(organism.getId(),
                            n.getId(), metadata.getInteractionCount(), 0);
                    records.add(record);
                }
            }
        }

        if (records.size() == 0) {
            throw new ApplicationException("no networks found!");
        }

        logger.info(String.format("total %d networks selected", records.size()));
        return records;
    }

    class UpdateRecord {
        long organismId;
        long networkId;
        long oldCount;
        long newCount;

        UpdateRecord(long organismId, long networkId, long oldCount,
                long newCount) {
            this.organismId = organismId;
            this.networkId = networkId;
            this.oldCount = oldCount;
            this.newCount = newCount;
        }
    }

    private void updateCounts(Collection<UpdateRecord> records)
            throws IOException {

        getSearcher().close();
        IndexUpdater updater = new IndexUpdater(new File(getIndexDir()),
                getAnalyzer());

        for (UpdateRecord record : records) {
            if (record.oldCount != record.newCount) {
                updater.updateNetworkStats(record.organismId, record.networkId,
                        record.newCount);
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        try {
            PostSparsifier instance = new PostSparsifier();

            // load up command line arguments
            instance.getCommandLineArgs(args);
            instance.setupLogging();

            instance.init();
            instance.process();
            instance.cleanup();
            logger.info("Sparsification completed.");
        } catch (Exception e) {
            logger.error("Fatal error", e);
            System.exit(1);
        }
    }
}
