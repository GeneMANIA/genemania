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
import org.genemania.plugin.proxies.EdgeProxy;
import org.genemania.plugin.proxies.NetworkProxy;
import org.genemania.plugin.proxies.NodeProxy;
import org.genemania.plugin.view.FunctionInfoPanel;
import org.genemania.plugin.view.GeneInfoPanel;
import org.genemania.plugin.view.NetworkChangeListener;
import org.genemania.plugin.view.NetworkGroupDetailPanel;
import org.genemania.plugin.view.components.BaseInfoPanel;

public abstract class AbstractNetworkSelectionManager implements NetworkSelectionManager {
	
	protected final Map<Object, ViewState> networkOptions;
	protected final CytoscapeUtils cytoscapeUtils;
	protected GeneMania plugin;

	protected String selectedNetworkId;
	protected boolean selectionListenerEnabled;

	public AbstractNetworkSelectionManager(CytoscapeUtils cytoscapeUtils) {
	    this.cytoscapeUtils = cytoscapeUtils;
	    
	    networkOptions = new HashMap<Object, ViewState>();
	    selectionListenerEnabled = true;
	}

	@Override
	public void setGeneMania(GeneMania geneMania) {
		plugin = geneMania;
	}
	
	@Override
	public String getSelectedNetworkId() {
		return selectedNetworkId;
	}
	
	@Override
	public int getTotalNetworks() {
		return networkOptions.size();
	}
	
	@Override
	public void addNetworkConfiguration(CyNetwork network, ViewState config) {
		NetworkProxy proxy = cytoscapeUtils.getNetworkProxy(network);
		networkOptions.put(proxy.getIdentifier(), config);
	}

	@Override
	public ViewState getNetworkConfiguration(CyNetwork network) {
		NetworkProxy proxy = cytoscapeUtils.getNetworkProxy(network);
		return networkOptions.get(proxy.getIdentifier());
	}
	
	@Override
	public boolean isGeneManiaNetwork(CyNetwork network) {
		NetworkProxy proxy = cytoscapeUtils.getNetworkProxy(network);
		return networkOptions.get(proxy.getIdentifier()) != null;
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
			public void selectionChanged(SelectionEvent<Gene> event) {
				if (!selectionListenerEnabled) {
					return;
				}
				
				boolean listenerState = selectionListenerEnabled;
				selectionListenerEnabled = false;
				CyNetwork network = cytoscapeUtils.getCurrentNetwork();
				NetworkProxy proxy = cytoscapeUtils.getNetworkProxy(network);
				
				for (Gene gene : event.items) {
					Node node = gene.getNode();
					CyNode cyNode = cytoscapeUtils.getNode(network, node, null);
					NodeProxy nodeProxy = cytoscapeUtils.getNodeProxy(cyNode, network);
					options.setGeneHighlighted(nodeProxy.getIdentifier(), event.selected);
					proxy.setSelectedNode(cyNode, event.selected);
				}
				selectionListenerEnabled = listenerState;
				cytoscapeUtils.repaint();
			}
		};		
	}
	
	@Override
	public boolean checkSelectionState(CyEdge referenceEdge, Set<CyEdge> selectedEdges, CyNetwork network) {
		EdgeProxy referenceEdgeProxy = cytoscapeUtils.getEdgeProxy(referenceEdge, network);
		String target = referenceEdgeProxy.getAttribute(CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, String.class);
		if (target == null) {
			return false;
		}
		for (CyEdge edge : selectedEdges) {
			EdgeProxy edgeProxy = cytoscapeUtils.getEdgeProxy(edge, network);
			String id = edgeProxy.getAttribute(CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, String.class);
			if (id == null) {
				continue;
			}
			if (id == target) {
				return false;
			}
		}
		return true;
	}

	@Override
	public SelectionListener<Group<?, ?>> createNetworkListSelectionListener(final BaseInfoPanel<Group<?, ?>, NetworkGroupDetailPanel> panel, final ViewState options) {
		return new SelectionListener<Group<?, ?>>() {
			@Override
			public void selectionChanged(SelectionEvent<Group<?, ?>> event) {
				if (!selectionListenerEnabled)
					return;
				
				CyNetwork cyNetwork = cytoscapeUtils.getCurrentNetwork();
				NetworkProxy networkProxy = cytoscapeUtils.getNetworkProxy(cyNetwork);
				ViewState options = networkOptions.get(networkProxy.getIdentifier());
				
				if (options == null)
					return;
				
				Set<CyEdge> enabledEdges = new HashSet<>();
				Set<CyEdge> disabledEdges = new HashSet<>();
				
				Map<String, Boolean> selectionChanges = new HashMap<String, Boolean>();
				
				for (Group<?, ?> group : event.items) {
					selectionChanges.put(group.getName(), event.selected);
					options.setGroupHighlighted(group, event.selected);
				}
				
				for (CyEdge edge : networkProxy.getEdges()) {
					EdgeProxy edgeProxy = cytoscapeUtils.getEdgeProxy(edge, cyNetwork);
					String name = edgeProxy.getAttribute(CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, String.class);
					Boolean selectionState = selectionChanges.get(name);
					if (selectionState == null) {
						edgeProxy.setAttribute(CytoscapeUtils.HIGHLIGHT_ATTRIBUTE, 0);
						continue;
					}
					
					if (selectionState) {
						enabledEdges.add(edge);
					} else {
						disabledEdges.add(edge);
					}
					Group<?, ?> group = options.getGroup(name);
					boolean highlighted = selectionState || options.isEnabled(group);
					edgeProxy.setAttribute(CytoscapeUtils.HIGHLIGHT_ATTRIBUTE, highlighted ? 1 : 0);
				}
				
				boolean listenerState = selectionListenerEnabled;
				selectionListenerEnabled = false;
				if (enabledEdges.size() > 0) {
					networkProxy.setSelectedEdges(enabledEdges, true);
				}
				if (disabledEdges.size() > 0) {
					networkProxy.setSelectedEdges(disabledEdges, false);
				}
				selectionListenerEnabled = listenerState;

				if (networkProxy.getSelectedEdges().size() == 0) {
					for (CyEdge edge : networkProxy.getEdges()) {
						EdgeProxy edgeProxy = cytoscapeUtils.getEdgeProxy(edge, cyNetwork);
						String name = edgeProxy.getAttribute(CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, String.class);
						Group<?, ?> group = options.getGroup(name);
						if (options.isEnabled(group)) {
							edgeProxy.setAttribute(CytoscapeUtils.HIGHLIGHT_ATTRIBUTE, 1);
						} else {
							edgeProxy.setAttribute(CytoscapeUtils.HIGHLIGHT_ATTRIBUTE, 0);
						}
					}
				}
				
				cytoscapeUtils.updateVisualStyles(cyNetwork);
				cytoscapeUtils.repaint();
			}
		};		
	}

	@Override
	public SelectionListener<Network<?>> createNetworkSelectionListener() {
		return new SelectionListener<Network<?>>() {
			@Override
			public void selectionChanged(SelectionEvent<Network<?>> event) {
				if (!selectionListenerEnabled)
					return;
				
				CyNetwork cyNetwork = cytoscapeUtils.getCurrentNetwork();
				NetworkProxy networkProxy = cytoscapeUtils.getNetworkProxy(cyNetwork);
				ViewState options = networkOptions.get(networkProxy.getIdentifier());
				
				if (options == null)
					return;
				
				Map<String, Boolean> edgeSelectionChanges = new HashMap<String, Boolean>();
				Map<String, Boolean> nodeSelectionChanges = new HashMap<String, Boolean>();
				
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
				
				for (CyEdge edge : networkProxy.getEdges()) {
					EdgeProxy edgeProxy = cytoscapeUtils.getEdgeProxy(edge, cyNetwork);
					
					@SuppressWarnings("unchecked")
					List<String> names = edgeProxy.getAttribute(CytoscapeUtils.NETWORK_NAMES_ATTRIBUTE, List.class);
					
					if (names == null)
						continue;
					
					boolean selectionState = false;
					
					for (String name : names) {
						Boolean selected = edgeSelectionChanges.get(name);
						selectionState = selectionState || selected != null && selected;
						
						if (selectionState)
							break;
					}
					
					String attributeName = edgeProxy.getAttribute(CytoscapeUtils.ATTRIBUTE_NAME_ATTRIBUTE, String.class);
					
					if (attributeName != null) {
						Boolean selected = nodeSelectionChanges.get(attributeName);
						selectionState = selectionState || selected != null && selected;
					}
					
					if (selectionState) {
						enabledEdges.add(edge);
					} else {
						disabledEdges.add(edge);
					}

					edgeProxy.setAttribute(CytoscapeUtils.HIGHLIGHT_ATTRIBUTE, selectionState ? 1 : 0);
				}
				
				Set<CyNode> enabledNodes = new HashSet<>();
				Set<CyNode> disabledNodes = new HashSet<>();
				
				for (CyNode node : networkProxy.getNodes()) {
					NodeProxy nodeProxy = cytoscapeUtils.getNodeProxy(node, cyNetwork);
					String name = nodeProxy.getAttribute(CytoscapeUtils.GENE_NAME_ATTRIBUTE, String.class);
					
					if (name == null)
						continue;
					
					Boolean selected = nodeSelectionChanges.get(name);
					
					if (selected != null && selected) {
						enabledNodes.add(node);
					} else {
						disabledNodes.add(node);
					}
				}
				
				boolean listenerState = selectionListenerEnabled;
				selectionListenerEnabled = false;
				
				if (enabledEdges.size() > 0) {
					networkProxy.setSelectedEdges(enabledEdges, true);
				}
				if (disabledEdges.size() > 0) {
					networkProxy.setSelectedEdges(disabledEdges, false);
				}
				if (enabledNodes.size() > 0) {
					networkProxy.setSelectedNodes(enabledNodes, true);
				}
				if (disabledNodes.size() > 0) {
					networkProxy.setSelectedNodes(disabledNodes, false);
				}
				selectionListenerEnabled = listenerState;

				if (networkProxy.getSelectedEdges().size() == 0) {
					for (CyEdge edge : networkProxy.getEdges()) {
						EdgeProxy edgeProxy = cytoscapeUtils.getEdgeProxy(edge, cyNetwork);
						String name = edgeProxy.getAttribute(CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, String.class);
						Group<?, ?> group = options.getGroup(name);
						
						if (options.isEnabled(group)) {
							edgeProxy.setAttribute(CytoscapeUtils.HIGHLIGHT_ATTRIBUTE, 1);
						} else {
							edgeProxy.setAttribute(CytoscapeUtils.HIGHLIGHT_ATTRIBUTE, 0);
						}
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
				NetworkProxy networkProxy = cytoscapeUtils.getNetworkProxy(cyNetwork);
				ViewState options = networkOptions.get(networkProxy.getIdentifier());
				
				if (options == null)
					return;
				
				Set<Long> selectedNodes = new HashSet<Long>();
				
				for (Gene gene : event.items) {
					selectedNodes.add(gene.getNode().getId());
				}
				
				boolean listenerState = selectionListenerEnabled;
				selectionListenerEnabled = false;

				networkProxy.unselectAllEdges();
				networkProxy.unselectAllNodes();
				
				for (CyNode node : networkProxy.getNodes()) {
					NodeProxy nodeProxy = cytoscapeUtils.getNodeProxy(node, cyNetwork);
					Long nodeId = options.getNodeId(nodeProxy.getIdentifier());
					
					if (selectedNodes.contains(nodeId))
						networkProxy.setSelectedNode(node, true);
				}
				
				selectionListenerEnabled = listenerState;
				cytoscapeUtils.repaint();
			}
		};
	}
	
	protected void handleNetworkChanged(String networkId) {
		if (networkId == null && selectedNetworkId == null) {
			return;
		}
		
		selectedNetworkId = networkId;
		if (networkId == null) {
			plugin.hideResults();
			return;
		}
		
		ViewState options = networkOptions.get(networkId);
		if (options == null) {
			plugin.hideResults();
			return;
		}
		
		plugin.applyOptions(options);
		plugin.showResults();
	}
	
	protected void handleNetworkDeleted(String networkId) {
		if (networkId == null && selectedNetworkId == null) {
			return;
		}
		networkOptions.remove(networkId);
		
		if (networkOptions.size() == 0) {
			plugin.hideResults();
		}
	}

}
