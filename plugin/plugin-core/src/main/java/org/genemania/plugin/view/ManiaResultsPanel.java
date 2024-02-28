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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;
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
import org.genemania.plugin.selection.SessionManager;
import org.genemania.plugin.selection.SelectionListener;
import org.genemania.plugin.view.components.BaseInfoPanel;
import org.genemania.plugin.view.util.UiUtils;

public class ManiaResultsPanel extends JPanel {
	
	private static final long serialVersionUID = -2824736017091793317L;
	
	private JTabbedPane tabPane;
	private JLabel organismLabel;
	private ViewState options;
	private GeneInfoPanel genePanel;
	private NetworkGroupInfoPanel networkPanel;
	private FunctionInfoPanel functionPanel;

	private SelectionListener<Gene> geneListener;

	private SelectionListener<Group<?, ?>> networkListener;

	private SelectionListener<Gene> functionListener;

	private final ManiaResultsController controller;

	private final CytoscapeUtils cytoscapeUtils;

	private final GeneMania plugin;

	private final NetworkUtils networkUtils;

	private final UiUtils uiUtils;
	
	public ManiaResultsPanel(
			ManiaResultsController controller,
			GeneMania plugin,
			CytoscapeUtils cytoscapeUtils,
			NetworkUtils networkUtils,
			UiUtils uiUtils
	) {
		this.controller = controller;
		this.plugin = plugin;
		this.cytoscapeUtils = cytoscapeUtils;
		this.networkUtils = networkUtils;
		this.uiUtils = uiUtils;
		
		setOpaque(!uiUtils.isAquaLAF());
		addComponents();
	}
	
	private void addComponents() {
		organismLabel = new JLabel();
		
		final JButton optionsButton = uiUtils.createIconButton(IconManager.ICON_BARS, 12.0f);
		optionsButton.setToolTipText("Options...");
		
		optionsButton.addActionListener(evt -> {
			JPopupMenu menu = new JPopupMenu();
			String tabTitle = getTabPane().getTitleAt(getTabPane().getSelectedIndex());
			
			final BaseInfoPanel<?, ?> infoPanel;
			
			if (Strings.maniaResultsPanelNetworkTab_label.equals(tabTitle))
				infoPanel = networkPanel;
			else if (Strings.maniaResultsPanelGeneTab_label.equals(tabTitle))
				infoPanel = genePanel;
			else
				infoPanel = null;
			
			if (infoPanel != null) {
				{
					JMenuItem mi = new JMenuItem("Expand All");
					mi.addActionListener(ae -> infoPanel.showDetails(true, -1));
					menu.add(mi);
				}
				if (infoPanel == networkPanel) {
					JMenuItem mi = new JMenuItem("Expand Top-Level");
					mi.addActionListener(ae -> {
						infoPanel.showDetails(false, -1);
						infoPanel.showDetails(true, 0);
					});
					menu.add(mi);
				}
				{
					JMenuItem mi = new JMenuItem("Expand None");
					mi.addActionListener(ae -> infoPanel.showDetails(false, -1));
					menu.add(mi);
				}
				menu.addSeparator();
				String enableOrSelect = infoPanel == networkPanel ? "Enable " : "Select ";
				{
					JMenuItem mi = new JMenuItem(enableOrSelect + "All");
					mi.addActionListener(ae -> infoPanel.setAllEnabled(true));
					menu.add(mi);
				}
				{
					JMenuItem mi = new JMenuItem(enableOrSelect + "None");
					mi.addActionListener(ae -> infoPanel.setAllEnabled(false));
					menu.add(mi);
				}
				menu.addSeparator();
				{
					JMenuItem mi = new JMenuItem("Sort by 'name'");
					mi.addActionListener(ae -> infoPanel.sort(0, null));
					menu.add(mi);
				}
				{
					String colName = infoPanel == networkPanel ? "'per cent weight'" : "'score'";
					JMenuItem mi = new JMenuItem("Sort by " + colName);
					mi.addActionListener(ae -> infoPanel.sort(1, null));
					menu.add(mi);
				}
				menu.addSeparator();
			}
			{
				JMenuItem mi = new JMenuItem(Strings.maniaResultsAttributesButton_label);
				mi.addActionListener(ae -> handleAttributesButton());
				menu.add(mi);
			}
			{
				JMenuItem mi = new JMenuItem(Strings.maniaResultsPanelExportButton_label);
				mi.addActionListener(ae -> handleExportButton());
				menu.add(mi);
			}
			
			menu.show(optionsButton, 0, optionsButton.getHeight());
		});
		
		final GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
		layout.setAutoCreateGaps(uiUtils.isWinLAF());
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(organismLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(optionsButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addContainerGap()
				)
				.addComponent(getTabPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(organismLabel)
						.addComponent(optionsButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(getTabPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
	}
	
	protected void handleExportButton() {
		try {
			CyNetwork cyNetwork = cytoscapeUtils.getCurrentNetwork();
			controller.exportReport(this, cyNetwork, options);
		} catch (ApplicationException e) {
			LogUtils.log(getClass(), e);
		}
	}

	private void handleAttributesButton() {
		CyNetwork cyNetwork = cytoscapeUtils.getCurrentNetwork();
		controller.showAttributesDialog(cyNetwork, options);
	}

	public void applyOptions(ViewState options) {
		// Remove old listeners
		if (geneListener != null)
			genePanel.removeSelectionListener(geneListener);
		if (networkListener != null)
			networkPanel.removeSelectionListener(networkListener);
		
		this.options = options;
		SearchResult result = options.getSearchResult();
		Organism organism = result.getOrganism();
		organismLabel.setText("<html><i>" + organism.getAlias() + "</i> (" + organism.getDescription() + ")</html>");
		
		SessionManager sessionManager = plugin.getSessionManager();
		geneListener = sessionManager.createGeneListSelectionListener(genePanel, options);
		networkListener = sessionManager.createNetworkListSelectionListener(networkPanel, options);
		functionListener = sessionManager.createFunctionListSelectionListener(functionPanel, result);
		
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
	
	private JTabbedPane getTabPane() {
		if (tabPane == null) {
			tabPane = new JTabbedPane(JTabbedPane.BOTTOM);
			
			networkPanel = new NetworkGroupInfoPanel(plugin, cytoscapeUtils, networkUtils, uiUtils);
			JScrollPane networkScrollPane = new JScrollPane(networkPanel);
			tabPane.addTab(Strings.maniaResultsPanelNetworkTab_label, networkScrollPane);

			genePanel = new GeneInfoPanel(networkUtils, uiUtils);
			JScrollPane geneScrollPane = new JScrollPane(genePanel);
			tabPane.addTab(Strings.maniaResultsPanelGeneTab_label, geneScrollPane);
			
			functionPanel = new FunctionInfoPanel(uiUtils);
			tabPane.addTab(Strings.maniaResultsPanelFunctionTab_label, functionPanel);
		}
		
		return tabPane;
	}
}
