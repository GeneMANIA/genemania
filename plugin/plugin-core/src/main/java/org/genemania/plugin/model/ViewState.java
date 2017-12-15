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
package org.genemania.plugin.model;

import java.util.Set;

public interface ViewState {

	boolean isEnabled(Group<?, ?> group);

	void setEnabled(Group<?, ?> group, boolean enabled);

	void setGeneHighlighted(String name, boolean highlighted);

	void setGroupHighlighted(Group<?, ?> group, boolean highlighted);

	boolean isGeneHighlighted(String name);

	boolean isGroupHighlighted(Group<?, ?> group);

	int getTotalHighlightedGenes();

	String getMostRecentNode();

	Group<?, ?> getMostRecentGroup();

	void clearHighlightedGroups();

	Group<?, ?> getGroup(String name);

	Group<?, ?> getGroup(Network<?> network);
	
	Long getNodeId(String nodeId);

	Set<Network<?>> getNetworksByEdge(String edgeId);

	void clearHighlightedNetworks();

	void setNetworkHighlighted(Network<?> network, boolean highlighted);
	
	boolean isNetworkHighlighted(Network<?> network);

	Set<String> getEdgeIds(Group<?, ?> source);

	SearchResult getSearchResult();
	
	Set<Group<?, ?>> getAllGroups();

	Set<Network<?>> getNetworksByNode(String nodeId);
}
