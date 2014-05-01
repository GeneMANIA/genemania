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

package org.genemania.plugin.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.genemania.domain.Gene;
import org.genemania.domain.Organism;
import org.genemania.exception.ApplicationException;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.controllers.ManiaResultsController;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.selection.NetworkSelectionManager;
import org.genemania.plugin.selection.SelectionListener;
import org.genemania.plugin.view.util.UiUtils;

public class ManiaResultsPanel<NETWORK, NODE, EDGE> extends JPanel {
	private static final long serialVersionUID = 1L;

	private JLabel organismLabel;
	private ViewState options;
	private GeneInfoPanel genePanel;
	private NetworkGroupInfoPanel<NETWORK, NODE, EDGE> networkPanel;
	private FunctionInfoPanel functionPanel;

	private SelectionListener<Gene> geneListener;

	private SelectionListener<Group<?, ?>> networkListener;

	private SelectionListener<Gene> functionListener;

	private final ManiaResultsController<NETWORK, NODE, EDGE> controller;

	private final CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils;

	private final GeneMania<NETWORK, NODE, EDGE> plugin;

	private final NetworkUtils networkUtils;

	private final UiUtils uiUtils;

	public ManiaResultsPanel(ManiaResultsController<NETWORK, NODE, EDGE> controller, GeneMania<NETWORK, NODE, EDGE> plugin, CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils, NetworkUtils networkUtils, UiUtils uiUtils) {
		this.controller = controller;
		this.plugin = plugin;
		this.cytoscapeUtils = cytoscapeUtils;
		this.networkUtils = networkUtils;
		this.uiUtils = uiUtils;
		
		setOpaque(false);		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		addComponents();
	}
	
	private void addComponents() {
		organismLabel = new JLabel();
		organismLabel.setAlignmentX(CENTER_ALIGNMENT);
		add(organismLabel);

		JTabbedPane tabPane = new JTabbedPane();
		tabPane.setAlignmentX(CENTER_ALIGNMENT);
		
		JPanel networkContents = uiUtils.createJPanel();
		networkContents.setLayout(new GridBagLayout());
		networkPanel = new NetworkGroupInfoPanel<NETWORK, NODE, EDGE>(plugin, cytoscapeUtils, networkUtils, uiUtils);
		JScrollPane networkScrollPane = new JScrollPane(networkPanel);
		
		JPanel optionsPanel = networkPanel.createExpanderPanel(Strings.maniaResultsPanelNetworkOptions_label);
		networkContents.add(optionsPanel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.PAGE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		networkContents.add(networkScrollPane, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		tabPane.add(networkContents);
		tabPane.setTitleAt(0, Strings.maniaResultsPanelNetworkTab_label);

		JPanel geneContents = uiUtils.createJPanel();
		geneContents.setLayout(new GridBagLayout());
		genePanel = new GeneInfoPanel(networkUtils, uiUtils);
		JScrollPane geneScrollPane = new JScrollPane(genePanel);
		
		optionsPanel = genePanel.createExpanderPanel(Strings.maniaResultsPanelGeneOptions_label);
		geneContents.add(optionsPanel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.PAGE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		geneContents.add(geneScrollPane, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		tabPane.add(geneContents);
		tabPane.setTitleAt(1, Strings.maniaResultsPanelGeneTab_label);
		
		JPanel functionContents = uiUtils.createJPanel();
		functionContents.setLayout(new GridBagLayout());
		
		functionPanel = new FunctionInfoPanel(uiUtils);
		functionContents.add(functionPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		tabPane.add(functionContents);
		tabPane.setTitleAt(2, Strings.maniaResultsPanelFunctionTab_label);
		
		add(tabPane);		
		
		JButton exportButton = new JButton(Strings.maniaResultsPanelExportButton_label);
		exportButton.setAlignmentX(CENTER_ALIGNMENT);
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				handleExportButton();
			}
		});
		add(exportButton);
		
		JButton attributesButton = new JButton(Strings.maniaResultsAttributesButton_label);
		attributesButton.setAlignmentX(CENTER_ALIGNMENT);
		attributesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleAttributesButton();
			}
		});
		add(attributesButton);
	}
	
	protected void handleExportButton() {
		try {
			controller.exportReport(this, options);
		} catch (ApplicationException e) {
			LogUtils.log(getClass(), e);
		}
	}

	private void handleAttributesButton() {
		NETWORK cyNetwork = cytoscapeUtils.getCurrentNetwork();
		controller.showAttributesDialog(cyNetwork, options);
	}

	public void applyOptions(ViewState options) {
		// Remove old listeners
		if (geneListener != null) {
			genePanel.removeSelectionListener(geneListener);
		}
		if (networkListener != null) {
			networkPanel.removeSelectionListener(networkListener);
		}
		
		this.options = options;
		SearchResult result = options.getSearchResult();
		Organism organism = result.getOrganism();
		organismLabel.setText(String.format(Strings.maniaResultsPanelOrganism_label, organism.getName()));
		
		NetworkSelectionManager<NETWORK, NODE, EDGE> selectionManager = plugin.getNetworkSelectionManager();
		geneListener = selectionManager.createGeneListSelectionListener(genePanel, options);
		networkListener = selectionManager.createNetworkListSelectionListener(networkPanel, options);
		functionListener = selectionManager.createFunctionListSelectionListener(functionPanel, result);
		
		genePanel.applyOptions(options);
		genePanel.addSelectionListener(geneListener);

		networkPanel.applyOptions(options);
		networkPanel.addSelectionListener(networkListener);

		functionPanel.applyOptions(options);
		functionPanel.setSelectionListener(functionListener);
		
		updateSelection(options);
	}
	
	public void updateSelection(ViewState options) {
		controller.computeHighlightedNetworks(options);
		genePanel.updateSelection(options);
		networkPanel.updateSelection(options);
		functionPanel.updateSelection(options);
	}
}
