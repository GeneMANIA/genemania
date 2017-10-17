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

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
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

public class NullCytoscapeUtils implements CytoscapeUtils {

	@Override
	public void applyVisualization(CyNetwork network,
			Map<Long, Double> filterGeneScores,
			Map<String, Color> computeColors, double[] extrema) {
	}

	@Override
	public CyNetwork createNetwork(String nextNetworkName, String dataVersion, SearchResult result,
			ViewStateBuilder options, EdgeAttributeProvider provider) {
		return null;
	}

	@Override
	public void expandAttributes(CyNetwork cyNetwork, ViewState options, List<String> attributes) {
	}

	@Override
	public CyNode getNode(CyNetwork network, Node node, String preferredSymbol) {
		return null;
	}

	@Override
	public void performLayout(CyNetwork network) {
	}

	@Override
	public void registerSelectionListener(CyNetwork cyNetwork, NetworkSelectionManager manager, GeneMania plugin) {
	}

	@Override
	public void setHighlight(ViewState config, Group<?, ?> group, CyNetwork network, boolean selected) {
	}

	@Override
	public void setHighlighted(ViewState options, CyNetwork cyNetwork, boolean highlighted) {
	}

	@Override
	public CyNetwork getCurrentNetwork() {
		return null;
	}

	@Override
	public void repaint() {
	}

	@Override
	public void updateVisualStyles(CyNetwork network) {
	}

	@Override
	public Frame getFrame() {
		return null;
	}

	@Override
	public Set<CyNetwork> getNetworks() {
		return Collections.emptySet();
	}

	@Override
	public void maximize(CyNetwork network) {
	}

	@Override
	public NetworkProxy getNetworkProxy(CyNetwork network) {
		return null;
	}

	@Override
	public EdgeProxy getEdgeProxy(CyEdge edge, CyNetwork network) {
		return null;
	}

	@Override
	public NodeProxy getNodeProxy(CyNode node, CyNetwork network) {
		return null;
	}

	@Override
	public void handleNetworkPostProcessing(CyNetwork network) {
	}

	@Override
	public Properties getGlobalProperties() {
		return null;
	}

}
