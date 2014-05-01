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
import java.util.Map;

import org.apache.log4j.Logger;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.engine.apps.support.DataConnector;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.Network;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.matricks.MatrixCursor;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;

/*
 * Create a data dump of a given db release from binary artifacts,
 * suitable for uploading to a public ftp site for distribution. 
 * Format spec'd in google doc. 
 * 
 */
public class NetworksDumper implements Summarizer {
    private static Logger logger = Logger.getLogger(NetworksDumper.class);	

    Organism organism;
    DataConnector dataConnector;
    PreferredNames preferredNames;

    // summarization info
    Map<String, Integer> countsByGroup;
    int uniqueNetworks;

    ReporterFactory reporterFactory;
    Reporter networkReporter;
    Reporter interactionReporter;

    public NetworksDumper(Organism organism, DataConnector dataConnector, PreferredNames preferredNames) throws Exception {
        this.organism = organism;
        this.dataConnector = dataConnector;
        this.preferredNames = preferredNames;
    }

    public void summarize(ReporterFactory reporterFactory) throws Exception {
        logger.info(String.format("summarizing networks for organism %d - %s", organism.getId(), organism.getName()));   

        this.reporterFactory = reporterFactory;
        networkReporter = reporterFactory.getReporter("networks");

        try {
            networkReporter.init("File_Name", "Network_Group_Name", "Network_Name", "Source", "Pubmed_ID");

            Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();

            for (InteractionNetworkGroup group: groups) {
                summerizeGroup(group);
            }

        }
        finally {
            networkReporter.close();
        }
    }

    void summerizeGroup(InteractionNetworkGroup group) throws ApplicationException {
        Collection<InteractionNetwork> networks = group.getInteractionNetworks();

        // group summary
        String groupName = group.getName();
        int numNetworks = networks.size();
        countsByGroup.put(groupName, numNetworks);

        // statistics for all the networks in this group
        for (InteractionNetwork network: networks) {
            reportNetwork(network, group);
        }
    }

    public String makeFileName(InteractionNetwork network, InteractionNetworkGroup group) {
        String name = group.getName() + "." + network.getName();
        name = name.replace(' ', '_');
        name = normalizeString(name);
        return name;
    }

    /*
     * get rid of accented chars
     */
    public static String normalizeString(String s) {
        s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        s = s.replaceAll("[^\\p{ASCII}]","");
        return s;
    }

    void reportNetwork(InteractionNetwork network, InteractionNetworkGroup group) throws ApplicationException {

        String name = makeFileName(network, group);		
        long numEdges = 0;
        long expectedNumInteractions = 0;
        DecimalFormat formatter = new DecimalFormat("0.#E0"); // couple digits
        NodeIds nodeIds = dataConnector.getCache().getNodeIds(organism.getId());
        Network networkData = dataConnector.getCache().getNetwork(Data.CORE, organism.getId(), network.getId());

        name = dumpNetwork(networkData.getData(), reporterFactory, name, preferredNames, nodeIds);
        networkReporter.write(name, group.getName(), normalizeString(network.getName()), network.getMetadata().getSource(), network.getMetadata().getPubmedId());

    }

    public static String dumpNetwork(SymMatrix nw, ReporterFactory reporterFactory, String reportName, 
            PreferredNames preferredNames, NodeIds nodeIds) throws ApplicationException {
        DecimalFormat formatter = new DecimalFormat("0.#E0"); // couple digits

        Reporter reporter = reporterFactory.getReporter(reportName);
        reportName = reporter.getReportName(); // extension
        try {
            reporter.init("Gene_A", "Gene_B", "Weight");
            MatrixCursor cursor = nw.cursor();
            while (cursor.next()) {
                if (cursor.val() != 0 && cursor.col() < cursor.row()) {
                    long nodeIdA = nodeIds.getIdForIndex(cursor.col());
                    long nodeIdB = nodeIds.getIdForIndex(cursor.row());
                    String symbolA = preferredNames.getName(nodeIdA);
                    String symbolB = preferredNames.getName(nodeIdB);
                    double weight = cursor.val();

                    reporter.write(symbolA, symbolB, formatter.format(weight));
                }
            }			
        }
        finally {
            reporter.close();
        }

        return reportName;
    }

    public void setUp() throws Exception {
        countsByGroup = new HashMap<String, Integer>();
        uniqueNetworks = 0;		
    }

    public void tearDown() throws Exception {
        // TODO Auto-generated method stub	
    }
}
