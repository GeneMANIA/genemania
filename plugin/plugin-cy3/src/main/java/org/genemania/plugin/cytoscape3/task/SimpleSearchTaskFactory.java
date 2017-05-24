package org.genemania.plugin.cytoscape3.task;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.cytoscape.application.swing.search.NetworkSearchTaskFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.util.UserAction;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.controllers.RetrieveRelatedGenesController;
import org.genemania.plugin.cytoscape3.actions.RetrieveRelatedGenesAction;
import org.genemania.plugin.cytoscape3.view.QueryBar;

public class SimpleSearchTaskFactory implements NetworkSearchTaskFactory {

	@Tunable(description = "Max Resultant Genes:")
	public int maxResGenes = 20;
	
	@Tunable(description="Advanced Search...")
	public UserAction advancedSearchAction = new UserAction(null);
	
	private static final String ID = "ca.utoronto.GeneMANIA";
	private static final String NAME = "GeneMANIA";
	private static final String DESCRIPTION = "Search related genes on GeneMANIA";
	private static final String WEBSITE_URL = "http://genemania.org/";
	
	private final Icon icon;
	private URL website;
	
	private QueryBar queryBar;
	
	private final GeneMania<CyNetwork, CyNode, CyEdge> plugin;
	private final RetrieveRelatedGenesController<CyNetwork, CyNode, CyEdge> controller;
	private final CyServiceRegistrar serviceRegistrar;

	public SimpleSearchTaskFactory(
			GeneMania<CyNetwork, CyNode, CyEdge> plugin,
			RetrieveRelatedGenesController<CyNetwork, CyNode, CyEdge> controller,
			RetrieveRelatedGenesAction retrieveRelatedGenesAction, 
			CyServiceRegistrar serviceRegistrar
	) {
		this.plugin = plugin;
		this.controller = controller;
		this.serviceRegistrar = serviceRegistrar;
		icon = new ImageIcon(getClass().getClassLoader().getResource("/img/logo_squared.png"));
		
		try {
			website = new URL(WEBSITE_URL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		try {
			advancedSearchAction.setActionListener(evt -> retrieveRelatedGenesAction.getDelegate().invoke());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public JComponent getQueryComponent() {
		if (queryBar == null) {
			queryBar = new QueryBar(plugin, controller, serviceRegistrar);
		}
		
		return queryBar;
	}
	
	@Override
	public JComponent getOptionsComponent() {
		return null;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new AbstractTask() {
			@Override
			public void run(TaskMonitor tm) throws Exception {
				System.out.println(
						"- Network Search [" + getName() + "]: " + ((QueryBar) getQueryComponent()).getQuery());
			}
		});
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public URL getWebsite() {
		return website;
	}

	@Override
	public TaskObserver getTaskObserver() {
		return null;
	}

	@Override
	public boolean isReady() {
		return ((QueryBar) getQueryComponent()).isReady();
	}
}
