/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2008-2011 University of Toronto.
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

package org.genemania.plugin.apps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Gene;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.NodeMediator;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class AbstractPluginDataApp extends AbstractPluginApp {
	
	protected DataSet fData;

	@Option(name = "--data", required = true, usage = "path to a GeneMANIA data set (e.g. gmdata-2010-06-24)")
	protected String fDataPath;

	@Option(name = "--threads", usage = "number of parallel threads (default = 1)")
	protected int fThreads = 1;
	
	@Option(name = "--verbose", usage = "give more details about what's happening")
	protected boolean fVerbose;
	
	@Argument
	protected List<String> fArguments = new ArrayList<>();

	protected List<String> getArguments() {
		return fArguments;
	}

	protected int getThreads() {
		return fThreads;
	}
	
	protected String getDataPath() {
		return fDataPath;
	}
	
	protected InteractionNetworkGroup parseGroup(Organism organism, String name) {
		for (InteractionNetworkGroup group : organism.getInteractionNetworkGroups()) {
			if (group.getName().equals(name))
				return group;
			if (group.getCode().equals(name))
				return group;
		}
		return null;
	}
	
	protected Collection<Collection<Long>> collapseNetworks(Collection<? extends Group<?, ?>> networks) {
		Collection<Collection<Long>> result = new ArrayList<>();
		List<Group<InteractionNetworkGroup, InteractionNetwork>> groups = new ArrayList<>();
		
		for (Group<?, ?> group : networks) {
			Group<InteractionNetworkGroup, InteractionNetwork> adapted = group.adapt(InteractionNetworkGroup.class, InteractionNetwork.class);
			if (adapted == null) {
				continue;
			}
			groups.add(adapted);
		}
		
		Collections.sort(groups, (Group<InteractionNetworkGroup, InteractionNetwork> g1, Group<InteractionNetworkGroup, InteractionNetwork> g2) -> {
			return (int) Math.signum(g1.getModel().getId() - g2.getModel().getId());
		});
		
		for (Group<InteractionNetworkGroup, InteractionNetwork> group : groups) {
			Collection<Long> groupMembers = new HashSet<>();
			
			for (Network<InteractionNetwork> network : group.getNetworks()) {
				groupMembers.add(network.getModel().getId());
			}
			
			if (!groupMembers.isEmpty()) {
				List<Long> sorted = new ArrayList<>(groupMembers);
				Collections.sort(sorted);
				result.add(sorted);
			}
		}
		
		return result;
	}
	
	protected Collection<Long> collapseAttributeGroups(Collection<Group<?, ?>> groups) {
		List<Long> result = new ArrayList<>();
		
		for (Group<?, ?> group : groups) {
			Group<Object, AttributeGroup> adapted = group.adapt(Object.class, AttributeGroup.class);
			
			if (adapted == null)
				continue;
			
			for (Network<AttributeGroup> network : adapted.getNetworks()) {
				result.add(network.getModel().getId());
			}
		}
		return result;
	}

	protected void printNetworks(String organismName) throws DataStoreException {
		Organism organism = parseOrganism(fData, organismName);
		
		if (organism == null)
			System.err.println(String.format("Unrecognized organism: %s", organismName)); //$NON-NLS-1$
		
		Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();
		
		for (InteractionNetworkGroup group : groups) {
			Collection<InteractionNetwork> networks = group.getInteractionNetworks();
			
			for (InteractionNetwork network : networks) {
				System.out.println(network.getName());
			}
		}
	}
	
	protected void printGenes(String organismName) throws DataStoreException {
		Organism organism = parseOrganism(fData, organismName);
		
		if (organism == null)
			System.err.println(String.format("Unrecognized organism: %s", organismName)); //$NON-NLS-1$
		
		long organismId = organism.getId();
		List<Long> nodeIds = fData.getNodeIds(organismId);
		NodeMediator mediator = fData.getMediatorProvider().getNodeMediator();
		
		for (long nodeId : nodeIds) {
			Node node = mediator.getNode(nodeId, organismId);
			List<Gene> genes = new ArrayList<>(node.getGenes());
			Collections.sort(genes, (Gene g1, Gene g2) -> {
				return g2.getNamingSource().getRank() - g1.getNamingSource().getRank();
			});
			
			boolean first = true;
			
			for (Gene gene : genes) {
				if (!first)
					System.out.print("\t"); //$NON-NLS-1$
				
				System.out.print(gene.getSymbol());
				first = false;
			}
			
			System.out.println();
		}
	}
}
