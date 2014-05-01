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

package org.genemania.plugin.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JCheckBox;

import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.proxies.NetworkProxy;

public class NetworkChangeListener<NETWORK, NODE, EDGE> implements ActionListener {
	private final Group<?, ?> source;
	private final Map<Object, ViewState> configurations;
	private final CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils;
	
	public NetworkChangeListener(Group<?, ?> group, Map<Object, ViewState> configurations, CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils) {
		this.source = group;
		this.configurations = configurations;
		this.cytoscapeUtils = cytoscapeUtils;
	}

	public void actionPerformed(ActionEvent e) {
		JCheckBox checkBox = (JCheckBox) e.getSource();
		NETWORK network = cytoscapeUtils.getCurrentNetwork();
		if (network == null) {
			return;
		}
		boolean selected = checkBox.isSelected();
		NetworkProxy<NETWORK, NODE, EDGE> networkProxy = cytoscapeUtils.getNetworkProxy(network);
		ViewState config = configurations.get(networkProxy.getIdentifier());
		if (config == null) {
			return;
		}
		
		cytoscapeUtils.setHighlight(config, source, network, selected);
	}

}
