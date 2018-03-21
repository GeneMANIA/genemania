package org.genemania.plugin.selection;

import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.genemania.domain.Gene;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.view.FunctionInfoPanel;
import org.genemania.plugin.view.GeneInfoPanel;
import org.genemania.plugin.view.NetworkChangeListener;
import org.genemania.plugin.view.NetworkGroupDetailPanel;
import org.genemania.plugin.view.components.BaseInfoPanel;

public interface NetworkSelectionManager {

	Long getSelectedNetworkId();

	int getTotalNetworks();

	void addNetworkConfiguration(CyNetwork network, ViewState config);

	ViewState getNetworkConfiguration(CyNetwork network);

	boolean isGeneManiaNetwork(CyNetwork network);

	NetworkChangeListener createChangeListener(Group<?, ?> group);

	void setSelectionListenerEnabled(boolean enabled);

	boolean isSelectionListenerEnabled();

	SelectionListener<Gene> createGeneListSelectionListener(GeneInfoPanel genePanel, ViewState options);

	/**
	 * Returns true if the referenceEdge belongs to a group which has an edge that's already selected.
	 */
	boolean checkSelectionState(CyEdge referenceEdge, Set<CyEdge> selectedEdges, CyNetwork network);

	SelectionListener<Group<?, ?>> createNetworkListSelectionListener(
			BaseInfoPanel<Group<?, ?>, NetworkGroupDetailPanel> panel, ViewState options);

	SelectionListener<Network<?>> createNetworkSelectionListener();

	SelectionListener<Gene> createFunctionListSelectionListener(FunctionInfoPanel functionPanel, SearchResult options);

	void setGeneMania(GeneMania geneMania);
}