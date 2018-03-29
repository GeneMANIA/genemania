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
package org.genemania.plugin.delegates;

import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.genemania.exception.ApplicationException;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.selection.SessionManager;

public class NodeSetSelectionDelegate extends SelectionDelegate {
	
	private final Set<CyNode> nodes;

	public NodeSetSelectionDelegate(
			Set<CyNode> nodes,
			boolean selected,
			CyNetwork network,
			SessionManager manager,
			GeneMania plugin,
			CytoscapeUtils cytoscapeUtils
	) {
		super(selected, network, manager, plugin, cytoscapeUtils);
		this.nodes = nodes;
	}
	
	@Override
	protected void handleSelection(ViewState options) throws ApplicationException {
		for (CyNode node : nodes) {
			String name = cytoscapeUtils.getAttribute(network, node, CytoscapeUtils.GENE_NAME_ATTRIBUTE, String.class);
			options.setGeneHighlighted(name, selected);
		}
	}
}
