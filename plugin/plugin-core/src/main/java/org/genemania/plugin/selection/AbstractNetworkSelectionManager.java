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
package org.genemania.plugin.selection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.genemania.domain.Attribute;
import org.genemania.domain.Gene;
import org.genemania.domain.Node;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.view.FunctionInfoPanel;
import org.genemania.plugin.view.GeneInfoPanel;
import org.genemania.plugin.view.NetworkChangeListener;
import org.genemania.plugin.view.NetworkGroupDetailPanel;
import org.genemania.plugin.view.components.BaseInfoPanel;

public abstract class AbstractNetworkSelectionManager implements NetworkSelectionManager {
	
	protected final Map<Long, ViewState> networkOptions;
	protected final CytoscapeUtils cytoscapeUtils;
	protected GeneMania plugin;

	protected Long selectedNetworkId;
	protected boolean selectionListenerEnabled;

	public AbstractNetworkSelectionManager(CytoscapeUtils cytoscapeUtils) {
	    this.cytoscapeUtils = cytoscapeUtils;
	    
	    networkOptions = new HashMap<>();
	    selectionListenerEnabled = true;
	}

	@Override
	public void setGeneMania(GeneMania geneMania) {
		plugin = geneMania;
	}
	
	@Override
	public Long getSelectedNetworkId() {
		return selectedNetworkId;
	}
	
	@Override
	public int getTotalNetworks() {
		return networkOptions.size();
	}
	
	@Override
	public void addNetworkConfiguration(CyNetwork network, ViewState config) {
		networkOptions.put(network.getSUID(), config);
	}

	@Override
	public ViewState getNetworkConfiguration(CyNetwork network) {
		return networkOptions.get(network.getSUID());
	}
	
	@Override
	public boolean isGeneManiaNetwork(CyNetwork network) {
		return networkOptions.get(network.getSUID()) != null;
	}
	
	@Override
	public NetworkChangeListener createChangeListener(Group<?, ?> group) {
		return new NetworkChangeListener(group, networkOptions, cytoscapeUtils);
	}
	
	@Override
	public void setSelectionListenerEnabled(boolean enabled) {
		selectionListenerEnabled = enabled;
	}

	@Override
	public boolean isSelectionListenerEnabled() {
		return selectionListenerEnabled;
	}
	
	@Override
	public SelectionListener<Gene> createGeneListSelectionListener(final GeneInfoPanel genePanel, final ViewState options) {
		return new SelectionListener<Gene>() {
			@Override
			public void selectionChanged(SelectionEvent<Gene> event) {
				if (!selectionListenerEnabled)
					return;
				
				boolean listenerState = selectionListenerEnabled;
				selectionListenerEnabled = false;
				CyNetwork network = cytoscapeUtils.getCurrentNetwork();
				
				for (Gene gene : event.items) {
					Node node = gene.getNode();
					CyNode cyNode = cytoscapeUtils.getNode(network, node, null);
					options.setGeneHighlighted(cytoscapeUtils.getIdentifier(network, cyNode), event.selected);
					cytoscapeUtils.setSelectedNode(network, cyNode, event.selected);
				}
				
				selectionListenerEnabled = listenerState;
				cytoscapeUtils.repaint();
			}
		};		
	}

	@Override
	public boolean checkSelectionState(CyEdge referenceEdge, Set<CyEdge> selectedEdges, CyNetwork network) {
		String target = cytoscapeUtils.getAttribute(network, referenceEdge, CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, String.class);
		
		if (target == null)
			return false;
		
		for (CyEdge edge : selectedEdges) {
			String id = cytoscapeUtils.getAttribute(network, edge, CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, String.class);
			
			if (id == null)
				continue;
			
			if (id == target)
				return false;
		}
		
		return true;
	}

	@Override
	public SelectionListener<Group<?, ?>> createNetworkListSelectionListener(
			final BaseInfoPanel<Group<?, ?>, NetworkGroupDetailPanel> panel, final ViewState options) {
		return new SelectionListener<Group<?, ?>>() {
			@Override
			public void selectionChanged(SelectionEvent<Group<?, ?>> event) {
				if (!selectionListenerEnabled)
					return;
				
				CyNetwork cyNetwork = cytoscapeUtils.getCurrentNetwork();
				ViewState options = networkOptions.get(cyNetwork.getSUID());
				
				if (options == null)
					return;
				
				Set<CyEdge> enabledEdges = new HashSet<>();
				Set<CyEdge> disabledEdges = new HashSet<>();
				Map<String, Boolean> selectionChanges = new HashMap<>();
				
				for (Group<?, ?> group : event.items) {
					selectionChanges.put(group.getName(), event.selected);
					options.setGroupHighlighted(group, event.selected);
				}
				
				for (CyEdge edge : cyNetwork.getEdgeList()) {
					String name = cytoscapeUtils.getAttribute(cyNetwork, edge, CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, String.class);
					Boolean selectionState = selectionChanges.get(name);
					
					if (selectionState == null) {
						cytoscapeUtils.setAttribute(cyNetwork, edge, CytoscapeUtils.HIGHLIGHT_ATTRIBUTE, 0);
						continue;
					}
					
					if (selectionState)
						enabledEdges.add(edge);
					else
						disabledEdges.add(edge);
					
					Group<?, ?> group = options.getGroup(name);
					boolean highlighted = selectionState || options.isEnabled(group);
					cytoscapeUtils.setAttribute(cyNetwork, edge, CytoscapeUtils.HIGHLIGHT_ATTRIBUTE, highlighted ? 1 : 0);
				}
				
				boolean listenerState = selectionListenerEnabled;
				selectionListenerEnabled = false;
				
				if (enabledEdges.size() > 0)
					cytoscapeUtils.setSelectedEdges(cyNetwork, enabledEdges, true);
				if (disabledEdges.size() > 0)
					cytoscapeUtils.setSelectedEdges(cyNetwork, disabledEdges, false);
				
					selectionListenerEnabled = listenerState;

				if (cytoscapeUtils.getSelectedEdges(cyNetwork).size() == 0) {
					for (CyEdge edge : cyNetwork.getEdgeList()) {
						String name = cytoscapeUtils.getAttribute(cyNetwork, edge, CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, String.class);
						Group<?, ?> group = options.getGroup(name);
						
						if (options.isEnabled(group))
							cytoscapeUtils.setAttribute(cyNetwork, edge, CytoscapeUtils.HIGHLIGHT_ATTRIBUTE, 1);
						else
							cytoscapeUtils.setAttribute(cyNetwork, edge, CytoscapeUtils.HIGHLIGHT_ATTRIBUTE, 0);
					}
				}
				
				cytoscapeUtils.updateVisualStyles(cyNetwork);
				cytoscapeUtils.repaint();
			}
		};		
	}

	@Override
	@SuppressWarnings("unchecked")
	public SelectionListener<Network<?>> createNetworkSelectionListener() {
		return new SelectionListener<Network<?>>() {
			@Override
			public void selectionChanged(SelectionEvent<Network<?>> event) {
				if (!selectionListenerEnabled)
					return;
				
				CyNetwork cyNetwork = cytoscapeUtils.getCurrentNetwork();
				ViewState options = networkOptions.get(cyNetwork.getSUID());
				
				if (options == null)
					return;
				
				Map<String, Boolean> edgeSelectionChanges = new HashMap<>();
				Map<String, Boolean> nodeSelectionChanges = new HashMap<>();
				
				for (Network<?> network : event.items) {
					Attribute attribute = network.adapt(Attribute.class);
					
					if (attribute != null) {
						nodeSelectionChanges.put(network.getName(), event.selected);
						options.setGeneHighlighted(attribute.getName(), true);
					} else {
						edgeSelectionChanges.put(network.getName(), event.selected);
						options.setNetworkHighlighted(network, event.selected);
					}
				}
				
				Set<CyEdge> enabledEdges = new HashSet<>();
				Set<CyEdge> disabledEdges = new HashSet<>();
				
				for (CyEdge edge : cyNetwork.getEdgeList()) {
					List<String> names = cytoscapeUtils.getAttribute(cyNetwork, edge, CytoscapeUtils.NETWORK_NAMES_ATTRIBUTE, List.class);
					
					if (names == null)
						continue;
					
					boolean selectionState = false;
					
					for (String name : names) {
						Boolean selected = edgeSelectionChanges.get(name);
						selectionState = selectionState || selected != null && selected;
						
						if (selectionState)
							break;
					}
					
					String attributeName = cytoscapeUtils.getAttribute(cyNetwork, edge, CytoscapeUtils.ATTRIBUTE_NAME_ATTRIBUTE, String.class);
					
					if (attributeName != null) {
						Boolean selected = nodeSelectionChanges.get(attributeName);
						selectionState = selectionState || selected != null && selected;
					}
					
					if (selectionState)
						enabledEdges.add(edge);
					else
						disabledEdges.add(edge);

					cytoscapeUtils.setAttribute(cyNetwork, edge, CytoscapeUtils.HIGHLIGHT_ATTRIBUTE, selectionState ? 1 : 0);
				}
				
				Set<CyNode> enabledNodes = new HashSet<>();
				Set<CyNode> disabledNodes = new HashSet<>();
				
				for (CyNode node : cyNetwork.getNodeList()) {
					String name = cytoscapeUtils.getAttribute(cyNetwork, node, CytoscapeUtils.GENE_NAME_ATTRIBUTE, String.class);
					
					if (name == null)
						continue;
					
					Boolean selected = nodeSelectionChanges.get(name);
					
					if (selected != null && selected)
						enabledNodes.add(node);
					else
						disabledNodes.add(node);
				}
				
				boolean listenerState = selectionListenerEnabled;
				selectionListenerEnabled = false;
				
				if (enabledEdges.size() > 0)
					cytoscapeUtils.setSelectedEdges(cyNetwork, enabledEdges, true);
				if (disabledEdges.size() > 0)
					cytoscapeUtils.setSelectedEdges(cyNetwork, disabledEdges, false);
				if (enabledNodes.size() > 0)
					cytoscapeUtils.setSelectedNodes(cyNetwork, enabledNodes, true);
				if (disabledNodes.size() > 0)
					cytoscapeUtils.setSelectedNodes(cyNetwork, disabledNodes, false);
				
				selectionListenerEnabled = listenerState;

				if (cytoscapeUtils.getSelectedEdges(cyNetwork).size() == 0) {
					for (CyEdge edge : cyNetwork.getEdgeList()) {
						String name = cytoscapeUtils.getAttribute(cyNetwork, edge, CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, String.class);
						Group<?, ?> group = options.getGroup(name);
						
						if (options.isEnabled(group))
							cytoscapeUtils.setAttribute(cyNetwork, edge, CytoscapeUtils.HIGHLIGHT_ATTRIBUTE, 1);
						else
							cytoscapeUtils.setAttribute(cyNetwork, edge, CytoscapeUtils.HIGHLIGHT_ATTRIBUTE, 0);
					}
				}
				
				cytoscapeUtils.updateVisualStyles(cyNetwork);
				cytoscapeUtils.repaint();
			}
		};		
	}

	@Override
	public SelectionListener<Gene> createFunctionListSelectionListener(FunctionInfoPanel functionPanel,
			SearchResult options) {
		return new SelectionListener<Gene>() {
			@Override
			public void selectionChanged(SelectionEvent<Gene> event) {
				if (!selectionListenerEnabled)
					return;
				
				CyNetwork cyNetwork = cytoscapeUtils.getCurrentNetwork();
				ViewState options = networkOptions.get(cyNetwork.getSUID());
				
				if (options == null)
					return;
				
				Set<Long> selectedNodes = new HashSet<>();
				
				for (Gene gene : event.items) {
					selectedNodes.add(gene.getNode().getId());
				}
				
				boolean listenerState = selectionListenerEnabled;
				selectionListenerEnabled = false;

				cytoscapeUtils.unselectAllEdges(cyNetwork);
				cytoscapeUtils.unselectAllNodes(cyNetwork);
				
				for (CyNode node : cyNetwork.getNodeList()) {
					Long nodeId = options.getNodeId(cytoscapeUtils.getIdentifier(cyNetwork, node));
					
					if (selectedNodes.contains(nodeId))
						cytoscapeUtils.setSelectedNode(cyNetwork, node, true);
				}
				
				selectionListenerEnabled = listenerState;
				cytoscapeUtils.repaint();
			}
		};
	}
	
	protected void handleNetworkChanged(CyNetwork network) {
		if (network == null && selectedNetworkId == null)
			return;
		
		selectedNetworkId = network != null ? network.getSUID() : null;
		
		if (selectedNetworkId == null) {
			plugin.hideResults();
			return;
		}
		
		ViewState options = networkOptions.get(selectedNetworkId);
		
		if (options == null) {
			plugin.hideResults();
			return;
		}
		
		plugin.applyOptions(options);
		plugin.showResults();
	}
	
	protected void handleNetworkDeleted(CyNetwork network) {
		if (network == null)
			return;
		
		networkOptions.remove(network.getSUID());
		
		if (networkOptions.size() == 0)
			plugin.hideResults();
	}
}
