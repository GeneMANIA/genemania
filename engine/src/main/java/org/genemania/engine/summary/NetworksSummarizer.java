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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.engine.apps.DatasetSummarizer;
import org.genemania.engine.apps.support.DataConnector;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.Network;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.exception.ApplicationException;

/*
 * generates a report of # of networks, organized by group
 * and a separate report of statistics for each network, including
 * # of interactions, # of genes, etc
 *
 * TODO: describe report format
 */
public class NetworksSummarizer implements Summarizer {
    private static Logger logger = Logger.getLogger(NetworksSummarizer.class);

	Organism organism;
	DataConnector dataConnector;
	PreferredNames preferredNames;

	// summarization info
	Map<String, Integer> countsByGroup;
	int uniqueNetworks;

	Reporter networkReporter;
	Reporter networkDegreeReporter;

	public NetworksSummarizer(Organism organism, DataConnector dataConnector) throws Exception {
		this.organism = organism;
		this.dataConnector = dataConnector;

		this.preferredNames = new PreferredNames(organism.getId(), dataConnector, DatasetSummarizer.preferredNamesList);
	}

	@Override
    public void summarize(ReporterFactory reporterFactory) throws Exception {
		logger.info(String.format("summarizing networks for organism %d - %s", organism.getId(), organism.getName()));

		networkReporter = reporterFactory.getReporter("networks");
		networkDegreeReporter = reporterFactory.getReporter("networkDegrees");

		try {
			networkReporter.init("Network Group ID", "Network Group Name", "Network ID", "Network Name", "Num Nodes", "Num Edges", "Source", "Source URL");
			networkDegreeReporter.init("Network ID", "Node ID", "Symbol", "Degree", "Interactors");

			Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();

			for (InteractionNetworkGroup group: groups) {
				summerizeGroup(group);
			}

		}
		finally {
			networkDegreeReporter.close();
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
        	summarizeNetwork(network, group);
        }
    }

	void reportGroups(ReporterFactory reporterFactory) throws Exception {
		Reporter reporter = reporterFactory.getReporter("network_groups");
		try {
	        reporter.init(new String[] {"Group name", "# of networks"});
			ArrayList<String> allGroups = new ArrayList<String>();
			allGroups.addAll(countsByGroup.keySet());
			Collections.sort(allGroups);

			int numGroups = allGroups.size();
			int numNetworks = 0;

			for (String group : allGroups) {
				int count = countsByGroup.get(group);
				numNetworks += count;
				reporter.write(group, "" + count);
			}

			reporter.write("total networks", "" + numNetworks);
			reporter.write("total groups", "" + numGroups);
		} finally {
			reporter.close();
		}
	}

	void reportNetworks(ReporterFactory reporterFactory) throws Exception {
		// one day, perhaps
	}

	void summarizeNetwork(InteractionNetwork network, InteractionNetworkGroup group) throws ApplicationException {
		Network networkData = dataConnector.getCache().getNetwork(Data.CORE, organism.getId(), network.getId());
		long expectedNumInteractions = network.getMetadata().getInteractionCount();

		NetworkStats stats = new NetworkStats(networkData);
		if (expectedNumInteractions != stats.getNumEdges()) {
			logger.warn(String.format("inconsistent number of interactions in network %d - %s (expected %d got %d)", network.getId(), network.getName(), expectedNumInteractions, stats.getNumEdges()));
		}

		network.getMetadata().getSource();


		networkReporter.write("" + group.getId(), group.getName(), "" + network.getId(), network.getName(), "" + stats.getNumInteractingNodes(), "" + stats.getNumEdges(), network.getMetadata().getSource(), network.getMetadata().getSourceUrl());

		List<GeneDataHolder> holders = buildOrderedGeneList(stats); // TODO: don't need to sort anymore?

		for (GeneDataHolder holder: holders) {
			networkDegreeReporter.write("" + network.getId(), "" + holder.nodeId, holder.name, "" + holder.degree, "" + holder.numInteractors);
		}
	}

	List<GeneDataHolder> buildOrderedGeneList(NetworkStats stats) throws ApplicationException {

		NodeIds nodeIds = dataConnector.getCache().getNodeIds(organism.getId());

		List<GeneDataHolder> holders = new ArrayList<GeneDataHolder>();

		for (int i=0; i<stats.getNodeDegrees().getSize(); i++) {
			double degree = stats.getNodeDegrees().get(i);

			if (degree > 0) {
				long nodeId = nodeIds.getIdForIndex(i);

				GeneDataHolder holder = new GeneDataHolder();
				holder.nodeId = nodeId;
				holder.name = preferredNames.getName(nodeId);
				holder.degree = degree;
				holder.numInteractors = stats.getNodeInteractorsCount()[i];

				holders.add(holder);
			}
		}

		Collections.sort(holders, HolderComparator.getInstance());
		return holders;
	}

	static class GeneDataHolder {
		String name;
		long nodeId;
		double degree;
		int numInteractors;
	}

	static class HolderComparator implements Comparator<GeneDataHolder> {
		static HolderComparator instance = new HolderComparator();
		@Override
        public int compare(GeneDataHolder arg0, GeneDataHolder arg1) {
			return Collator.getInstance().compare(arg0.name, arg1.name);
		}

		public static HolderComparator getInstance() {
			return instance;
		}
	}


	@Override
    public void setUp() throws Exception {
		countsByGroup = new HashMap<String, Integer>();
		uniqueNetworks = 0;
	}

	@Override
    public void tearDown() throws Exception {
		// TODO Auto-generated method stub

	}
}
