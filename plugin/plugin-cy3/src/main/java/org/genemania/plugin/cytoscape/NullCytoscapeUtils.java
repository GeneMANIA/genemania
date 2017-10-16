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
package org.genemania.plugin.cytoscape;

import java.awt.Color;
import java.awt.Frame;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.genemania.domain.Node;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.model.ViewStateBuilder;
import org.genemania.plugin.proxies.EdgeProxy;
import org.genemania.plugin.proxies.NetworkProxy;
import org.genemania.plugin.proxies.NodeProxy;
import org.genemania.plugin.selection.NetworkSelectionManager;

public class NullCytoscapeUtils<NETWORK, NODE, EDGE> implements CytoscapeUtils<NETWORK, NODE, EDGE> {

	@Override
	public void applyVisualization(NETWORK network,
			Map<Long, Double> filterGeneScores,
			Map<String, Color> computeColors, double[] extrema) {
	}

	@Override
	public NETWORK createNetwork(String nextNetworkName, String dataVersion, SearchResult result,
			ViewStateBuilder options, EdgeAttributeProvider provider) {
		return null;
	}

	@Override
	public void expandAttributes(NETWORK cyNetwork, ViewState options,
			List<String> attributes) {
	}
	
	@Override
	public NODE getNode(NETWORK network, Node node, String preferredSymbol) {
		return null;
	}

	@Override
	public void performLayout(NETWORK network) {
	}

	@Override
	public void registerSelectionListener(NETWORK cyNetwork,
			NetworkSelectionManager<NETWORK, NODE, EDGE> manager, GeneMania<NETWORK, NODE, EDGE> plugin) {
	}

	@Override
	public void setHighlight(ViewState config, Group<?, ?> group,
			NETWORK network, boolean selected) {
	}
	
	@Override
	public void setHighlighted(ViewState options, NETWORK cyNetwork,
			boolean highlighted) {
	}
	
	@Override
	public NETWORK getCurrentNetwork() {
		return null;
	}
	
	@Override
	public void repaint() {
	}
	
	@Override
	public void updateVisualStyles(NETWORK network) {
	}
	
	@Override
	public Frame getFrame() {
		return null;
	}
	
	@Override
	public Set<NETWORK> getNetworks() {
		return Collections.emptySet();
	}
	
	@Override
	public void maximize(NETWORK network) {
	}
	
	@Override
	public NetworkProxy<NETWORK, NODE, EDGE> getNetworkProxy(NETWORK network) {
		return null;
	}
	
	@Override
	public EdgeProxy<EDGE, NODE> getEdgeProxy(EDGE edge, NETWORK network) {
		return null;
	}
	
	@Override
	public NodeProxy<NODE> getNodeProxy(NODE node, NETWORK network) {
		return null;
	}
	
	@Override
	public void handleNetworkPostProcessing(
			NETWORK network) {
	}
	
	@Override
	public Properties getGlobalProperties() {
		return null;
	}
	
}
