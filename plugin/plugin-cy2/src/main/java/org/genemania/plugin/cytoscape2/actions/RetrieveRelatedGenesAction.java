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

import org.genemania.plugin.FileUtils;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.delegates.RetrieveRelatedGenesDelegate;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.RetrieveRelatedGenesDialog;
import org.genemania.plugin.view.util.UiUtils;

import cytoscape.util.CytoscapeAction;

public class RetrieveRelatedGenesAction<NETWORK, NODE, EDGE> extends CytoscapeAction {
	private static final long serialVersionUID = 1L;

	private final RetrieveRelatedGenesDelegate<NETWORK, NODE, EDGE> delegate;

	public RetrieveRelatedGenesAction(String name, GeneMania<NETWORK, NODE, EDGE> plugin, CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils, NetworkUtils networkUtils, UiUtils uiUtils, FileUtils fileUtils, TaskDispatcher taskDispatcher) {
		super(name);
		delegate = new RetrieveRelatedGenesDelegate<NETWORK, NODE, EDGE>(plugin, cytoscapeUtils, networkUtils, uiUtils, fileUtils, taskDispatcher);
    }
	
	@Override
	public void actionPerformed(ActionEvent event) {
		delegate.invoke();
	}

	public RetrieveRelatedGenesDialog<NETWORK, NODE, EDGE> getDialog() {
		return delegate.getDialog();
	}
}
