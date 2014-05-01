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

import javax.swing.JPanel;

import cytoscape.util.CytoscapeAction;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;

/**
 * Base class for an Action that displays a singleton panel.  The panel is
 * created the first time the Action is invoked.  Subsequent invocations will
 * bring the panel into focus.
 */
public abstract class ShowCytoPanelAction<T extends JPanel> extends CytoscapeAction {
	public static final String SHOW = "show"; //$NON-NLS-1$
	public static final String HIDE = "hide"; //$NON-NLS-1$
	
	private static final long serialVersionUID = 1L;
	private final Object mutex = new Object();
	private T panel;
	private boolean visible;

	public ShowCytoPanelAction(String name) {
		super(name);
	}

	public void actionPerformed(ActionEvent e) {
		if (e != null && SHOW.equals(e.getActionCommand())) {
			showPanel();
		} else if (e != null && HIDE.equals(e.getActionCommand())) {
			hidePanel();
		} else if (visible) {
			hidePanel();
		} else {
			showPanel();
		}
	}

	protected boolean isVisible() {
		return visible;
	}
	
	public void hidePanel() {
		CytoPanel cytoPanel = getCytoPanel();
		int index = cytoPanel.indexOfComponent(getPanel());
		if (index != -1) {
			cytoPanel.remove(index);
		}
		if (visible) {
			visible = false;
			handleVisibilityChange(visible);
		}
	}

	public void showPanel() {
		CytoPanel cytoPanel = getCytoPanel();
		int index = cytoPanel.indexOfComponent(getPanel());
		if (index == -1) {
			addPanel();
		} else {
			cytoPanel.setSelectedIndex(index);
		}
		if (!visible) {
			visible = true;
			handleVisibilityChange(visible);
		}
	}

	public T getPanel() {
		synchronized (mutex) {
			if (panel == null) {
		        panel = createPanel();
		        addPanel();
			}
		}
		return panel;
	}
	
	void addPanel() {
		CytoPanel cytoPanel = getCytoPanel();
        cytoPanel.add(panel.getName(), panel);
        cytoPanel.setState(CytoPanelState.DOCK);
	}

	public abstract CytoPanel getCytoPanel();
	protected abstract T createPanel();
	protected abstract void handleVisibilityChange(boolean newState);
}
