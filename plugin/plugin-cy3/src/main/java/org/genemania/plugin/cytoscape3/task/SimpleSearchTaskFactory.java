package org.genemania.plugin.cytoscape3.task;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

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
import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.controllers.RetrieveRelatedGenesController;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.cytoscape3.actions.RetrieveRelatedGenesAction;
import org.genemania.plugin.cytoscape3.view.QueryBar;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.model.impl.InteractionNetworkGroupImpl;
import org.genemania.plugin.parsers.Query;
import org.genemania.plugin.selection.NetworkSelectionManager;
import org.genemania.type.CombiningMethod;
import org.genemania.type.ScoringMethod;

public class SimpleSearchTaskFactory implements NetworkSearchTaskFactory, ActionListener {

	@Tunable(description = "Max Resultant Genes:")
	public int geneLimit = 20;
	
	@Tunable(description="Advanced Search...")
	public UserAction advancedSearchAction = new UserAction(this);
	
	private static final String ID = "ca.utoronto.GeneMANIA";
	private static final String NAME = "GeneMANIA";
	private static final String DESCRIPTION = "Search related genes on GeneMANIA";
	private static final String WEBSITE_URL = "http://genemania.org/";
	
	private final Icon icon;
	private URL website;
	
	private QueryBar queryBar;
	
	private final GeneMania<CyNetwork, CyNode, CyEdge> plugin;
	private final RetrieveRelatedGenesController<CyNetwork, CyNode, CyEdge> controller;
	private final RetrieveRelatedGenesAction retrieveRelatedGenesAction;
	private final CytoscapeUtils<CyNetwork, CyNode, CyEdge> cytoscapeUtils;
	private final CyServiceRegistrar serviceRegistrar;

	public SimpleSearchTaskFactory(
			GeneMania<CyNetwork, CyNode, CyEdge> plugin,
			RetrieveRelatedGenesController<CyNetwork, CyNode, CyEdge> controller,
			CytoscapeUtils<CyNetwork, CyNode, CyEdge> cytoscapeUtils, 
			RetrieveRelatedGenesAction retrieveRelatedGenesAction, 
			CyServiceRegistrar serviceRegistrar
	) {
		this.plugin = plugin;
		this.controller = controller;
		this.cytoscapeUtils = cytoscapeUtils;
		this.retrieveRelatedGenesAction = retrieveRelatedGenesAction;
		this.serviceRegistrar = serviceRegistrar;
		icon = new ImageIcon(getClass().getClassLoader().getResource("/img/logo_squared.png"));
		
		try {
			website = new URL(WEBSITE_URL);
		} catch (MalformedURLException e) {
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
				tm.setTitle("GeneMANIA");
				tm.setStatusMessage("Validating search...");
				tm.setProgress(-1);
				
				Query query = getQuery();
				
				if (query.getOrganism() == null)
					throw new RuntimeException("Please select an organism.");
				if (query.getGenes().isEmpty())
					throw new RuntimeException("Please enter one or more genes.");
				if (!hasValidGenes(query.getOrganism(), query.getGenes()))
					throw new RuntimeException("Please specify a set of valid gene names and try again.");
					
				tm.setStatusMessage("Searching...");
				
				List<Group<?, ?>> groups = getGroups(query.getOrganism());
				
				new Thread(() -> {
					CyNetwork network = controller.runMania(SwingUtilities.getWindowAncestor(queryBar), query, groups);
	
					cytoscapeUtils.handleNetworkPostProcessing(network);
					cytoscapeUtils.performLayout(network);
					cytoscapeUtils.maximize(network);
					
					NetworkSelectionManager<CyNetwork, CyNode, CyEdge> selManager = plugin.getNetworkSelectionManager();
					ViewState options = selManager.getNetworkConfiguration(network);
					plugin.applyOptions(options);
					plugin.showResults();
				}).start();
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

	@Override
	public void actionPerformed(ActionEvent e) {
		retrieveRelatedGenesAction.getDelegate().invoke();
	}
	
	private Query getQuery() {
		final Query query = new Query();
		query.setOrganism(queryBar.getSelectedOrganism());
		query.setGenes(new ArrayList<>(queryBar.getQueryGenes()));
		query.setGeneLimit(geneLimit);
		query.setAttributeLimit(0);
		query.setCombiningMethod(CombiningMethod.AUTOMATIC_SELECT);
		query.setScoringMethod(ScoringMethod.DISCRIMINANT);
		
		return query;
	}
	
	private List<Group<?, ?>> getGroups(Organism organism) {
		List<Group<?, ?>> groups = new ArrayList<>();
		
		// TODO get only default or selected groups
		for (InteractionNetworkGroup group : organism.getInteractionNetworkGroups())
			groups.add(new InteractionNetworkGroupImpl(group));
		
		return groups;
	}
	
	private boolean hasValidGenes(Organism organism, List<String> geneList) {
		DataSetManager dataSetManager = plugin.getDataSetManager();
		GeneCompletionProvider2 provider = dataSetManager.getDataSet().getCompletionProvider(organism);
		
		for (String gene : geneList) {
			if (provider.isValid(gene))
				return true;
		}
		
		return false;
	}
}
