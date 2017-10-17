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
package org.genemania.plugin.proxies;

import java.util.Collection;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

@Deprecated
public interface NetworkProxy extends Proxy<CyNetwork> {
	
	String getTitle();

	Collection<CyNode> getNodes();

	Collection<CyEdge> getEdges();

	Set<CyNode> getSelectedNodes();

	Set<CyEdge> getSelectedEdges();

	void setSelectedNode(CyNode node, boolean selected);

	void setSelectedNodes(Collection<CyNode> nodes, boolean selected);

	void setSelectedEdge(CyEdge edge, boolean selected);

	void setSelectedEdges(Collection<CyEdge> edges, boolean selected);

	void unselectAllEdges();

	void unselectAllNodes();

	Collection<String> getNodeAttributeNames();

	Collection<String> getEdgeAttributeNames();

	Collection<CyNode> getNeighbours(CyNode node);
}
