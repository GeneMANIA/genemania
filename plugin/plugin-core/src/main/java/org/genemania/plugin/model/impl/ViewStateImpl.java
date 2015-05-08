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
package org.genemania.plugin.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Node;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.model.ViewStateBuilder;

public class ViewStateImpl implements ViewStateBuilder {
	
	private final Map<String, Set<Network<?>>> networksByEdge;
	private final Map<String, Set<Network<?>>> networksByNode;
	private final Map<Group<?, ?>, Set<String>> edgeCache;
	private final Map<String, Long> nodeCache;
	private final Set<Group<?, ?>> enabledGroups;
	private final Set<Group<?, ?>> highlightedGroups;
	private final Set<Network<?>> highlightedNetworks; 
	private final Set<String> highlightedNodes;
	private final Map<Network<?>, Group<?, ?>> groupsByNetwork;
	private final Map<String, Group<?, ?>> groupsByName;
	
	private String mostRecentNode;
	private Group<?, ?> mostRecentGroup;
	private SearchResult searchResult;
	
	public ViewStateImpl(SearchResult result) {
		searchResult = result;
		edgeCache = new HashMap<Group<?, ?>, Set<String>>();
		nodeCache = new WeakHashMap<String, Long>();
		enabledGroups = new HashSet<Group<?, ?>>();
		highlightedGroups = new HashSet<Group<?, ?>>();
		highlightedNetworks = new HashSet<Network<?>>();
		highlightedNodes = new HashSet<String>();
		networksByEdge = new HashMap<String, Set<Network<?>>>();
		networksByNode = new HashMap<String, Set<Network<?>>>();
		groupsByName = new HashMap<String, Group<?, ?>>();
		groupsByNetwork = new HashMap<Network<?>, Group<?, ?>>();
		
		addGroups(result);
	}

	private void addGroups(SearchResult result) {
		{
			Map<InteractionNetwork, Double> weights = result.getNetworkWeights();
			for (InteractionNetworkGroup model : result.getInteractionNetworkGroups().values()) {
				Collection<Network<InteractionNetwork>> networks = new ArrayList<Network<InteractionNetwork>>();
				for (InteractionNetwork network : model.getInteractionNetworks()) {
					Double weight = weights.get(network);
					if (weight == null) {
						continue;
					}
					networks.add(new InteractionNetworkImpl(network, weight));
				}
				
				InteractionNetworkGroupImpl group = new InteractionNetworkGroupImpl(model, networks);
				groupsByName.put(model.getName(), group);
				
				for (Network<?> network : group.getNetworks()) {
					groupsByNetwork.put(network, group);
				}
			}
		}
		{
			Map<Attribute, Double> weights = result.getAttributeWeights();
			if (weights != null) {
				Map<AttributeGroup, Collection<Network<Attribute>>> networksByGroup = new HashMap<AttributeGroup, Collection<Network<Attribute>>>();
				for (Entry<Attribute, Double> entry : weights.entrySet()) {
					Attribute attribute = entry.getKey();
					AttributeGroup group = result.getAttributeGroup(attribute.getId());
					Collection<Network<Attribute>> networks = networksByGroup.get(group);
					if (networks == null) {
						networks = new ArrayList<Network<Attribute>>();
						networksByGroup.put(group,  networks);
					}
					networks.add(new ResultAttributeNetworkImpl(attribute, entry.getValue()));
				}
				
				for (Entry<AttributeGroup, Collection<Network<Attribute>>> entry : networksByGroup.entrySet()) {
					ResultAttributeGroupImpl group = new ResultAttributeGroupImpl(entry.getKey(), entry.getValue());
					groupsByName.put(group.getName(), group);
					
					for (Network<?> network : group.getNetworks()) {
						groupsByNetwork.put(network, group);
					}
					setEnabled(group, true);
				}
				
			}
		}
	}

	@Override
	public boolean isEnabled(Group<?, ?> group) {
		return enabledGroups.contains(group);
	}
	
	@Override
	public void setEnabled(Group<?, ?> group, boolean enabled) {
		if (enabled) {
			enabledGroups.add(group);
		} else {
			enabledGroups.remove(group);
		}
	}
	
	@Override
	public void setGeneHighlighted(String name, boolean highlighted) {
		if (name == null) {
			return;
		}
		if (highlighted) {
			highlightedNodes.add(name);
			mostRecentNode = name;
		} else {
			highlightedNodes.remove(name);
		}
	}

	@Override
	public void setGroupHighlighted(Group<?, ?> group, boolean highlighted) {
		if (highlighted) {
			highlightedGroups.add(group);
			mostRecentGroup = group;
		} else {
			highlightedGroups.remove(group);
		}
	}

	public void setNetworkHighlighted(Network<?> network, boolean highlighted) {
		if (highlighted) {
			highlightedNetworks.add(network);
		} else {
			highlightedNetworks.remove(network);
		}
	}
	
	@Override
	public boolean isGeneHighlighted(String name) {
		return highlightedNodes.contains(name);
	}

	@Override
	public boolean isGroupHighlighted(Group<?, ?> group) {
		return highlightedGroups.contains(group);
	}
	
	@Override
	public boolean isNetworkHighlighted(Network<?> network) {
		return highlightedNetworks.contains(network);
	}
	
	@Override
	public int getTotalHighlightedGenes() {
		return highlightedNodes.size();
	}
	
	@Override
	public String getMostRecentNode() {
		return mostRecentNode;
	}
	
	@Override
	public Group<?, ?> getMostRecentGroup() {
		return mostRecentGroup;
	}

	@Override
	public void clearHighlightedNetworks() {
		highlightedNetworks.clear();
	}
	
	@Override
	public void clearHighlightedGroups() {
		highlightedGroups.clear();
	}

	@Override
	public Group<?, ?> getGroup(String name) {
		return groupsByName.get(name);
	}
	
	@Override
	public Long getNodeId(String nodeId) {
		return nodeCache.get(nodeId);
	}

	@Override
	public Set<Network<?>> getNetworksByEdge(String edgeId) {
		Set<Network<?>> networks = networksByEdge.get(edgeId);
		if (networks == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(networks);
	}
	
	@Override
	public Set<Network<?>> getNetworksByNode(String nodeId) {
		Set<Network<?>> networks = networksByNode.get(nodeId);
		if (networks == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(networks);
	}

	@Override
	public Group<?, ?> getGroup(Network<?> network) {
		return groupsByNetwork.get(network);
	}
	
	@Override
	public ViewState build() {
		return this;
	}
	
	@Override
	public void addEdge(Group<?, ?> group, String edgeId) {
		Set<String> edgeIds = edgeCache.get(group);
		if (edgeIds == null) {
			edgeIds = new HashSet<String>();
			edgeCache.put(group, edgeIds);
		}
		edgeIds.add(edgeId);
		if (group != null) {
			addEdge(null, edgeId);
		}
	}

	@Override
	public Set<String> getEdgeIds(Group<?, ?> group) {
		return edgeCache.get(group);
	}
	
	@Override
	public void addSourceNetworkForEdge(String edgeId, Network<?> network) {
		Set<Network<?>> networks = networksByEdge.get(edgeId);
		if (networks == null) {
			networks = new HashSet<Network<?>>();
			networksByEdge.put(edgeId, networks);
		}
		networks.add(network);
	}
	
	@Override
	public void addSourceNetworkForNode(String nodeId, Network<?> network) {
		Set<Network<?>> networks = networksByNode.get(nodeId);
		if (networks == null) {
			networks = new HashSet<Network<?>>();
			networksByNode.put(nodeId, networks);
		}
		networks.add(network);
	}
	
	@Override
	public void addNode(Node node, String nodeId) {
		nodeCache.put(nodeId, node.getId());
	}
	
	@Override
	public SearchResult getSearchResult() {
		return searchResult;
	}
	
	@Override
	public Set<Group<?, ?>> getAllGroups() {
		return new HashSet<Group<?, ?>>(groupsByName.values());
	}
}
