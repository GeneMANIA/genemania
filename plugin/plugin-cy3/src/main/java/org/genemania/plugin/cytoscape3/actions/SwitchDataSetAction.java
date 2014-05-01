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
package org.genemania.plugin.cytoscape3.actions;

import java.awt.event.ActionEvent;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.Strings;

@SuppressWarnings("serial")
public class SwitchDataSetAction extends GeneManiaAction {

	public SwitchDataSetAction(Map<String, String> properties,
			CyApplicationManager applicationManager,
			GeneMania<CyNetwork, CyNode, CyEdge> geneMania,
			CyNetworkViewManager viewManager) {
		super(properties, applicationManager, geneMania, viewManager);
		putValue(NAME, Strings.changeData_menuLabel);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		geneMania.handleSwitch();
	}

}
