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

package org.genemania.plugin.cytoscape2.actions;

import java.awt.event.ActionEvent;
import java.net.URL;

import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.delegates.AboutDelegate;
import org.genemania.plugin.view.AboutDialog;
import org.genemania.plugin.view.util.UiUtils;

import cytoscape.util.CytoscapeAction;

public class AboutAction<NETWORK, NODE, EDGE> extends CytoscapeAction {
	private static final long serialVersionUID = 1L;
	private final AboutDelegate delegate;

	public AboutAction(String description, UiUtils uiUtils, CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils) {
		super(description);
		URL baseUrl = AboutDialog.class.getResource("."); //$NON-NLS-1$
		delegate = new AboutDelegate(cytoscapeUtils.getFrame(), uiUtils, baseUrl);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		delegate.invoke();
	}
}
