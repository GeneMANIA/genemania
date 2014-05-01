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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.cytoscape3.ThreadContextClassLoaderHack;
import org.genemania.plugin.delegates.AboutDelegate;
import org.genemania.plugin.view.AboutDialog;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public class AboutAction extends AbstractCyAction {

	private CySwingApplication application;
	private UiUtils uiUtils;

	public AboutAction(Map<String, String> properties, CyApplicationManager applicationManager, CySwingApplication application, UiUtils uiUtils, CyNetworkViewManager viewManager) {
		super(properties, applicationManager, viewManager);
		putValue(NAME, Strings.about_menuLabel);
		this.application = application;
		this.uiUtils = uiUtils;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		new ThreadContextClassLoaderHack(CytoscapeUtils.class.getClassLoader()) {
			@Override
			protected void runWithHack() {
				URL baseUrl = getParent(AboutDialog.class.getResource("about.html")); //$NON-NLS-1$
				AboutDelegate delegate = new AboutDelegate(application.getJFrame(), uiUtils, baseUrl);
				delegate.invoke();
			}
		}.run();
	}
	
	private static URL getParent(URL resource) {
		if (resource == null) {
			return null;
		}
		try {
			return resource.toURI().resolve(".").toURL(); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			return null;
		} catch (MalformedURLException e) {
			return null;
		}
	}
}
