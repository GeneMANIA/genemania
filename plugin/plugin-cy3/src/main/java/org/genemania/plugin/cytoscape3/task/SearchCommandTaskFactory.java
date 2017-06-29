package org.genemania.plugin.cytoscape3.task;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.controllers.RetrieveRelatedGenesController;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.cytoscape3.actions.RetrieveRelatedGenesAction;

public class SearchCommandTaskFactory extends AbstractTaskFactory {

	private final GeneMania<CyNetwork, CyNode, CyEdge> plugin;
	private final RetrieveRelatedGenesController<CyNetwork, CyNode, CyEdge> controller;
	private final RetrieveRelatedGenesAction retrieveRelatedGenesAction;
	private final CytoscapeUtils<CyNetwork, CyNode, CyEdge> cytoscapeUtils;
	private final CyServiceRegistrar serviceRegistrar;

	public SearchCommandTaskFactory(
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
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
				new SearchCommandTask(plugin, controller, cytoscapeUtils, retrieveRelatedGenesAction, serviceRegistrar));
	}
}
