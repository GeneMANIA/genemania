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

package org.genemania.plugin.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.AttributeMediator;
import org.genemania.plugin.apps.IQueryErrorHandler;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.impl.InteractionNetworkGroupImpl;
import org.genemania.plugin.model.impl.InteractionNetworkImpl;
import org.genemania.plugin.model.impl.QueryAttributeGroupImpl;
import org.genemania.plugin.model.impl.QueryAttributeNetworkImpl;

/**
 * Parses a tab-delimited file containing query parameters for performing a
 * single prediction.  The format is as follows:
 * 
 *     organism
 *     query-gene1 [ \t query-gene2 ... ]
 *     networks
 *     related-gene-limit
 *     combining-method
 */
public class TabDelimitedQueryParser extends AbstractQueryParser {
    public Query parse(DataSet data, Reader reader, IQueryErrorHandler handler) throws IOException {
		BufferedReader in = new BufferedReader(reader);
		
		try {
			Query query = new Query();
			String organismData = in.readLine();
			String filtered = organismData.toLowerCase();
			
			Organism organism = parseOrganism(data, filtered);
			if (organism == null) {
				throw new IOException(String.format("Cannot find organism: %s", organismData)); //$NON-NLS-1$
			}
			query.setOrganism(organism);
			
			List<String> genes = parseGenes(in.readLine());
			query.setGenes(genes);
			
			String networkData = in.readLine();
			AttributeMediator attributeMediator = data.getMediatorProvider().getAttributeMediator();
			Collection<Group<?, ?>> groups = parseNetworks(networkData, organism, "\t", handler, attributeMediator); //$NON-NLS-1$
			if (groups.size() == 0) {
				throw new IOException(String.format("Unrecognized network(s): %s", networkData)); //$NON-NLS-1$
			}
			for (Group<?, ?> group : groups) {
				{
					Group<InteractionNetworkGroup, InteractionNetwork> adapted = group.adapt(InteractionNetworkGroup.class, InteractionNetwork.class);
					if (adapted != null) {
						for (Network<InteractionNetwork> network : adapted.getNetworks()) {
							handler.handleNetwork(network.getModel());
						}
					}
				}
			}
			query.setGroups(groups);
			
			String limits = in.readLine();
			setLimits(query, limits);
			
			String combiningMethod = in.readLine();
			
			Set<Long> nodes = new HashSet<Long>();
			GeneCompletionProvider2 provider = data.getCompletionProvider(organism);
			for (String gene : genes) {
				Long nodeId = provider.getNodeId(gene);
				if (nodeId == null) {
					handler.handleUnrecognizedGene(gene);
				} else if (nodes.contains(nodeId)) {
					handler.handleSynonym(gene);
				} else {
					nodes.add(nodeId);
				}
			}
			if (nodes.size() == 0) {
				throw new IOException("None of your query genes were recognized"); //$NON-NLS-1$
			}
			query.setNodes(nodes);
			query.setCombiningMethod(parseCombiningMethod(combiningMethod, query, handler));
			return query;
		} catch (DataStoreException e) {
			throw new IOException(e.getMessage());
		} finally {
			in.close();
		}
	}
    
	private void setLimits(Query query, String limits) {
		Pattern pattern = Pattern.compile("(\\d+)(\t(\\d+))?"); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(limits);
		if (matcher.matches()) {
			String geneLimit = matcher.group(1);
			if (geneLimit != null) {
				query.setGeneLimit(Integer.parseInt(geneLimit));
			}
			String attributeLimit = matcher.group(3);
			if (attributeLimit != null) {
				query.setAttributeLimit(Integer.parseInt(attributeLimit));
			}
		}
	}

	protected List<String> parseGenes(String data) {
		String[] genes = data.split("\t"); //$NON-NLS-1$
		return Arrays.asList(genes);
	}
	
	public Collection<Group<?, ?>> parseNetworks(String networkData, Organism organism, String delimiter, IQueryErrorHandler handler, AttributeMediator attributeMediator) {
		if (networkData == null) {
			return Collections.emptyList();
		}
		
		Set<String> types = new HashSet<String>();
		Set<Long> networkIds = new HashSet<Long>();
		boolean useDefaults = false;
		boolean useAll = false;
		boolean useAllAttributes = false;
		
		Set<String> notHandled = new HashSet<String>();
		for (String item : networkData.split(delimiter)) {
			item = item.trim();
			types.add(item);
			notHandled.add(item);
			try {
				networkIds.add(Long.parseLong(item));
			} catch (NumberFormatException e) {
			}
			if (item.equalsIgnoreCase("default")) { //$NON-NLS-1$
				useDefaults = true;
			} else if (item.equalsIgnoreCase("preferred")) { //$NON-NLS-1$
				types.addAll(Arrays.asList(preferredGroupCodes));
			} else if (item.equalsIgnoreCase("all")) { //$NON-NLS-1$
				useAll = true;
			} else if (item.equalsIgnoreCase("Attributes")) { //$NON-NLS-1$
				useAllAttributes = true;
			}
		}
		
		notHandled.remove("default"); //$NON-NLS-1$
		notHandled.remove("preferred"); //$NON-NLS-1$
		notHandled.remove("all"); //$NON-NLS-1$
		notHandled.remove("Attributes"); //$NON-NLS-1$

		Set<Group<?, ?>> groupMembers = new HashSet<Group<?, ?>>();
		Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();
		for (InteractionNetworkGroup group : groups) {
			List<Network<InteractionNetwork>> networkMembers = new ArrayList<Network<InteractionNetwork>>();
			boolean useGroupCode = types.contains(group.getCode());
			if (useGroupCode) {
				notHandled.remove(group.getCode());
			}
			boolean useGroupName = types.contains(group.getName());
			if (useGroupName) {
				notHandled.remove(group.getName());
			}
			Collection<InteractionNetwork> networks = group.getInteractionNetworks();
			for (InteractionNetwork network : networks) {
				String key = findGroupAndNetworkKey(group, network, types);
				boolean useKey = key != null;
				if (useKey) {
					notHandled.remove(key);
				}
				boolean useNetworkName = types.contains(network.getName());
				if (useNetworkName) {
					notHandled.remove(network.getName());
				}
				boolean useNetworkId = networkIds.contains(network.getId());
				if (useNetworkId) {
					notHandled.remove(String.valueOf(network.getId()));
				}
				
				if (useAll
					|| useGroupCode
					|| useGroupName
					|| useNetworkId
					|| useNetworkName
					|| useKey
					|| useDefaults && network.isDefaultSelected()) {
					networkMembers.add(new InteractionNetworkImpl(network, 0));
				}
			}
			if (networkMembers.size() > 0) {
				groupMembers.add(new InteractionNetworkGroupImpl(group, networkMembers));
			}
		}
		
		List<Network<AttributeGroup>> networkMembers = new ArrayList<Network<AttributeGroup>>();
		for (AttributeGroup group : attributeMediator.findAttributeGroupsByOrganism(organism.getId())) {
			String key = findKey(group, types);
			boolean useKey = key != null;
			if (useKey) {
				notHandled.remove(key);
			}
			boolean useGroupName = types.contains(group.getName());
			if (useGroupName) {
				notHandled.remove(group.getName());
			}
			boolean useGroupCode = types.contains(group.getCode());
			if (useGroupCode) {
				notHandled.remove(group.getCode());
			}
			
			if (useAll
				|| useGroupCode
				|| useGroupName
				|| useKey
				|| useAllAttributes) {
				networkMembers.add(new QueryAttributeNetworkImpl(group, 0));
			}
		}
		if (networkMembers.size() > 0) {
			groupMembers.add(new QueryAttributeGroupImpl(networkMembers));
		}
		
		for (String item : notHandled) {
			handler.handleUnrecognizedNetwork(item);
		}
		return groupMembers;
	}

	private String findKey(AttributeGroup group, Set<String> types) {
		String[] groupNames = new String[] { group.getCode(), group.getName() };
		for (String groupName : groupNames) {
			String key = String.format("Attributes|%s", groupName); //$NON-NLS-1$
			if (types.contains(key)) {
				return key;
			}
		}
		return null;
	}

	private String findGroupAndNetworkKey(InteractionNetworkGroup group, InteractionNetwork network, Set<String> types) {
		String[] groupNames = new String[] { group.getCode(), group.getName() };
		for (String groupName : groupNames) {
			String key = String.format("%s|%s", groupName, network.getName()); //$NON-NLS-1$
			if (types.contains(key)) {
				return key;
			}
		}
		return null;
	}
}
