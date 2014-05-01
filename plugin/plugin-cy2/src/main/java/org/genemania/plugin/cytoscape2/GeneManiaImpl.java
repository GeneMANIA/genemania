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

package org.genemania.plugin.cytoscape2;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.SwingPropertyChangeSupport;

import org.genemania.plugin.AbstractGeneMania;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.cytoscape2.actions.AboutAction;
import org.genemania.plugin.cytoscape2.actions.ManiaResultsAction;
import org.genemania.plugin.cytoscape2.actions.RetrieveRelatedGenesAction;
import org.genemania.plugin.cytoscape2.actions.ShowCytoPanelAction;
import org.genemania.plugin.cytoscape2.layout.FilteredLayout;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetChangeListener;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.ManiaResultsPanel;
import org.genemania.plugin.view.RetrieveRelatedGenesDialog;
import org.genemania.plugin.view.util.UiUtils;
import org.genemania.util.ProgressReporter;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.layout.CyLayouts;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.NetworkViewManager;

/**
 * Controls the lifecycle of the GeneMania Cytoscape plugin.
 */
public class GeneManiaImpl extends AbstractGeneMania<CyNetwork, CyNode, CyEdge> implements GeneMania<CyNetwork, CyNode, CyEdge> {
	protected RetrieveRelatedGenesAction<CyNetwork, CyNode, CyEdge> retrieveRelatedGenesAction;
	protected ManiaResultsAction<CyNetwork, CyNode, CyEdge> showResultsAction;
	protected PropertyChangeListener networkFocusListener;
	protected PropertyChangeListener networkDestroyedListener;
	protected SessionChangeListener sessionChangeListener;

	public GeneManiaImpl(DataSetManager dataSetManager, final CytoscapeUtils<CyNetwork, CyNode, CyEdge> cytoscapeUtils, UiUtils uiUtils, FileUtils fileUtils, NetworkUtils networkUtils, TaskDispatcher taskDispatcher) {
		super(dataSetManager, cytoscapeUtils, uiUtils, fileUtils, networkUtils, taskDispatcher);
		dataSetManager.addDataSetChangeListener(new DataSetChangeListener() {
			@Override
			public void dataSetChanged(DataSet dataSet, ProgressReporter progress) {
				Properties properties = cytoscapeUtils.getGlobalProperties();
				if (dataSet == null) {
					properties.remove(GeneMania.DATA_SOURCE_PATH_PROPERTY);
					return;
				} else {
					properties.setProperty(GeneMania.DATA_SOURCE_PATH_PROPERTY, dataSet.getBasePath());
				}
			}
		});
	}
	
	@SuppressWarnings("serial")
	@Override
	public void startUp() {
		sessionChangeListener = new SessionChangeListener(this, taskDispatcher, cytoscapeUtils);
		
		PropertyChangeSupport coreSupport = Cytoscape.getPropertyChangeSupport();
		coreSupport.addPropertyChangeListener(Cytoscape.SESSION_LOADED, sessionChangeListener);
		
		networkFocusListener = selectionManager.getNetworkChangeListener();
		networkDestroyedListener = selectionManager.getNetworkDestroyedListener();
		NetworkViewManager viewManager = Cytoscape.getDesktop().getNetworkViewManager();
		SwingPropertyChangeSupport support = viewManager.getSwingPropertyChangeSupport();
		support.addPropertyChangeListener(CytoscapeDesktop.NETWORK_VIEW_FOCUSED, networkFocusListener);
		
		coreSupport.addPropertyChangeListener(Cytoscape.NETWORK_DESTROYED, networkDestroyedListener);
		
		retrieveRelatedGenesAction = new RetrieveRelatedGenesAction<CyNetwork, CyNode, CyEdge>(Strings.retrieveRelatedGenes_menuLabel, this, cytoscapeUtils, networkUtils, uiUtils, fileUtils, taskDispatcher);
		showResultsAction = new ManiaResultsAction<CyNetwork, CyNode, CyEdge>(Strings.maniaResults_menuLabel, this, cytoscapeUtils, networkUtils, uiUtils);
		AboutAction<CyNetwork, CyNode, CyEdge> aboutAction = new AboutAction<CyNetwork, CyNode, CyEdge>(Strings.about_menuLabel, uiUtils, cytoscapeUtils);
		
		CytoscapeAction checkAction = new CytoscapeAction(Strings.dataUpdateCheck_menuLabel) {
			@Override
			public void actionPerformed(ActionEvent event) {
				handleCheck();
			}
		};
		
		CytoscapeAction downloadAction = new CytoscapeAction(Strings.dataUpdateDownload_menuLabel) {
			public void actionPerformed(ActionEvent event) {
				handleDownload();
			}
		};
		
		CytoscapeAction switchAction = new CytoscapeAction(Strings.changeData_menuLabel) {
			@Override
			public void actionPerformed(ActionEvent event) {
				handleSwitch();
			}
		};
		
		showResultsMenu = new JMenuItem(showResultsAction);
		showResultsAction.setMenuItem(showResultsMenu);

		JMenu menu = new JMenu(Strings.root_menuLabel);
		menu.add(new JMenuItem(retrieveRelatedGenesAction));
		menu.add(showResultsMenu);
		menu.add(new JMenuItem(downloadAction));
		menu.add(new JMenuItem(switchAction));			
		menu.add(new JSeparator());
		menu.add(new JMenuItem(aboutAction));
		menu.add(new JMenuItem(checkAction));
		
		rootMenuItem = Cytoscape.getDesktop().getCyMenus().getOperationsMenu().add(menu);
		
		menu.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent e) {
				validateMenu();
			}
			
			public void menuDeselected(MenuEvent e) {
			}
			
			public void menuCanceled(MenuEvent e) {
			}
		});
		
		CyLayouts.addLayout(new FilteredLayout(this), Strings.layoutMenu_title);
	}
	
	@Override
	public void shutDown() {
		PropertyChangeSupport coreSupport = Cytoscape.getPropertyChangeSupport();
		coreSupport.removePropertyChangeListener(sessionChangeListener);
		
		NetworkViewManager viewManager = Cytoscape.getDesktop().getNetworkViewManager();
		SwingPropertyChangeSupport support = viewManager.getSwingPropertyChangeSupport();
		support.removePropertyChangeListener(networkFocusListener);
		support.removePropertyChangeListener(networkDestroyedListener);

		Cytoscape.getDesktop().getCyMenus().getViewMenu().remove(rootMenuItem);
		rootMenuItem = null;
	}
	
	public RetrieveRelatedGenesDialog<CyNetwork, CyNode, CyEdge> getResultsDialog() {
		return retrieveRelatedGenesAction.getDialog();
	}
	
	@Override
	public void showResults() {
		showResultsAction.actionPerformed(new ActionEvent(this, 0, ShowCytoPanelAction.SHOW));
	}
	
	@Override
	public void hideResults() {
		showResultsAction.actionPerformed(new ActionEvent(this, 0, ShowCytoPanelAction.HIDE));
	}
	
	public void showQuery() {
		retrieveRelatedGenesAction.actionPerformed(null);
	}

	@Override
	public void applyOptions(ViewState options) {
		ManiaResultsPanel<CyNetwork, CyNode, CyEdge> panel = showResultsAction.getPanel();
		panel.applyOptions(options);
	}

	@Override
	public void updateSelection(ViewState options) {
		ManiaResultsPanel<CyNetwork, CyNode, CyEdge> panel = showResultsAction.getPanel();
		panel.updateSelection(options);
	}
}
