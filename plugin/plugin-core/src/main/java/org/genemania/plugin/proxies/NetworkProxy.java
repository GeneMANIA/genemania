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

public interface NetworkProxy<T, NODE, EDGE> extends Proxy<T> {
	String getTitle();
	Collection<NODE> getNodes();
	Collection<EDGE> getEdges();
	Set<NODE> getSelectedNodes();
	Set<EDGE> getSelectedEdges();
	void setSelectedNode(NODE node, boolean selected);
	void setSelectedNodes(Collection<NODE> nodes, boolean selected);
	void setSelectedEdge(EDGE edge, boolean selected);
	void setSelectedEdges(Collection<EDGE> edges, boolean selected);
	void unselectAllEdges();
	void unselectAllNodes();
	Collection<String> getNodeAttributeNames();
	Collection<String> getEdgeAttributeNames();
	Collection<NODE> getNeighbours(NODE node);
}
