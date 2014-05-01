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
package org.genemania.plugin.cytoscape;

import java.awt.Color;
import java.awt.Frame;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.genemania.domain.Node;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.model.ViewStateBuilder;
import org.genemania.plugin.proxies.EdgeProxy;
import org.genemania.plugin.proxies.NetworkProxy;
import org.genemania.plugin.proxies.NodeProxy;
import org.genemania.plugin.selection.NetworkSelectionManager;

public interface CytoscapeUtils<NETWORK, NODE, EDGE> {
	static final String LOG_SCORE_ATTRIBUTE = "log score"; //$NON-NLS-1$
	static final String SCORE_ATTRIBUTE = "score"; //$NON-NLS-1$
	static final String RAW_WEIGHTS_ATTRIBUTE = "raw weights"; //$NON-NLS-1$
	static final String MAX_WEIGHT_ATTRIBUTE = "normalized max weight"; //$NON-NLS-1$
	static final String NODE_TYPE_ATTRIBUTE = "node type"; //$NON-NLS-1$
	static final String NODE_TYPE_RESULT = "result"; //$NON-NLS-1$
	static final String NODE_TYPE_QUERY = "query"; //$NON-NLS-1$
	static final String NODE_TYPE_ATTRIBUTE_NODE = "attribute"; //$NON-NLS-1$
	static final String GENE_NAME_ATTRIBUTE = "gene name"; //$NON-NLS-1$
	static final String SYNONYMS_ATTRIBUTE = "synonyms"; //$NON-NLS-1$
	static final String RANK_ATTRIBUTE = "rank"; //$NON-NLS-1$
	static final String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$
	static final String HIGHLIGHT_ATTRIBUTE = "highlight"; //$NON-NLS-1$
	static final String NETWORK_GROUP_NAME_ATTRIBUTE = "data type"; //$NON-NLS-1$
	
	static final String GENEMANIA_NETWORK_TYPE = "genemania"; //$NON-NLS-1$
	static final String ORGANISM_NAME_ATTRIBUTE = "organism"; //$NON-NLS-1$
	static final String DATA_VERSION_ATTRIBUTE = "data version"; //$NON-NLS-1$
	static final String NETWORKS_ATTRIBUTE = "source-networks"; //$NON-NLS-1$
	static final String COMBINING_METHOD_ATTRIBUTE = "combining method"; //$NON-NLS-1$
	static final String GENE_SEARCH_LIMIT_ATTRIBUTE = "search limit"; //$NON-NLS-1$
	static final String ATTRIBUTE_SEARCH_LIMIT_ATTRIBUTE = "attribute search limit"; //$NON-NLS-1$
	static final String NETWORK_NAMES_ATTRIBUTE = "networks"; //$NON-NLS-1$
	static final String ANNOTATION_ID_ATTRIBUTE = "annotations"; //$NON-NLS-1$
	static final String ANNOTATION_NAME_ATTRIBUTE = "annotation name"; //$NON-NLS-1$
	static final String ANNOTATIONS_ATTRIBUTE = "annotations"; //$NON-NLS-1$
	static final String ATTRIBUTE_NAME_ATTRIBUTE = "attribute name"; //$NON-NLS-1$
	
	static final String URL = "url"; //$NON-NLS-1$
	static final String TITLE = "title"; //$NON-NLS-1$
	static final String TAGS = "tags"; //$NON-NLS-1$
	static final String SOURCE_URL = "sourceUrl"; //$NON-NLS-1$
	static final String SOURCE = "source"; //$NON-NLS-1$
	static final String YEAR_PUBLISHED = "yearPublished"; //$NON-NLS-1$
	static final String PUBLICATION_NAME = "publicationName"; //$NON-NLS-1$
	static final String PROCESSING_DESCRIPTION = "processingDescription"; //$NON-NLS-1$
	static final String PUBMED_ID = "pubmedId"; //$NON-NLS-1$
	static final String INTERACTION_COUNT = "interactionCount"; //$NON-NLS-1$
	static final String AUTHORS = "authors"; //$NON-NLS-1$
	
	static final Color QUERY_COLOR = new Color(0, 0, 0);
	static final Color RESULT_COLOR = new Color(159, 159, 159);
	
	void expandAttributes(NETWORK cyNetwork, ViewState options,
			List<String> attributes);

	NODE getNode(NETWORK network, Node node, String preferredSymbol);

	void registerSelectionListener(NETWORK cyNetwork, NetworkSelectionManager<NETWORK, NODE, EDGE> manager, GeneMania<NETWORK, NODE, EDGE> plugin);

	void performLayout(NETWORK network);

	void applyVisualization(NETWORK network,
			Map<Long, Double> filterGeneScores,
			Map<String, Color> computeColors, double[] extrema);

	NETWORK createNetwork(DataSet data, String nextNetworkName, SearchResult result, ViewStateBuilder options,
			EdgeAttributeProvider provider);

	void setHighlight(ViewState config, Group<?, ?> group,
			NETWORK network, boolean selected);

	void setHighlighted(ViewState options, NETWORK cyNetwork, boolean highlighted);
	
	NETWORK getCurrentNetwork();
	
	void repaint();

	void updateVisualStyles(NETWORK network);
	
	void maximize(NETWORK network);

	Frame getFrame();

	Set<NETWORK> getNetworks();
	
	NetworkProxy<NETWORK, NODE, EDGE> getNetworkProxy(NETWORK network);
	NodeProxy<NODE> getNodeProxy(NODE node, NETWORK network);
	EdgeProxy<EDGE, NODE> getEdgeProxy(EDGE edge, NETWORK network);
	
	void handleNetworkPostProcessing(NETWORK network);

	Properties getGlobalProperties();
}
