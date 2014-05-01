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

package org.genemania.plugin.cytoscape2.layout;

import giny.model.Edge;
import giny.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.OneUseIterable;
import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetChangeListener;
import org.genemania.plugin.selection.NetworkSelectionManager;
import org.genemania.util.ProgressReporter;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.SelectFilter;
import cytoscape.layout.AbstractLayout;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.CyLayouts;
import cytoscape.layout.LayoutProperties;
import cytoscape.layout.Tunable;
import cytoscape.task.TaskMonitor;
import cytoscape.view.CyNetworkView;

public class FilteredLayout extends AbstractLayout {
	private static final String GROUP_ATTRIBUTE = "groups"; //$NON-NLS-1$

	public static final String ID = "genemania-filtered-layout"; //$NON-NLS-1$
	
	private static final String FORCE_DIRECTED_LAYOUT_ID = "force-directed"; //$NON-NLS-1$

	private final LayoutProperties properties;

	private final GeneMania<CyNetwork, CyNode, CyEdge> plugin;
	
	public FilteredLayout(GeneMania<CyNetwork, CyNode, CyEdge> plugin) {
		this.plugin = plugin;
		properties = new LayoutProperties(getName());
		createGroupList();

		Object initialValue = ""; //$NON-NLS-1$
		Object groupList = new Object[0];
		properties.add(new Tunable(GROUP_ATTRIBUTE, Strings.filteredLayout_groupsTunable, Tunable.LIST, initialValue, groupList , null, Tunable.MULTISELECT));
		properties.initializeProperties();
		updateSettings(true);
	}
	
	@Override
	public LayoutProperties getSettings() {
		return properties;
	}
	
	private void createGroupList() {
		plugin.getDataSetManager().addDataSetChangeListener(new DataSetChangeListener() {
			public void dataSetChanged(DataSet data, ProgressReporter progress) {
				populateGroups(data);
			}
		});
		DataSet data = plugin.getDataSetManager().getDataSet();
		if (data == null) {
			return;
		}
		populateGroups(data);
	}

	protected void populateGroups(DataSet data) {
		Tunable tunable = properties.get(GROUP_ATTRIBUTE);
		Set<String> names = new HashSet<String>();
		try {
			if (data != null) {
				for (Organism organism : data.getMediatorProvider().getOrganismMediator().getAllOrganisms()) {
					for (InteractionNetworkGroup group : organism.getInteractionNetworkGroups()) {
						names.add(group.getName());
					}
				}
			}
		} catch (DataStoreException e) {
			LogUtils.log(getClass(), e);
		}
		
		List<String> groups = new ArrayList<String>();
		groups.addAll(names);
		Collections.sort(groups, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		});
		tunable.setLowerBound(groups.toArray());
		
		StringBuilder indices = new StringBuilder();
		int index = 0;
		for (String group : groups) {
			if (group.equalsIgnoreCase("Co-expression") || group.equalsIgnoreCase("Co-localization")) { //$NON-NLS-1$ //$NON-NLS-2$
				if (indices.length() > 0) {
					indices.append(","); //$NON-NLS-1$
				}
				indices.append(index);
			}
			index++;
		}
		tunable.setValue(indices.toString());
	}

	private void updateSettings(boolean force) {
		properties.updateValues();
	}
	
	private List<String> decodeGroups(Tunable tunable) {
		List<String> groups = new ArrayList<String>();
		Object[] items = (Object[]) tunable.getLowerBound();
		String value = (String) tunable.getValue();
		for (String rawIndex : value.split(",")) { //$NON-NLS-1$
			try {
				int index = Integer.parseInt(rawIndex);
				String group = (String) items[index];
				groups.add(group);
			} catch (NumberFormatException e) {
			}
		}
		return groups;
	}

	@Override
	public void revertSettings() {
		properties.revertProperties();
	}
	
	@Override
	public JPanel getSettingsPanel() {
		return properties.getTunablePanel();
	}

	@Override
	public void doLayout(CyNetworkView view, TaskMonitor monitor) {
		CyLayoutAlgorithm layout = CyLayouts.getLayout(FORCE_DIRECTED_LAYOUT_ID);
		if (layout == null) {
			return;
		}
		
		if (!layout.supportsSelectedOnly()) {
			return;
		}

		if (!(layout instanceof AbstractLayout)) {
			return;
		}

		AbstractLayout layout2 = (AbstractLayout) layout;
		layout2.setSelectedOnly(true);
		LayoutProperties settings = layout2.getSettings();
		
		// Derived through linear regression of manual parameter tuning for
		// 20, 60, and 110 nodes.
		double defaultMass = -20.0/9 * view.getNetwork().getNodeCount() + 1000;
		
		settings.get("selected_only").setValue(true); //$NON-NLS-1$
		settings.get("edge_attribute").setValue(CytoscapeUtils.MAX_WEIGHT_ATTRIBUTE); //$NON-NLS-1$
		settings.get("weight_type").setValue(1); //$NON-NLS-1$
		settings.get("min_weight").setValue(0.0); //$NON-NLS-1$
		settings.get("max_weight").setValue(10.0); //$NON-NLS-1$
		settings.get("defaultSpringCoefficient").setValue(0.1); //$NON-NLS-1$
		settings.get("defaultSpringLength").setValue(1.0); //$NON-NLS-1$
		settings.get("defaultNodeMass").setValue(defaultMass); //$NON-NLS-1$
		settings.get("numIterations").setValue(100); //$NON-NLS-1$
		layout2.updateSettings();

		// TODO: Save selection first?
		NetworkSelectionManager<CyNetwork, CyNode, CyEdge> manager = plugin.getNetworkSelectionManager();
		manager.setSelectionListenerEnabled(false);
		
		CyNetwork network = Cytoscape.getCurrentNetwork();
		SelectFilter filter = network.getSelectFilter();
		try {
			// Clear selection first
			filter.unselectAllNodes();
			filter.unselectAllEdges();
			
			applySelection(network);
			layout2.doLayout(view, monitor);
			view.fitContent();
			
			Cytoscape.getDesktop().repaint();
		} finally {
			filter.unselectAllNodes();
			filter.unselectAllEdges();
			manager.setSelectionListenerEnabled(true);
		}
	}
	
	@Override
	public void construct() {
	}

	@SuppressWarnings("unchecked")
	private void applySelection(CyNetwork network) {
		SelectFilter filter = network.getSelectFilter();

		try {
			Set<String> excludedGroups = computeExcludedGroups(properties.get(GROUP_ATTRIBUTE));
	
			Set<Node> nodes = new HashSet<Node>();
			Set<Edge> edges = new HashSet<Edge>();
			CyAttributes attributes = Cytoscape.getEdgeAttributes();
			for (CyEdge edge : new OneUseIterable<CyEdge>(network.edgesIterator())) {
				String groupName = attributes.getStringAttribute(edge.getIdentifier(), CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE);
				if (excludedGroups.contains(groupName)) {
					continue;
				}
				edges.add(edge);
				nodes.add(edge.getSource());
				nodes.add(edge.getTarget());
			}
			
			if (edges.isEmpty()) {
				filter.selectAllEdges();
				filter.selectAllNodes();
			} else {
				filter.setSelectedNodes(nodes, true);
				filter.setSelectedEdges(edges, true);
			}
		} catch (DataStoreException e) {
			throw new RuntimeException(e);
		}
	}

	private Set<String> computeExcludedGroups(Tunable tunable) throws DataStoreException {
		List<String> exclusions = decodeGroups(tunable);
		return new HashSet<String>(exclusions);
	}

	@Override
	public String getName() {
		return ID;
	}

	@Override
	public String toString() {
		return Strings.filteredLayout_title;
	}
}
