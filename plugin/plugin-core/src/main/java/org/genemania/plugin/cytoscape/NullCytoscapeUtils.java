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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.genemania.domain.Node;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.model.ViewStateBuilder;
import org.genemania.plugin.selection.SessionManager;
import org.genemania.util.ProgressReporter;

public class NullCytoscapeUtils implements CytoscapeUtils {

	@Override
	public CyServiceRegistrar getServiceRegistrar() {
		return null;
	}
	
	@Override
	public void applyVisualization(CyNetwork network, Map<Long, Double> filterGeneScores,
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
	public void registerSelectionListener(CyNetwork cyNetwork, SessionManager manager, GeneMania plugin) {
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
	public void handleNetworkPostProcessing(CyNetwork network) {
	}

	@Override
	public Set<CyEdge> getSelectedEdges(CyNetwork network) {
		return null;
	}

	@Override
	public Set<CyNode> getSelectedNodes(CyNetwork network) {
		return null;
	}

	@Override
	public String getTitle(CyNetwork network) {
		return null;
	}

	@Override
	public void setSelectedEdge(CyNetwork network, CyEdge edge, boolean selected) {
	}

	@Override
	public void setSelectedEdges(CyNetwork network, Collection<CyEdge> edges, boolean selected) {
	}

	@Override
	public void setSelectedNode(CyNetwork network, CyNode node, boolean selected) {
	}

	@Override
	public void setSelectedNodes(CyNetwork network, Collection<CyNode> nodes, boolean selected) {
	}

	@Override
	public void unselectAllEdges(CyNetwork network) {
	}

	@Override
	public void unselectAllNodes(CyNetwork network) {
	}

	@Override
	public Collection<String> getNodeAttributeNames(CyNetwork network) {
		return null;
	}

	@Override
	public Collection<String> getEdgeAttributeNames(CyNetwork network) {
		return null;
	}

	@Override
	public Collection<String> getNames(Collection<CyColumn> columns, CyNetwork network) {
		return null;
	}

	@Override
	public Collection<CyNode> getNeighbours(CyNode node, CyNetwork network) {
		return null;
	}

	@Override
	public String getIdentifier(CyNetwork network, CyIdentifiable entry) {
		return null;
	}

	@Override
	public <U> U getAttribute(CyNetwork network, CyIdentifiable entry, String name, Class<U> type) {
		return null;
	}

	@Override
	public <U> void setAttribute(CyNetwork network, CyIdentifiable entry, String name, U value) {
	}

	@Override
	public Class<?> getAttributeType(CyNetwork network, CyIdentifiable entry, String name) {
		return null;
	}

	@Override
	public CyNetwork getNetwork(long suid) {
		return null;
	}

	@Override
	public boolean isGeneManiaNetwork(CyNetwork network) {
		return false;
	}

	@Override
	public String getDataVersion(CyNetwork cyNetwork) {
		return null;
	}

	@Override
	public void saveSessionState(Map<Long, ViewState> states) {
	}

	@Override
	public Map<CyNetwork, ViewState> restoreSessionState(ProgressReporter progress) {
		return Collections.emptyMap();
	}

	@Override
	public void removeSavedSessionState(Long networkId) {
	}

	@Override
	public void clearSavedSessionState() {
	}

	@Override
	public String getSessionProperty(String key) {
		return null;
	}

	@Override
	public void setSessionProperty(String key, String value) {
	}

	@Override
	public void removeSessionProperty(String key) {
	}

	@Override
	public String getPreference(String key) {
		return null;
	}

	@Override
	public void setPreference(String key, String value) {
	}
	
	@Override
	public void removePreference(String key) {
	}
}
