package org.genemania.plugin.selection;

import java.util.Set;

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

public interface NetworkSelectionManager<NETWORK, NODE, EDGE> {

	String getSelectedNetworkId();

	int getTotalNetworks();

	void addNetworkConfiguration(NETWORK network, ViewState config);

	ViewState getNetworkConfiguration(NETWORK network);

	boolean isGeneManiaNetwork(NETWORK network);

	NetworkChangeListener<NETWORK, NODE, EDGE> createChangeListener(
			Group<?, ?> group);

	void setSelectionListenerEnabled(boolean enabled);

	boolean isSelectionListenerEnabled();

	SelectionListener<Gene> createGeneListSelectionListener(
			GeneInfoPanel genePanel, ViewState options);

	/**
	 * Returns true if the referenceEdge belongs to a group which has an edge
	 * that's already selected.
	 * @param referenceEdge
	 * @param selectedEdges
	 * @return
	 */
	boolean checkSelectionState(EDGE referenceEdge, Set<EDGE> selectedEdges,
			NETWORK network);

	SelectionListener<Group<?, ?>> createNetworkListSelectionListener(
			BaseInfoPanel<Group<?, ?>, NetworkGroupDetailPanel<NETWORK, NODE, EDGE>> panel,
			ViewState options);

	SelectionListener<Network<?>> createNetworkSelectionListener();

	SelectionListener<Gene> createFunctionListSelectionListener(
			FunctionInfoPanel functionPanel, SearchResult options);

	void setGeneMania(GeneMania<NETWORK, NODE, EDGE> geneMania);
}