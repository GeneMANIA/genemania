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

package org.genemania.plugin.controllers;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.genemania.domain.GeneNamingSource;
import org.genemania.exception.ApplicationException;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.report.CytoscapeTextReportExporter;
import org.genemania.plugin.report.ManiaReport;
import org.genemania.plugin.report.TextReportExporter;
import org.genemania.plugin.view.AttributesDialog;
import org.genemania.plugin.view.util.FileSelectionMode;
import org.genemania.plugin.view.util.UiUtils;

public class ManiaResultsController {
	
	private final CytoscapeUtils cytoscapeUtils;
	private final DataSetManager dataSetManager;
	private final NetworkUtils networkUtils;
	private final UiUtils uiUtils;

	public ManiaResultsController(
			final DataSetManager dataSetManager,
			final CytoscapeUtils cytoscapeUtils,
			final UiUtils uiUtils,
			final NetworkUtils networkUtils
	) {
		this.dataSetManager = dataSetManager;
		this.cytoscapeUtils = cytoscapeUtils;
		this.uiUtils = uiUtils;
		this.networkUtils = networkUtils;
	}
	
	public void showAttributesDialog(CyNetwork cyNetwork, ViewState options) {		
		final AttributesDialog dialog =
				new AttributesDialog(cytoscapeUtils.getFrame(), new AttributesController(), uiUtils);
		dialog.setLocationRelativeTo(cytoscapeUtils.getFrame());
		dialog.setVisible(true);
		List<String> attributes = dialog.getSelectedAttributes();
		cytoscapeUtils.expandAttributes(cyNetwork, options, attributes);
	}
	
	public void exportReport(Component parent, ViewState viewState) throws ApplicationException {
		SearchResult options = viewState.getSearchResult();
		Set<String> extensions = new HashSet<>();
		extensions.add("txt"); //$NON-NLS-1$
		String date = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date());
		date = date.replace(":", "-").replace("/", "-"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
		String filename = String.format("GeneMANIA-Report-%s-%s.txt", options.getOrganism().getName(), date); //$NON-NLS-1$
		File initialFile = new File(filename);
		File file = uiUtils.getFile(parent, Strings.maniaResultsPanelExport_title, initialFile, Strings.maniaResultsPanelExportFile_description, extensions, FileSelectionMode.SAVE_FILE);
		
		if (file == null)
			return;
		
		try {
			FileOutputStream stream = new FileOutputStream(file);
			
			try {
				DataSet data = dataSetManager.getDataSet();
				List<GeneNamingSource> preferences = Collections.emptyList();
				IGeneProvider provider = new RankedGeneProviderWithUniprotHack(data.getAllNamingSources(), preferences);
				TextReportExporter exporter = new CytoscapeTextReportExporter(provider, networkUtils);
				ManiaReport report = new ManiaReport(viewState, data);
				exporter.export(report, stream);
				stream.flush();
			} finally {
				stream.close();
			}
		} catch (IOException e) {
			LogUtils.log(getClass(), e);
		}
	}

	public void computeHighlightedNetworks(ViewState options) {
		Set<Network<?>> highlightedNetworks = new HashSet<>();
		// Compute affected networks
		CyNetwork cyNetwork = cytoscapeUtils.getCurrentNetwork();
		
		if (cyNetwork == null)
			return;
		
		Set<CyEdge> selectedEdges = cytoscapeUtils.getSelectedEdges(cyNetwork);
		
		for (CyEdge edge : selectedEdges) {
			Set<Network<?>> networks = options.getNetworksByEdge(
					cytoscapeUtils.getIdentifier(cyNetwork, edge));
			
			for (Network<?> network : networks)
				highlightedNetworks.add(network);
		}

		Set<CyNode> selectedNodes = cytoscapeUtils.getSelectedNodes(cyNetwork);
		
		for (CyNode node : selectedNodes) {
			Set<Network<?>> networks = options.getNetworksByNode(
					cytoscapeUtils.getIdentifier(cyNetwork, node));
			
			for (Network<?> network : networks)
				highlightedNetworks.add(network);
		}

		// Compute affected groups while highlighting affected networks
		Set<Group<?, ?>> groups = new HashSet<>();
		options.clearHighlightedNetworks();
		
		for (Network<?> network : highlightedNetworks) {
			groups.add(options.getGroup(network));
			options.setNetworkHighlighted(network, true);
		}
		
		options.clearHighlightedGroups();
		
		// Highlight affected groups
		for (Group<?, ?> group : groups) {
			options.setGroupHighlighted(group, true);
		}
	}
}
